package io.jchunk.fixed;

import static org.assertj.core.api.Assertions.assertThat;

import io.jchunk.core.Delimiter;
import io.jchunk.core.chunk.Chunk;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FixedChunkerTest {

    private FixedChunker chunker;

    private static final String CONTENT =
            "This is the text I would like to chunk up. It is the example text for this exercise";

    @Test
    void test_split_with_default_config() {
        // given
        chunker = new FixedChunker();

        // when
        var chunks = chunker.split(CONTENT);

        // then
        assertThat(chunks)
                .isNotNull()
                .containsExactly(Chunk.of(
                        0, "This is the text I would like to chunk up. It is the example text for this exercise"));
    }

    @Test
    void test_split_with_custom_delimiter() {
        // given
        var config =
                Config.builder().chunkSize(20).chunkOverlap(0).delimiter("\\.").build();
        chunker = new FixedChunker(config);

        // when
        var chunks = chunker.split("This is an example. Let's split on periods. Okay?");

        // then
        assertThat(chunks)
                .isNotNull()
                .containsExactly(
                        Chunk.of(0, "This is an example"), Chunk.of(1, "Let's split on periods"), Chunk.of(2, "Okay?"));
    }

    @Test
    void test_split_with_custom_config() {
        // given
        var config =
                Config.builder().chunkSize(35).chunkOverlap(4).delimiter("").build();
        chunker = new FixedChunker(config);

        // when
        var chunks = chunker.split(CONTENT);

        // then
        assertThat(chunks)
                .isNotNull()
                .containsExactly(
                        Chunk.of(0, "This is the text I would like to ch"),
                        Chunk.of(1, "o chunk up. It is the example text"),
                        Chunk.of(2, "ext for this exercise"));
    }

    @Test
    void test_split_with_custom_config_no_white_space() {
        // given
        var config = Config.builder()
                .chunkSize(35)
                .chunkOverlap(0)
                .delimiter("")
                .trimWhitespace(false)
                .build();
        chunker = new FixedChunker(config);

        // when
        var chunks = chunker.split(CONTENT);

        // then
        assertThat(chunks)
                .isNotNull()
                .containsExactly(
                        Chunk.of(0, "This is the text I would like to ch"),
                        Chunk.of(1, "unk up. It is the example text for "),
                        Chunk.of(2, "this exercise"));
    }

    @Test
    void test_split_with_custom_config_with_keep_delimiter_none() {
        // given
        var config = Config.builder()
                .chunkSize(35)
                .chunkOverlap(0)
                .delimiter("ch")
                .trimWhitespace(true)
                .keepDelimiter(Delimiter.NONE)
                .build();
        chunker = new FixedChunker(config);

        // when
        var chunks = chunker.split(CONTENT);

        // then
        assertThat(chunks)
                .isNotNull()
                .containsExactly(
                        Chunk.of(0, "This is the text I would like to"),
                        Chunk.of(1, "unk up. It is the example text for this exercise"));
    }

    @Test
    void test_split_into_sentences_with_blank_separator() {
        // given
        var config = Config.builder().delimiter("").build();
        chunker = new FixedChunker(config);

        // when
        var sentences = chunker.splitIntoSentences(CONTENT, config);

        // then
        assertThat(sentences)
                .isNotNull()
                .containsExactlyElementsOf(
                        CONTENT.chars().mapToObj(c -> String.valueOf((char) c)).toList());
    }

    @Test
    void test_split_into_sentences_with_no_delimiter() {
        // given
        var config = Config.builder().delimiter("ch").build();
        chunker = new FixedChunker(config);

        // when
        var sentences = chunker.splitIntoSentences(CONTENT, config);

        // then
        assertThat(sentences)
                .isNotNull()
                .containsExactly(
                        "This is the text I would like to ", "unk up. It is the example text for this exercise");
    }

    @Test
    void test_split_into_sentences_with_delimiter_start() {
        // given
        var config =
                Config.builder().delimiter("ch").keepDelimiter(Delimiter.START).build();
        chunker = new FixedChunker(config);

        // when
        var sentences = chunker.splitIntoSentences(CONTENT, config);

        // then
        assertThat(sentences)
                .isNotNull()
                .hasSize(2)
                .containsExactly(
                        "This is the text I would like to ", "chunk up. It is the example text for this exercise");
    }

    @Test
    void test_split_into_sentences_with_delimiter_end() {
        // given
        var config =
                Config.builder().delimiter("ch").keepDelimiter(Delimiter.END).build();
        chunker = new FixedChunker(config);

        // when
        var sentences = chunker.splitIntoSentences(CONTENT, config);

        // then
        assertThat(sentences)
                .isNotNull()
                .hasSize(2)
                .containsExactly(
                        "This is the text I would like to ch", "unk up. It is the example text for this exercise");
    }
}
