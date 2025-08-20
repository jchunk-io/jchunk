package io.jchunk.fixed;

import io.jchunk.assertions.Assertions;
import io.jchunk.commons.Delimiter;

/**
 * Configuration for a fixed-size, delimiter-aware chunker.
 * <p>
 * Defines how text should be split into chunks, including:
 * <ul>
 *   <li>{@code chunkSize} - maximum size (in characters) of each chunk.</li>
 *   <li>{@code chunkOverlap} – number of characters to overlap between consecutive chunks.</li>
 *   <li>{@code delimiter} - delimiter defined as regex-string used to split the content.</li>
 *   <li>{@code keepDelimiter} – policy defining whether to keep delimiters at the start or end of a chunk.</li>
 *   <li>{@code trimWhiteSpace} – whether leading and trailing whitespace should be removed from chunks.</li>
 * </ul>
 * <p>
 * Instances are immutable and must be created via {@link Builder} or the convenience
 * method {@link #defaultConfig()}.
 *
 * <pre>{@code
 * Config cfg = Config.builder()
 *     .chunkSize(1000)
 *     .chunkOverlap(100)
 *     .delimiter("\\.") // dot
 *     .keepDelimiter(Delimiter.NONE)
 *     .trimWhitespace(true)
 *     .build();
 * }</pre>
 *
 * @see FixedChunker
 * @see Delimiter
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

    private final int chunkSize;
    private final int chunkOverlap;
    private final String delimiter;
    private final boolean trimWhitespace;
    private final Delimiter keepDelimiter;

    private Config(int chunkSize, int chunkOverlap, String delimiter, boolean trimWhitespace, Delimiter keepDelimiter) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.delimiter = delimiter;
        this.trimWhitespace = trimWhitespace;
        this.keepDelimiter = keepDelimiter;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public boolean getTrimWhitespace() {
        return trimWhitespace;
    }

    public Delimiter getKeepDelimiter() {
        return keepDelimiter;
    }

    /**
     * @return the default config
     */
    public static Config defaultConfig() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int chunkSize = 1000;

        private int chunkOverlap = 100;

        private String delimiter = " ";

        private boolean trimWhitespace = true;

        private Delimiter keepDelimiter = Delimiter.NONE;

        public Builder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder chunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
            return this;
        }

        public Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder trimWhitespace(boolean trimWhitespace) {
            this.trimWhitespace = trimWhitespace;
            return this;
        }

        public Builder keepDelimiter(Delimiter keepDelimiter) {
            this.keepDelimiter = keepDelimiter;
            return this;
        }

        public Config build() {
            Assertions.isTrue(chunkSize > 0, "Chunk size must be greater than 0");
            Assertions.isTrue(chunkOverlap >= 0, "Chunk overlap must be greater than or equal to 0");
            Assertions.isTrue(chunkSize > chunkOverlap, "Chunk size must be greater than chunk overlap");

            return new Config(chunkSize, chunkOverlap, delimiter, trimWhitespace, keepDelimiter);
        }
    }
}
