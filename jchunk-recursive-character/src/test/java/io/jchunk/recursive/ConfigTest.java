package io.jchunk.recursive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import jchunk.chunker.Delimiter;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConfigTest {

    @Test
    void test_default_builder_config() {
        Config config = Config.defaultConfig();

        assertThat(config.getChunkSize()).isEqualTo(100);
        assertThat(config.getChunkOverlap()).isEqualTo(20);
        assertThat(config.getDelimiters()).containsExactly("\n\n", "\n", " ", "");
        assertThat(config.getKeepDelimiter()).isEqualTo(Delimiter.START);
        assertThat(config.getTrimWhiteSpace()).isTrue();
    }

    @Test
    void test_custom_config() {
        Config config = Config.builder()
                .chunkSize(50)
                .chunkOverlap(10)
                .delimiters(List.of("-", "!", "?"))
                .keepDelimiter(Delimiter.END)
                .trimWhitespace(false)
                .build();

        assertThat(config.getChunkSize()).isEqualTo(50);
        assertThat(config.getChunkOverlap()).isEqualTo(10);
        assertThat(config.getDelimiters()).containsExactly("-", "!", "?");
        assertThat(config.getKeepDelimiter()).isEqualTo(Delimiter.END);
        assertThat(config.getTrimWhiteSpace()).isFalse();
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("provideInvalidConfiguration")
    void test_invalid_config(Config.Builder invalidConfigToBuild, String expectedMessage) {
        assertThatThrownBy(invalidConfigToBuild::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideInvalidConfiguration() {
        return Stream.of(
                Arguments.of(Config.builder().chunkSize(0), "Chunk size must be greater than 0"),
                Arguments.of(Config.builder().chunkSize(-1), "Chunk size must be greater than 0"),
                Arguments.of(Config.builder().chunkOverlap(-1), "Chunk overlap must be greater than or equal to 0"),
                Arguments.of(
                        Config.builder().chunkSize(10).chunkOverlap(20),
                        "Chunk size must be greater than chunk overlap"));
    }
}
