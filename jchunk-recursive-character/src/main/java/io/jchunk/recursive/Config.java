package io.jchunk.recursive;

import io.jchunk.assertions.Assertions;
import java.util.ArrayList;
import java.util.List;
import jchunk.chunker.Delimiter;

/**
 * {@link Config} is a class that holds the configuration for the {@link RecursiveCharacterChunker}.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

    private final int chunkSize;
    private final int chunkOverlap;
    private final List<String> delimiters;
    private final Delimiter keepDelimiter;
    private final boolean trimWhiteSpace;

    private Config(
            int chunkSize, int chunkOverlap, List<String> delimiters, Delimiter keepDelimiter, boolean trimWhiteSpace) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.delimiters = delimiters;
        this.keepDelimiter = keepDelimiter;
        this.trimWhiteSpace = trimWhiteSpace;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public List<String> getDelimiters() {
        return delimiters;
    }

    public Delimiter getKeepDelimiter() {
        return keepDelimiter;
    }

    public boolean getTrimWhiteSpace() {
        return trimWhiteSpace;
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

        private int chunkSize = 100;

        private int chunkOverlap = 20;

        private List<String> delimiters = new ArrayList<>(List.of("\n\n", "\n", " ", ""));

        private Delimiter keepDelimiter = Delimiter.START;

        private boolean trimWhitespace = true;

        public Builder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder chunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
            return this;
        }

        public Builder separators(List<String> delimiters) {
            this.delimiters = delimiters;
            return this;
        }

        public Builder keepDelimiter(Delimiter keepDelimiter) {
            this.keepDelimiter = keepDelimiter;
            return this;
        }

        public Builder trimWhitespace(boolean trimWhitespace) {
            this.trimWhitespace = trimWhitespace;
            return this;
        }

        public Config build() {
            Assertions.isTrue(chunkSize > 0, "Chunk size must be greater than 0");
            Assertions.isTrue(chunkOverlap >= 0, "Chunk overlap must be greater than or equal to 0");
            Assertions.isTrue(chunkSize > chunkOverlap, "Chunk size must be greater than chunk overlap");

            return new Config(chunkSize, chunkOverlap, delimiters, keepDelimiter, trimWhitespace);
        }
    }
}
