package io.jchunk.recursive;

import io.jchunk.commons.Delimiter;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;
import io.jchunk.core.decorators.VisibleForTesting;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Recursive, delimiter-aware chunker.
 *
 * <p>Implements {@link IChunker} to split text into {@link Chunk} objects using a hierarchical set
 * of delimiters (e.g., paragraph breaks, newlines, spaces, falling back to characters). The split
 * proceeds recursively: if a fragment exceeds {@code chunkSize}, it is re-split using the next delimiter.
 *
 * <p>Features:
 * <ul>
 *   <li><b>Target size:</b> each chunk aims to be ≤ {@code chunkSize}.</li>
 *   <li><b>Overlap:</b> adjacent chunks keep {@code chunkOverlap} characters of overlap.</li>
 *   <li><b>Delimiters:</b> evaluated in the order provided by {@link Config#getDelimiters()}.
 *       An empty delimiter means character-level splitting.</li>
 *   <li><b>Delimiter retention:</b> controlled by {@link Config#getKeepDelimiter()}
 *       (START, END, or NONE).</li>
 *   <li><b>Whitespace handling:</b> leading/trailing whitespace trimming via
 *       {@link Config#getTrimWhiteSpace()}.</li>
 * </ul>
 *
 * <p>Contract:
 * <ul>
 *   <li>Non-empty input yields a non-empty list of chunks.</li>
 *   <li>The chunker <i>tries</i> to respect {@code chunkSize}; if it cannot (e.g., no suitable
 *       delimiter), a larger chunk may be produced and a warning is logged.</li>
 *   <li>Chunk indices are assigned monotonically during a single {@link #split(String)} call.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * Config cfg = Config.builder()
 *     .chunkSize(200)
 *     .chunkOverlap(40)
 *     .delimiters(List.of("\n\n", "\n", " ", "")) // paragraph → line → word → char
 *     .keepDelimiter(Delimiter.END)
 *     .trimWhitespace(true)
 *     .build();
 *
 * IChunker chunker = new RecursiveCharacterChunker(cfg);
 * List<Chunk> chunks = chunker.split(text);
 * }</pre>
 *
 * @see Config
 * @see Delimiter
 * @see IChunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public class RecursiveCharacterChunker implements IChunker {

    private final Config config;
    private final Map<String, Pattern> compiledPatterns;

    public RecursiveCharacterChunker() {
        this(Config.defaultConfig());
    }

    public RecursiveCharacterChunker(Config config) {
        this.config = config;
        this.compiledPatterns = new HashMap<>();

        for (String delimiter : config.getDelimiters()) {
            if (!delimiter.isEmpty()) {
                this.compiledPatterns.put(delimiter, Pattern.compile(delimiter));
            }
        }
    }

    /**
     * Splits the given content into smaller chunks based on the configuration's delimiters.
     * If the content is null or blank, it returns an empty list. Otherwise, it delegates
     * the splitting process to {@code splitContent}.
     *
     * @param content the input string to be split into chunks
     * @return a list of {@link Chunk} objects representing the split content;
     *         or an empty list if the content is null or blank
     */
    @Override
    public List<Chunk> split(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        return splitContent(content, config.getDelimiters(), new AtomicInteger(0));
    }

    /**
     * Recursively splits {@code content} using the remaining {@code delimiters}.
     * The {@code index} is incremented as chunks are produced.
     *
     * @param content     the text to split
     * @param delimiters  remaining delimiters (will be consumed as recursion proceeds)
     * @param index       running chunk index (shared across recursion)
     * @return list of generated chunks
     */
    @SuppressWarnings("java:S3776")
    private List<Chunk> splitContent(String content, List<String> delimiters, AtomicInteger index) {
        if (content.length() <= config.getChunkSize() || delimiters.isEmpty()) {
            return List.of(createChunk(index, content));
        }

        var newDelimiters = new ArrayList<>(delimiters);
        var delimiter = getBestMatchingDelimiter(content, newDelimiters);

        if (delimiter.isEmpty() && newDelimiters.equals(delimiters)) {
            var chunkContent = config.getTrimWhiteSpace() ? content.trim() : content;
            return List.of(Chunk.of(index.getAndIncrement(), chunkContent));
        }

        var splits = splitWithDelimiter(content, delimiter);
        var glue = (config.getKeepDelimiter() == Delimiter.NONE) ? delimiter : "";

        final var chunks = new ArrayList<Chunk>(Math.max(4, splits.size()));
        final var bucket = new ArrayList<String>();

        for (String split : splits) {
            if (split.length() <= config.getChunkSize()) {
                bucket.add(split);
            } else {
                chunks.addAll(createChunksFromBucket(bucket, glue, index));
                bucket.clear();
                chunks.addAll(splitContent(split, newDelimiters, index));
            }
        }

        chunks.addAll(createChunksFromBucket(bucket, glue, index));
        return chunks;
    }

    /**
     * Returns the first delimiter (in order) that matches {@code content}.
     * Removes the chosen delimiter from {@code delimiters}. If the empty delimiter is encountered,
     * clears the list to force character-level splitting.
     *
     * @param content       the text being analyzed
     * @param delimiters    candidate delimiters (modified in place)
     * @return the best matching delimiter, or {@code ""} if none
     */
    private String getBestMatchingDelimiter(String content, List<String> delimiters) {
        for (Iterator<String> iterator = delimiters.iterator(); iterator.hasNext(); ) {
            String delimiter = iterator.next();

            if (delimiter.isEmpty()) {
                delimiters.clear();
                return delimiter;
            }

            var pattern = compiledPatterns.get(delimiter);
            if (pattern.matcher(content).find()) {
                iterator.remove();
                return delimiter;
            }
        }

        return "";
    }

    /**
     * Creates a list of {@link Chunk} objects from the given bucket of strings by merging the
     * fragments using the specified glue and assigning unique identifiers to each chunk.
     *
     * @param bucket    a list of strings to be merged into chunks
     * @param glue      a string used to join the fragments in the bucket
     * @param index     an {@link AtomicInteger} used to assign unique identifiers to each chunk
     * @return a list of created {@link Chunk} objects or an empty list if the bucket is empty
     */
    private List<Chunk> createChunksFromBucket(List<String> bucket, String glue, AtomicInteger index) {
        if (bucket.isEmpty()) return Collections.emptyList();
        return mergeSentences(bucket, glue, index);
    }

    /**
     * Splits the given content into a list of strings based on the specified delimiter. The behavior of
     * the split operation depends on the configuration of delimiter retention. It supports character-level
     * splitting if the delimiter is empty.
     *
     * @param content the input string to be split
     * @param delimiter the delimiter string used to split the content; if empty, the content is split at the character level
     * @return a list of non-blank substrings obtained after splitting
     */
    @VisibleForTesting
    List<String> splitWithDelimiter(String content, String delimiter) {
        if (delimiter.isEmpty()) {
            return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
        }

        var regex =
                switch (config.getKeepDelimiter()) {
                    case START -> String.format("(?=%1$s)", delimiter);
                    case END -> String.format("(?<=%1$s)", delimiter);
                    case NONE -> delimiter;
                };

        return Arrays.stream(content.split(regex, -1)).filter(s -> !s.isBlank()).toList();
    }

    /**
     * Merges a list of sentences into chunks of text while respecting a maximum chunk size
     * and overlap constraints defined in the configuration. Chunks are created using the specified
     * delimiter to join sentences, and each chunk is assigned a unique identifier.
     *
     * @param sentences a list of sentences to be merged into chunks
     * @param delimiter the string used to join sentences within a chunk
     * @param index     an {@link AtomicInteger} used to generate unique identifiers for each chunk
     * @return a list of {@link Chunk} objects, where each represents a merged group of sentences
     */
    private List<Chunk> mergeSentences(List<String> sentences, String delimiter, AtomicInteger index) {
        final var chunks = new ArrayList<Chunk>();
        final var window = new ArrayDeque<String>();
        var windowLen = 0;

        for (String sentence : sentences) {
            int addSize = sentence.length() + (window.isEmpty() ? 0 : delimiter.length());

            if (!window.isEmpty() && windowLen + addSize > config.getChunkSize()) {
                addChunk(chunks, window, delimiter, index);

                while (windowLen > config.getChunkOverlap() && !window.isEmpty()) {
                    var removed = window.removeFirst();
                    windowLen -= removed.length();
                    if (!window.isEmpty()) {
                        windowLen -= delimiter.length();
                    }
                }
            }

            window.addLast(sentence);
            windowLen += (window.size() == 1 ? sentence.length() : delimiter.length() + sentence.length());
        }

        if (!window.isEmpty()) {
            addChunk(chunks, window, delimiter, index);
        }

        return chunks;
    }

    /**
     * Combines the elements of the current chunk into a single string using the specified delimiter
     * and adds the resulting {@link Chunk} to the collection of chunks.
     *
     * @param chunks        the collection of {@link Chunk} objects to which the new chunk will be added
     * @param currentChunk  a deque of strings representing the current chunk to be processed
     * @param delimiter     the delimiter used to join the elements of the current chunk
     * @param index         an {@link AtomicInteger} used to generate a unique identifier for each chunk
     */
    private void addChunk(List<Chunk> chunks, Deque<String> currentChunk, String delimiter, AtomicInteger index) {
        var sb = new StringBuilder();
        for (String s : currentChunk) {
            if (!sb.isEmpty()) sb.append(delimiter);
            sb.append(s);
        }

        chunks.add(createChunk(index, sb.toString()));
    }

    /**
     * Creates a new {@link Chunk} object with a unique identifier and content.
     * The content is optionally trimmed based on the configuration.
     *
     * @param index   an {@link AtomicInteger} used to generate a unique identifier for the chunk
     * @param content the content to be stored in the chunk
     * @return a new {@link Chunk} with a unique identifier and processed content
     */
    private Chunk createChunk(AtomicInteger index, String content) {
        return Chunk.of(index.getAndIncrement(), config.getTrimWhiteSpace() ? content.trim() : content);
    }
}
