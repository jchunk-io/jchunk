package io.jchunk.core.util;

import io.jchunk.core.Delimiter;
import java.util.*;

/**
 * Utility class for operations related to splitting and merging text content into chunks.
 * This class provides methods for dividing a string based on delimiters and joining
 * sentences into chunks with size and overlap constraints.
 */
public final class ChunkerUtil {

    private ChunkerUtil() {}

    /**
     * Splits the given content into a list of strings based on the specified delimiter. The behavior of
     * the split operation depends on the configuration of delimiter retention. It supports character-level
     * splitting if the delimiter is empty.
     *
     * @param content the input string to be split
     * @param delimiter the delimiter string used to split the content; if empty, the content is split at the character level
     * @return a list of non-blank substrings obtained after splitting
     */
    public static List<String> splitWithDelimiter(final String content, final String delimiter, final Delimiter keep) {
        if (delimiter.isEmpty()) {
            return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
        }

        var regex =
                switch (keep) {
                    case START -> String.format("(?=%1$s)", delimiter);
                    case END -> String.format("(?<=%1$s)", delimiter);
                    case NONE -> delimiter;
                };

        return Arrays.stream(content.split(regex, -1)).filter(s -> !s.isBlank()).toList();
    }

    /**
     * Merges a list of sentences into chunks based on the provided size and overlap constraints.
     * Each chunk is created by joining sentences within the specified size limit. Overlapping content
     * is maintained as specified by the overlap parameter. The method also supports trimming of chunks
     * if desired.
     *
     * @param sentences the list of sentences to merge into chunks
     * @param delimiter the string used to join sentences within a chunk
     * @param size      the maximum allowed size of a chunk
     * @param overlap   the number of characters to retain as overlap between consecutive chunks
     * @param trim      whether to trim surrounding whitespace in the merged chunks
     * @return a list of {@code Chunk} objects created by merging the input sentences
     */
    public static List<String> merge(
            final List<String> sentences,
            final String delimiter,
            final int size,
            final int overlap,
            final boolean trim) {
        if (sentences.isEmpty()) return Collections.emptyList();

        final var combinedSentences = new ArrayList<String>();
        final var window = new ArrayDeque<String>();
        var windowLen = 0;

        for (String sentence : sentences) {
            int addSize = sentence.length() + (window.isEmpty() ? 0 : delimiter.length());

            if (!window.isEmpty() && windowLen + addSize > size) {
                combinedSentences.add(create(window, delimiter, trim));

                while (windowLen > overlap && !window.isEmpty()) {
                    var removed = window.removeFirst();
                    windowLen -= removed.length();
                    if (!window.isEmpty()) windowLen -= delimiter.length();
                }
            }

            window.addLast(sentence);
            windowLen += (window.size() == 1 ? sentence.length() : delimiter.length() + sentence.length());
        }

        if (!window.isEmpty()) {
            combinedSentences.add(create(window, delimiter, trim));
        }
        return combinedSentences;
    }

    /**
     * Creates a string by joining the elements of the given deque using the specified glue string.
     * Optionally trims the resulting string based on the provided trim flag.
     *
     * @param window    the deque containing strings to be joined
     * @param glue      the string used to join the elements of the deque
     * @param trim      whether to trim surrounding whitespace from the result
     * @return the joined string, optionally trimmed
     */
    private static String create(final Deque<String> window, final String glue, final boolean trim) {
        var content = String.join(glue, window);
        return trim ? content.trim() : content;
    }
}
