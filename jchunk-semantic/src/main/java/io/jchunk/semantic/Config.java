package io.jchunk.semantic;

import io.jchunk.assertions.Assertions;

/**
 * Configuration object for {@link SemanticChunker}.
 * <p>
 * A {@code Config} defines how the semantic chunker splits text into sentences,
 * how much contextual overlap is used when combining sentences, and what
 * similarity threshold is applied when determining chunk boundaries.
 * <p>
 * Instances are immutable and can be created either with {@link #defaultConfig()}
 * or with the {@link Builder}.
 *
 * <h2>Configuration options</h2>
 * <ul>
 *   <li><b>Sentence splitting regex</b> – determines how the input text
 *       is split into sentences. Defaults to {@link SentenceSplittingStrategy#DEFAULT}.</li>
 *   <li><b>Percentile</b> – threshold (1–99) used when selecting break points
 *       based on sentence similarity scores. Defaults to {@code 95}.</li>
 *   <li><b>Buffer size</b> – number of neighboring sentences to include on each side
 *       when building the combined context window. Must be {@code > 0}.
 *       Defaults to {@code 1}.</li>
 * </ul>
 *
 * <p>Validation is performed when building the configuration:
 * <ul>
 *   <li>{@code percentile} must be between 1 and 99 (exclusive).</li>
 *   <li>{@code bufferSize} must be greater than 0.</li>
 * </ul>
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

    private final String sentenceSplittingRegex;
    private final int percentile;
    private final int bufferSize;

    private Config(String sentenceSplittingRegex, int percentile, int bufferSize) {
        this.sentenceSplittingRegex = sentenceSplittingRegex;
        this.percentile = percentile;
        this.bufferSize = bufferSize;
    }

    public String getSentenceSplittingRegex() {
        return sentenceSplittingRegex;
    }

    public int getPercentile() {
        return percentile;
    }

    public int getBufferSize() {
        return bufferSize;
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

        private String splittingRegex = SentenceSplittingStrategy.DEFAULT.getStrategy();
        private int percentile = 95;
        private int bufferSize = 1;

        public Builder sentenceSplittingStrategy(String splittingRegex) {
            this.splittingRegex = splittingRegex;
            return this;
        }

        public Builder sentenceSplittingStrategy(SentenceSplittingStrategy splittingStrategy) {
            this.splittingRegex = splittingStrategy.getStrategy();
            return this;
        }

        public Builder percentile(int percentile) {
            this.percentile = percentile;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Config build() {
            Assertions.isTrue(percentile > 0, "The percentile must be greater than 0");
            Assertions.isTrue(percentile < 100, "The percentile must be less than 100");
            Assertions.isTrue(bufferSize > 0, "The bufferSize must be greater than 0");

            return new Config(splittingRegex, percentile, bufferSize);
        }
    }
}
