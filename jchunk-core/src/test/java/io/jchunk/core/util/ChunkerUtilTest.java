package io.jchunk.core.util;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static io.jchunk.core.util.ChunkerUtil.merge;
import static io.jchunk.core.util.ChunkerUtil.splitWithDelimiter;
import static org.assertj.core.api.Assertions.assertThat;

import io.jchunk.core.Delimiter;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ChunkerUtilTest {

    @ParameterizedTest
    @MethodSource("provideMergeCases")
    void test_merge_logic(List<String> sentences, String delimiter, int size, int overlap, List<String> expected) {
        // when
        var result = merge(sentences, delimiter, size, overlap, true);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void test_merge_trim_whitespace() {
        // given
        var input = List.of("  word  ", "next");

        // when
        var trimmed = merge(input, " ", 100, 0, true);
        var untrimmed = merge(input, " ", 100, 0, false);

        // then
        assertThat(trimmed.getFirst()).isEqualTo("word   next");
        assertThat(untrimmed.getFirst()).isEqualTo("  word   next");
    }

    @Test
    void test_single_large_sentence() {
        // given
        var input = List.of("ThisIsAVeryLongSentence");

        // when
        var result = merge(input, " ", 5, 0, true);

        // then
        assertThat(result).containsExactly("ThisIsAVeryLongSentence");
    }

    @ParameterizedTest
    @MethodSource("provideAllSplitCases")
    void test_split_with_delimiter(String content, String delimiter, Delimiter policy, List<String> expectedChunks) {
        // when
        var chunks = splitWithDelimiter(content, delimiter, policy);

        // then
        assertThat(chunks).isEqualTo(expectedChunks);
    }

    private static Stream<Arguments> provideMergeCases() {
        return Stream.of(
                // case 1: Simple merge without exceeding size
                Arguments.of(List.of("Hello", "world"), " ", 20, 0, List.of("Hello world")),

                // case 2: Split into two chunks (No overlap)
                Arguments.of(List.of("Hello", "world", "again"), " ", 15, 0, List.of("Hello world", "again")),

                // case 3: Merge with overlap
                Arguments.of(List.of("A", "B", "C"), " ", 4, 1, List.of("A B", "B C")),

                // case 4: delimiter is an empty string
                Arguments.of(List.of("A", "B", "C", "D"), "", 2, 1, List.of("AB", "BC", "CD")),

                // case 5: large overlap (retains multiple fragments)
                Arguments.of(
                        List.of("Sentence1", "Sentence2", "Sentence3"),
                        " ",
                        25,
                        20,
                        List.of("Sentence1 Sentence2", "Sentence1 Sentence2 Sentence3")),

                // case 6: empty input
                Arguments.of(List.of(), " ", 10, 0, List.of()));
    }

    // @formatter:off

    private static Stream<Arguments> provideAllSplitCases() {
        return Stream.of(
                // --- START POLICY ---
                Arguments.of("A!!B", "!", Delimiter.START, List.of("A", "!", "!B")),
                Arguments.of("!A!B", "!", Delimiter.START, List.of("!A", "!B")),
                Arguments.of("A!B", "!", Delimiter.START, List.of("A", "!B")),

                // --- END POLICY ---
                Arguments.of("A..B", "\\.", Delimiter.END, List.of("A.", ".", "B")),
                Arguments.of("A.B.", "\\.", Delimiter.END, List.of("A.", "B.")),
                Arguments.of(".A", "\\.", Delimiter.END, List.of(".", "A")),

                // --- NONE POLICY ---
                Arguments.of("A!B!C", "!", Delimiter.NONE, List.of("A", "B", "C")),
                Arguments.of("!!!", "!", Delimiter.NONE, List.of()),

                // --- EDGE CASES ---
                Arguments.of("ABC", "", Delimiter.NONE, List.of("A", "B", "C")),
                Arguments.of("   ", "!", Delimiter.START, List.of()),
                Arguments.of("A", "!", Delimiter.START, List.of("A")));
    }

    // @formatter:on

}
