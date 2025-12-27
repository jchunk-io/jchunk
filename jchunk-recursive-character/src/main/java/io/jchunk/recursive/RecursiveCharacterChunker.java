package io.jchunk.recursive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import io.jchunk.core.Delimiter;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;

import static io.jchunk.core.util.ChunkerUtil.merge;
import static io.jchunk.core.util.ChunkerUtil.splitWithDelimiter;

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

    /**
     * Constructs a new instance of {@code RecursiveCharacterChunker} with the default configuration.
     * Internally, it delegates to the main constructor, initializing the object with the default
     * {@link Config} instance.
     */
    public RecursiveCharacterChunker() {
        this(Config.defaultConfig());
    }

    /**
     * Constructs a new instance of {@code RecursiveCharacterChunker} with the specified configuration.
     * Initializes the internal map of compiled patterns for each non-empty delimiter specified
     * in the provided {@link Config}.
     *
     * @param config the configuration defining splitting rules, including delimiters, chunk size,
     *               overlap parameters, and trimming policies
     */
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

        var sentences = splitContent(content, config.getDelimiters());
        return IntStream.range(0, sentences.size())
                .mapToObj(i -> Chunk.of(i, sentences.get(i)))
                .toList();
    }

    /**
     * Recursively splits {@code content} using the remaining {@code delimiters}.
     * The {@code index} is incremented as chunks are produced.
     *
     * @param content     the text to split
     * @param delimiters  remaining delimiters (will be consumed as recursion proceeds)
     * @return list of generated chunks
     */
    @SuppressWarnings("java:S3776")
    private List<String> splitContent(String content, List<String> delimiters) {
        if (content.length() <= config.getChunkSize() || delimiters.isEmpty()) {
            return List.of(content);
        }

        var newDelimiters = new ArrayList<>(delimiters);
        var delimiter = getBestMatchingDelimiter(content, newDelimiters);

        var splits = splitWithDelimiter(content, delimiter, config.getKeepDelimiter());
        var glue = (config.getKeepDelimiter() == Delimiter.NONE) ? delimiter : "";

        final var sentences = new ArrayList<String>(Math.max(4, splits.size()));
        final var bucket = new ArrayList<String>();

        for (String split : splits) {
            if (split.length() <= config.getChunkSize()) {
                bucket.add(split);
            } else {
                sentences.addAll(merge(
                        bucket, glue, config.getChunkSize(), config.getChunkOverlap(), config.getTrimWhiteSpace()));
                bucket.clear();
                sentences.addAll(splitContent(split, newDelimiters));
            }
        }

        sentences.addAll(
                merge(bucket, glue, config.getChunkSize(), config.getChunkOverlap(), config.getTrimWhiteSpace()));
        return sentences;
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
}
