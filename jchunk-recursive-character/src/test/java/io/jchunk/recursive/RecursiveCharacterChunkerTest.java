package io.jchunk.recursive;

import static org.assertj.core.api.Assertions.assertThat;

import io.jchunk.commons.Delimiter;
import io.jchunk.core.chunk.Chunk;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RecursiveCharacterChunkerTest {

    static String content =
            """
			One of the most important things I didn't understand about the world when I was a child is the degree to which the returns for performance are superlinear.

			Teachers and coaches implicitly told us the returns were linear. "You get out," I heard a thousand times, "what you put in." They meant well, but this is rarely true. If your product is only half as good as your competitor's, you don't get half as many customers. You get no customers, and you go out of business.

			It's obviously true that the returns for performance are superlinear in business. Some think this is a flaw of capitalism, and that if we changed the rules it would stop being true. But superlinear returns for performance are a feature of the world, not an artifact of rules we've invented. We see the same pattern in fame, power, military victories, knowledge, and even benefit to humanity. In all of these, the rich get richer. [1]
			""";

    @Test
    void test_content_split() {
        Config config = Config.builder().chunkSize(65).chunkOverlap(0).build();
        RecursiveCharacterChunker chunker = new RecursiveCharacterChunker(config);

        List<Chunk> expectedChunks = List.of(
                Chunk.of(0, "One of the most important things I didn't understand about the"),
                Chunk.of(1, "world when I was a child is the degree to which the returns for"),
                Chunk.of(2, "performance are superlinear."),
                Chunk.of(3, "Teachers and coaches implicitly told us the returns were linear."),
                Chunk.of(4, "\"You get out,\" I heard a thousand times, \"what you put in.\" They"),
                Chunk.of(5, "meant well, but this is rarely true. If your product is only"),
                Chunk.of(6, "half as good as your competitor's, you don't get half as many"),
                Chunk.of(7, "customers. You get no customers, and you go out of business."),
                Chunk.of(8, "It's obviously true that the returns for performance are"),
                Chunk.of(9, "superlinear in business. Some think this is a flaw of"),
                Chunk.of(10, "capitalism, and that if we changed the rules it would stop being"),
                Chunk.of(11, "true. But superlinear returns for performance are a feature of"),
                Chunk.of(12, "the world, not an artifact of rules we've invented. We see the"),
                Chunk.of(13, "same pattern in fame, power, military victories, knowledge, and"),
                Chunk.of(14, "even benefit to humanity. In all of these, the rich get richer."),
                Chunk.of(15, "[1]"));

        List<Chunk> chunks = chunker.split(content);

        assertThat(chunks).isNotNull().hasSize(expectedChunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
            assertThat(chunks.get(i).content()).isEqualTo(expectedChunks.get(i).content());
        }
    }

    @Test
    void test_split_with_big_chunk_size() {
        var config = Config.builder().chunkSize(450).chunkOverlap(0).build();
        var chunker = new RecursiveCharacterChunker(config);

        var expectedChunks = List.of(
                Chunk.of(
                        0,
                        "One of the most important things I didn't understand about the world when I was a child is the degree to which the returns for performance are superlinear."),
                Chunk.of(
                        1,
                        "Teachers and coaches implicitly told us the returns were linear. \"You get out,\" I heard a thousand times, \"what you put in.\" They meant well, but this is rarely true. If your product is only half as good as your competitor's, you don't get half as many customers. You get no customers, and you go out of business."),
                Chunk.of(
                        2,
                        "It's obviously true that the returns for performance are superlinear in business. Some think this is a flaw of capitalism, and that if we changed the rules it would stop being true. But superlinear returns for performance are a feature of the world, not an artifact of rules we've invented. We see the same pattern in fame, power, military victories, knowledge, and even benefit to humanity. In all of these, the rich get richer. [1]"));

        var chunks = chunker.split(content);

        assertThat(chunks).isNotNull().hasSize(expectedChunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
            assertThat(chunks.get(i).content()).isEqualTo(expectedChunks.get(i).content());
        }
    }

    @Test
    void test_split_with_non_splittable_config_and_content() {
        // given
        var testText = "no split";
        var config = Config.builder()
                .chunkSize(10)
                .chunkOverlap(0)
                .delimiters(List.of("!"))
                .keepDelimiter(Delimiter.NONE)
                .build();
        var chunker = new RecursiveCharacterChunker(config);

        // when
        var chunks = chunker.split(testText);

        // then
        assertThat(chunks).isNotNull().hasSize(1).extracting(Chunk::content).containsExactly(testText);
    }

    @Test
    void test_deterministic_recursive_fallback() {
        var config = Config.builder()
                .chunkSize(10)
                .chunkOverlap(0)
                .delimiters(List.of("\n", " ", ""))
                .keepDelimiter(Delimiter.END)
                .build();
        var chunker = new RecursiveCharacterChunker(config);

        // when
        var chunks = chunker.split("Hellooooooooooo world this is\nsome text");

        // then
        assertThat(chunks)
                .hasSize(5)
                .extracting(Chunk::content)
                .containsExactly("Helloooooo", "ooooo", "world", "this is", "some text");
    }

    @Test
    void test_index_monotonicity_across_recursion() {
        // given
        var config = Config.builder()
                .chunkSize(10)
                .chunkOverlap(0)
                .delimiters(List.of("\n", " "))
                .build();
        var chunker = new RecursiveCharacterChunker(config);

        // when
        var chunks = chunker.split("LineOne is long\nLineTwo");

        // then
        assertThat(chunks).hasSize(3).extracting(Chunk::content).containsExactly("LineOne is", "long", "LineTwo");
    }

    @ParameterizedTest(name = "{index}: \"{0}\" => \"{1}\"")
    @MethodSource("provideEmptyAndNullCases")
    void test_null_and_empty_inputs(String input, int expectedSize) {
        var config = Config.builder().build();
        var chunker = new RecursiveCharacterChunker(config);

        var chunks = chunker.split(input);
        assertThat(chunks).hasSize(expectedSize);
    }

    @ParameterizedTest
    @MethodSource("provideAllSplitCases")
    void test_split_with_delimiter(String content, String delimiter, Delimiter policy, List<String> expectedChunks) {
        // given
        var config = Config.builder()
                .delimiters(List.of(delimiter))
                .keepDelimiter(policy)
                .build();

        var chunker = new RecursiveCharacterChunker(config);

        // when
        var chunks = chunker.splitWithDelimiter(content, delimiter);

        // then
        assertThat(chunks).isEqualTo(expectedChunks);
    }

    @ParameterizedTest
    @MethodSource("provideSplitWithOverlapScenarios")
    void test_split_with_overlap(Delimiter keepDelimiter, List<String> expectedContent) {
        // given
        var testText = "no! this is. A split";
        var config = Config.builder()
                .chunkSize(10)
                .chunkOverlap(2)
                .delimiters(List.of("!", "\\."))
                .keepDelimiter(keepDelimiter)
                .build();

        var chunker = new RecursiveCharacterChunker(config);

        // when
        var chunks = chunker.split(testText);

        // then
        assertThat(chunks).isNotNull().extracting(Chunk::content).containsAnyElementsOf(expectedContent);
    }

    private static Stream<Arguments> provideEmptyAndNullCases() {
        return Stream.of(Arguments.of("", 0), Arguments.of(null, 0), Arguments.of("   ", 0));
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

    private static Stream<Arguments> provideSplitWithOverlapScenarios() {
        return Stream.of(
                Arguments.of(Delimiter.NONE, List.of("no", "this is", "A split")),
                Arguments.of(Delimiter.START, List.of("no", "! this is", ". A split")),
                Arguments.of(Delimiter.END, List.of("no!", "this is.", "A split")));
    }
}
