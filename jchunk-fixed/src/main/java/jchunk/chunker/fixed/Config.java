package jchunk.chunker.fixed;

import jchunk.chunker.Delimiter;

/**
 * Configuration for the fixed chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public record Config(
        int chunkSize, int chunkOverlap, String delimiter, boolean trimWhitespace, Delimiter keepDelimiter) {

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
            assert chunkSize > 0 : "Chunk size must be greater than 0";
            assert chunkOverlap >= 0 : "Chunk overlap must be greater than or equal to 0";
            assert chunkSize > chunkOverlap : "Chunk size must be greater than chunk overlap";

            return new Config(chunkSize, chunkOverlap, delimiter, trimWhitespace, keepDelimiter);
        }
    }
}
