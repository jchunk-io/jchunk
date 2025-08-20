package io.jchunk.recursive;

import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import jchunk.chunker.Delimiter;

/**
 * Recursive, delimiter-aware chunker.
 *
 * <p>Implements {@link IChunker} to split text into {@link Chunk} objects using a hierarchical set
 * of delimiters (e.g., paragraph breaks, newlines, spaces, falling back to characters). The split
 * proceeds recursively: if a fragment exceeds {@code chunkSize}, it is re-split using the next delimiter.
 *
 * <h5>Features</h5>
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
 * <h5>Contract</h5>
 * <ul>
 *   <li>Non-empty input yields a non-empty list of chunks.</li>
 *   <li>The chunker <i>tries</i> to respect {@code chunkSize}; if it cannot (e.g., no suitable
 *       delimiter), a larger chunk may be produced and a warning is logged.</li>
 *   <li>Chunk indices are assigned monotonically during a single {@link #split(String)} call.</li>
 * </ul>
 *
 * <h5>Example</h5>
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

    private static final Logger logger = Logger.getLogger(RecursiveCharacterChunker.class.getName());

    private static final String LONGER_THAN_THE_SPECIFIED =
            "Created a chunk of size %d, which is longer than the specified %d";

    private final Config config;

    public RecursiveCharacterChunker() {
        this(Config.defaultConfig());
    }

    public RecursiveCharacterChunker(Config config) {
        this.config = config;
    }

    /**
     * Splits the provided content according to the configured delimiters and size/overlap policy.
     *
     * @param content input text to split
     * @return ordered list of {@link Chunk}
     */
    @Override
    public List<Chunk> split(String content) {
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
        var newDelimiters = new ArrayList<>(delimiters);
        var delimiter = getBestMatchingDelimiter(content, newDelimiters);

        var splits = splitWithDelimiter(content, delimiter);

        var goodSplits = new ArrayList<String>();
        var delimiterToUse = config.getKeepDelimiter() != Delimiter.NONE ? "" : delimiter;

        var chunks = new ArrayList<Chunk>();

        for (String split : splits) {
            if (split.length() < config.getChunkSize()) {
                goodSplits.add(split);
            } else {
                if (!goodSplits.isEmpty()) {
                    var generatedChunks = mergeSentences(goodSplits, delimiterToUse, index);
                    chunks.addAll(generatedChunks);
                    goodSplits.clear();
                }

                if (newDelimiters.isEmpty()) {
                    var chunkContent = config.getTrimWhiteSpace() ? split.trim() : split;
                    Chunk chunk = Chunk.of(index.getAndIncrement(), chunkContent);
                    chunks.add(chunk);
                } else {
                    List<Chunk> generatedChunks = splitContent(split, newDelimiters, index);
                    chunks.addAll(generatedChunks);
                }
            }
        }

        if (!goodSplits.isEmpty()) {
            List<Chunk> generatedChunks = mergeSentences(goodSplits, delimiterToUse, index);
            chunks.addAll(generatedChunks);
        }

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

            if (Pattern.compile(delimiter).matcher(content).find()) {
                iterator.remove();
                return delimiter;
            }
        }

        return "";
    }

    /**
     * Splits {@code content} by {@code delimiter}, applying the configured delimiter-retention policy
     * ({@link Delimiter#START}, {@link Delimiter#END}, or {@link Delimiter#NONE}).
     *
     * @param content   the text to split
     * @param delimiter the delimiter regex for content splitting
     * @return split fragments
     */
    private List<String> splitWithDelimiter(String content, String delimiter) {
        if (delimiter.isEmpty()) {
            return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
        }

        if (config.getKeepDelimiter() == Delimiter.NONE) {
            return Arrays.stream(content.split(delimiter))
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        String withDelimiter = "((?<=%1$s)|(?=%1$s))";
        List<String> preSplits = new ArrayList<>(List.of(content.split(String.format(withDelimiter, delimiter))));

        return config.getKeepDelimiter() == Delimiter.START
                ? splitWithDelimiterStart(preSplits)
                : splitWithDelimiterEnd(preSplits);
    }

    /**
     * Splits the content into sentences using the delimiter at the start of each sentence. {@link
     * Delimiter#START}
     *
     * @param preSplits pre-splits by the delimiter
     * @return the list of split sentences
     */
    private List<String> splitWithDelimiterStart(List<String> preSplits) {
        var splits = new ArrayList<String>();

        splits.add(preSplits.getFirst());
        IntStream.range(1, preSplits.size() - 1)
                .filter(i -> i % 2 == 1)
                .forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));

        return splits.stream().filter(s -> !s.isBlank()).toList();
    }

    /**
     * Splits the content into sentences using the delimiter at the end of each sentence. {@link
     * Delimiter#END}
     *
     * @param preSplits the pre-splits by the delimiter
     * @return the list of split sentences
     */
    private List<String> splitWithDelimiterEnd(List<String> preSplits) {
        var splits = new ArrayList<String>();

        IntStream.range(0, preSplits.size() - 1)
                .filter(i -> i % 2 == 0)
                .forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));
        splits.add(preSplits.getLast());

        return splits.stream().filter(s -> !s.isBlank()).toList();
    }

    /**
     * Merges fragments into size-bounded chunks while maintaining the configured overlap.
     * Emits a warning if a produced chunk exceeds {@code chunkSize}.
     *
     * @param sentences   candidate fragments to merge
     * @param delimiter   glue used when joining fragments
     * @param index       running chunk index
     * @return generated chunks (ordered)
     */
    private List<Chunk> mergeSentences(List<String> sentences, String delimiter, AtomicInteger index) {

        var currentLen = 0;
        var delimiterLen = delimiter.length();
        var chunks = new ArrayList<Chunk>();
        var currentChunk = new LinkedList<String>();

        for (String sentence : sentences) {
            int sentenceLength = sentence.length();

            if (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > config.getChunkSize()) {

                if (currentLen > config.getChunkSize()) {
                    var msg = String.format(LONGER_THAN_THE_SPECIFIED, currentLen, config.getChunkSize());
                    logger.warning(msg);
                }

                if (!currentChunk.isEmpty()) {
                    addChunk(chunks, currentChunk, delimiter, index);
                    currentLen = adjustCurrentChunkForOverlap(currentChunk, currentLen, delimiterLen);
                }
            }

            currentChunk.addLast(sentence);
            currentLen += sentenceLength + (currentChunk.size() > 1 ? delimiterLen : 0);
        }

        if (!currentChunk.isEmpty()) {
            addChunk(chunks, currentChunk, delimiter, index);
        }

        return chunks;
    }

    /**
     * Adds the chunk to the list of chunks.
     *
     * @param chunks        the list of chunks
     * @param currentChunk  the current chunk
     * @param delimiter     the delimiter
     * @param index         the index of the chunk
     */
    private void addChunk(List<Chunk> chunks, Deque<String> currentChunk, String delimiter, AtomicInteger index) {
        var generatedSentence = joinSentences(new ArrayList<>(currentChunk), delimiter);
        var chunk = Chunk.of(index.getAndIncrement(), generatedSentence);
        chunks.add(chunk);
    }

    /**
     * Adjusts the current chunk for overlap.
     *
     * @param currentChunk  the current chunk
     * @param currentLen    the current length of the chunk
     * @param delimiterLen  the length of the delimiter
     * @return the adjusted length of the chunk
     */
    private int adjustCurrentChunkForOverlap(Deque<String> currentChunk, int currentLen, int delimiterLen) {
        while (currentLen > config.getChunkOverlap() && !currentChunk.isEmpty()) {
            currentLen -= currentChunk.removeFirst().length() + (currentChunk.isEmpty() ? 0 : delimiterLen);
        }
        return currentLen;
    }

    /**
     * Joins the sentences into a single sentence.
     *
     * @param sentences the sentences to join
     * @param delimiter the delimiter to join the sentences
     * @return the generated sentence
     */
    private String joinSentences(List<String> sentences, String delimiter) {
        var generatedSentence = String.join(delimiter, sentences);
        if (config.getTrimWhiteSpace()) {
            generatedSentence = generatedSentence.trim();
        }

        return generatedSentence;
    }
}
