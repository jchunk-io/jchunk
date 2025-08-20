package io.jchunk.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.jchunk.core.chunk.Chunk;
import io.jchunk.semantic.embedder.Embedder;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SemanticChunkerTest {

    private static final double MARGIN = 0.0001d;

    private final Embedder embeddingModel;

    private final SemanticChunker semanticChunker;

    SemanticChunkerTest() {
        this.embeddingModel = Mockito.mock(Embedder.class);
        this.semanticChunker = new SemanticChunker(embeddingModel);
    }

    // @formatter:off

    @Test
    void split_sentences_with_default_strategy() {
        // given
        var expectedResult = List.of(
                Sentence.of(0, "This is a test sentence."),
                Sentence.of(1, "How are u?"),
                Sentence.of(2, "I am fine thanks\nI am a test sentence!"),
                Sentence.of(3, "sure"));

        var content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";

        // when
        var defaultStrategy = SentenceSplittingStrategy.DEFAULT.getStrategy();
        List<Sentence> result = semanticChunker.splitSentences(content, defaultStrategy);

        // then
        assertThat(result).isNotNull().hasSize(expectedResult.size());

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getContent())
                    .isEqualTo(expectedResult.get(i).getContent());
        }
    }

    @Test
    void split_sentences_with_line_break_strategy() {
        // given
        var expectedResult = List.of(
                Sentence.of(0, "This is a test sentence. How are u? I am fine thanks"),
                Sentence.of(1, "I am a test sentence! sure"));

        var content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";

        // when
        var lineBreakStrategy = SentenceSplittingStrategy.LINE_BREAK.getStrategy();
        List<Sentence> result = semanticChunker.splitSentences(content, lineBreakStrategy);

        // then
        assertThat(result).isNotNull().hasSameSizeAs(expectedResult);

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
    }

    @Test
    void split_sentences_with_paragraph_strategy() {
        // given
        var expectedResult = List.of(
                Sentence.of(0, "This is a test sentence."),
                Sentence.of(1, "How are u? I am fine thanks"),
                Sentence.of(2, "I am a test sentence!\nsure"));

        var content = "This is a test sentence.\n\nHow are u? I am fine thanks\n\nI am a test sentence!\nsure";

        // when
        var paragraphStrategy = SentenceSplittingStrategy.PARAGRAPH.getStrategy();
        var result = semanticChunker.splitSentences(content, paragraphStrategy);

        // then
        assertThat(result).isNotNull().hasSameSizeAs(expectedResult);

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
        assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
    }

    @Test
    void combine_sentences_test() {
        // given
        var bufferSize = 2;
        var input = List.of(
                Sentence.of(0, "This"),
                Sentence.of(1, "is"),
                Sentence.of(2, "a"),
                Sentence.of(3, "sentence"),
                Sentence.of(4, "for"),
                Sentence.of(5, "you"),
                Sentence.of(6, "mate"));

        var expectedResult = List.of(
                Sentence.builder(0, "This").combined("This is a").build(),
                Sentence.builder(1, "is").combined("This is a sentence").build(),
                Sentence.builder(2, "a").combined("This is a sentence for").build(),
                Sentence.builder(3, "sentence")
                        .combined("is a sentence for you")
                        .build(),
                Sentence.builder(4, "for").combined("a sentence for you mate").build(),
                Sentence.builder(5, "you").combined("sentence for you mate").build(),
                Sentence.builder(6, "mate").combined("for you mate").build());

        // when
        var result = semanticChunker.combineSentences(input, bufferSize);

        // then
        assertThat(result).isNotNull().hasSameSizeAs(expectedResult);

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getIndex()).isEqualTo(expectedResult.get(i).getIndex());
            assertThat(result.get(i).getContent())
                    .isEqualTo(expectedResult.get(i).getContent());
            assertThat(result.get(i).getCombined())
                    .isEqualTo(expectedResult.get(i).getCombined());
        }
    }

    @ParameterizedTest
    @MethodSource("provideCombineSentencesFailureScenarios")
    void combine_sentences_fail(List<Sentence> sentences, Integer bufferSize, String expectedMsg) {
        assertThatThrownBy(() -> semanticChunker.combineSentences(sentences, bufferSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMsg);
    }

    @Test
    void embed_sentences() {
        // given
        Mockito.when(embeddingModel.embed(Mockito.anyList()))
                .thenReturn(List.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {4.0f, 5.0f, 6.0f}));

        var sentences = List.of(
                Sentence.builder(0, "").combined("This is a test sentence.").build(),
                Sentence.builder(0, "").combined("How are u?").build());

        var expectedResult = List.of(
                Sentence.builder(0, "")
                        .combined("This is a test sentence.")
                        .embedding(new float[] {1.0f, 2.0f, 3.0f})
                        .build(),
                Sentence.builder(0, "")
                        .combined("How are u?")
                        .embedding(new float[] {4.0f, 5.0f, 6.0f})
                        .build());

        // when
        var result = semanticChunker.embedSentences(embeddingModel, sentences);

        // then
        assertThat(result).isNotNull();

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getCombined())
                    .isEqualTo(expectedResult.get(i).getCombined());
            assertThat(result.get(i).getEmbedding())
                    .isEqualTo(expectedResult.get(i).getEmbedding());
        }
    }

    @ParameterizedTest
    @MethodSource("provideVectorScenarios")
    void cosine_similarity_test(float[] embedding1, float[] embedding2, double expectedResult, boolean isExact) {
        // when
        double result = semanticChunker.cosineSimilarity(embedding1, embedding2);

        // then
        double margin = isExact ? 0 : MARGIN;
        assertThat(result).isCloseTo(expectedResult, within(margin));
    }

    @Test
    void cosine_similarity_zero_vectors() {
        // given
        var embedding1 = new float[] {0.0f, 0.0f, 0.0f};
        var embedding2 = new float[] {0.0f, 0.0f, 0.0f};

        // when
        var result = semanticChunker.cosineSimilarity(embedding1, embedding2);

        // given
        assertThat(result).isNaN();
    }

    @Test
    void get_indices_above_threshold() {
        // given
        var percentile = 95;
        var distances = List.of(10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 70.0, 75.0);
        var expectedIndices = List.of(13);

        // when
        var actualIndices = semanticChunker.calculateBreakPoints(distances, percentile);

        // then
        assertThat(actualIndices).isEqualTo(expectedIndices);
    }

    @Test
    void generate_chunk_from_break_points() {
        // given
        var sentences = List.of(
                Sentence.of(0, "This"),
                Sentence.of(1, "is"),
                Sentence.of(2, "a"),
                Sentence.of(3, "test."),
                Sentence.of(4, "We"),
                Sentence.of(5, "are"),
                Sentence.of(6, "writing"),
                Sentence.of(7, "unit"),
                Sentence.of(8, "tests."));

        var breakPoints = List.of(2, 4, 6);

        var expectedChunks = List.of(
                new Chunk(0, "This is a"),
                new Chunk(1, "test. We"),
                new Chunk(2, "are writing"),
                new Chunk(3, "unit tests."));

        // when
        var actualChunks = semanticChunker.generateChunks(sentences, breakPoints);

        // then
        assertThat(actualChunks).isNotNull().hasSize(expectedChunks.size());

        for (int i = 0; i < actualChunks.size(); i++) {
            assertThat(actualChunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
            assertThat(actualChunks.get(i).content())
                    .isEqualTo(expectedChunks.get(i).content());
        }
    }

    @Test
    void generate_chunks_with_no_break_points() {
        // given
        var sentences = List.of(Sentence.of(0, "this is"), Sentence.of(1, "a test"));

        List<Integer> breakPoints = List.of();

        var expectedChunks = List.of(Chunk.of(0, "this is a test"));

        // when
        var actualChunks = semanticChunker.generateChunks(sentences, breakPoints);

        // then
        assertThat(actualChunks).isEqualTo(expectedChunks);
    }

    private static Stream<Arguments> provideCombineSentencesFailureScenarios() {
        final var nonEmptySentences = List.of(Sentence.of(0, "This"), Sentence.of(1, "is"));
        return Stream.of(
                Arguments.of(nonEmptySentences, 0, "The buffer size must be greater than 0"),
                Arguments.of(nonEmptySentences, null, "The buffer size cannot be null"),
                Arguments.of(nonEmptySentences, 2, "The buffer size must be smaller than the sentences size"),
                Arguments.of(null, 2, "The list of sentences cannot be null"),
                Arguments.of(List.of(), 2, "The list of sentences cannot be empty"));
    }

    private static Stream<Arguments> provideVectorScenarios() {
        return Stream.of(
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {1.0f, 2.0f, 3.0f}, 1.0, false),
                Arguments.of(new float[] {1.0f, 0.0f, 0.0f}, new float[] {0.0f, 1.0f, 0.0f}, 0.0, true),
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {-1.0f, -2.0f, -3.0f}, -1.0, false),
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {2.0f, 4.0f, 6.0f}, 1.0, false));
    }

    // @formatter:on

}
