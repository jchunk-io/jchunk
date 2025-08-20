package io.jchunk.semantic;

/**
 * Enumeration of the available strategies to split text into sentences.
 * <p>
 * Each constant defines a regular expression that determines how the text
 * should be split:
 * <ul>
 *   <li>{@link #DEFAULT} - Splits sentences whenever a period, question mark,
 *       or exclamation mark is followed by whitespace.</li>
 *   <li>{@link #LINE_BREAK} - Splits sentences at every line break ({@code \n}).</li>
 *   <li>{@link #PARAGRAPH} - Splits sentences at every paragraph break
 *       ({@code \n\n}).</li>
 * </ul>
 *
 * The regex pattern associated with each strategy can be obtained with
 * {@link #getStrategy()}.
 *
 * @author Pablo Sanchidrian Herrera
 */
public enum SentenceSplittingStrategy {
    /**
     * Split by punctuation marks (., ?, !) followed by whitespace.
     */
    DEFAULT("(?<=[.?!])\\s+"),

    /**
     * Split by single line breaks ({@code \n}).
     */
    LINE_BREAK("\n"),

    /**
     * Split by paragraph breaks ({@code \n\n}).
     */
    PARAGRAPH("\n\n");

    private final String strategy;

    SentenceSplittingStrategy(String strategy) {
        this.strategy = strategy;
    }

    /**
     * Returns the regular expression used by this strategy.
     *
     * @return regex pattern as a string
     */
    public String getStrategy() {
        return strategy;
    }
}
