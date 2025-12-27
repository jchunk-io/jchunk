package io.jchunk.fixed;

import static io.jchunk.core.util.ChunkerUtil.merge;
import static io.jchunk.core.util.ChunkerUtil.splitWithDelimiter;

import io.jchunk.core.Delimiter;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;
import java.util.*;
import java.util.stream.IntStream;

/**
 * {@code FixedChunker} implements {@link IChunker} by splitting text into
 * fixed-size chunks based on a single delimiter.
 *
 * <p>It processes input by:
 * <ul>
 *   <li>Splitting the text into sentences/fragments using the configured delimiter
 *       (with optional retention of the delimiter at the start, end, or not at all).</li>
 *   <li>Merging those fragments into chunks that are approximately
 *       {@link Config#getChunkSize()} characters long.</li>
 *   <li>Ensuring an overlap of {@link Config#getChunkOverlap()} characters between
 *       consecutive chunks.</li>
 *   <li>Optionally trimming leading/trailing whitespace from each chunk.</li>
 * </ul>
 *
 * <p>If a chunk exceeds the configured maximum size because it cannot be split further,
 * a warning is logged.
 * <p>
 * Example:
 * <pre>{@code
 * Config cfg = Config.builder()
 *     .chunkSize(1000)
 *     .chunkOverlap(100)
 *     .delimiter(" ")
 *     .keepDelimiter(Delimiter.NONE)
 *     .trimWhitespace(true)
 *     .build();
 *
 * IChunker chunker = new FixedChunker(cfg);
 * List<Chunk> chunks = chunker.split("Your text here...");
 * }</pre>
 *
 * @see Config
 * @see Delimiter
 * @see Chunk
 *
 * @author Pablo Sanchidrian Herrera
 */
public class FixedChunker implements IChunker {

    private final Config config;

    /**
     * Creates an instance of {@code FixedChunker} with the default configuration provided
     * by {@link Config#defaultConfig()}.
     *
     * @see Config#defaultConfig()
     * @see FixedChunker#FixedChunker(Config)
     */
    public FixedChunker() {
        this(Config.defaultConfig());
    }

    /**
     * Constructs an instance of {@code FixedChunker} using the specified configuration.
     *
     * @param config the configuration object defining chunk size, overlap, delimiters, and other settings
     */
    public FixedChunker(Config config) {
        this.config = config;
    }

    @Override
    public List<Chunk> split(String content) {
        var sentences = splitIntoSentences(content, config);
        var combinedSentences = merge(
                sentences,
                config.getDelimiter(),
                config.getChunkSize(),
                config.getChunkOverlap(),
                config.getTrimWhitespace());

        return IntStream.range(0, combinedSentences.size())
                .mapToObj(i -> Chunk.of(i, combinedSentences.get(i)))
                .toList();
    }

    /**
     * Splits the given content into a list of strings based on the delimiter specified in the configuration.
     * If the delimiter is empty, the method splits the content into individual characters.
     *
     * @param content   the input string to be split
     * @param config    the configuration object containing the delimiter and other settings
     * @return a list of strings obtained by splitting the content
     */
    public List<String> splitIntoSentences(String content, Config config) {
        final var delimiter = config.getDelimiter();

        if (delimiter.isEmpty()) {
            return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
        }

        return splitWithDelimiter(content, delimiter, config.getKeepDelimiter());
    }
}
