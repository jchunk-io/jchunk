package io.jchunk.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConfigTest {

    @Test
    void default_builder_config() {
        // when
        Config config = Config.defaultConfig();

        // then
        assertThat(config.getSentenceSplittingRegex()).isEqualTo(SentenceSplittingStrategy.DEFAULT.getStrategy());
        assertThat(config.getBufferSize()).isEqualTo(1);
        assertThat(config.getPercentile()).isEqualTo(95);
    }

    @Test
    void set_splitting_strategy() {
        // when
        Config config = Config.builder()
                .sentenceSplittingStrategy(SentenceSplittingStrategy.LINE_BREAK)
                .build();

        // then
        assertThat(config.getSentenceSplittingRegex()).isEqualTo(SentenceSplittingStrategy.LINE_BREAK.getStrategy());
    }

    @Test
    void set_user_defined_splitting_strategy() {
        // when
        var splittingRegex = "!";
        Config config =
                Config.builder().sentenceSplittingStrategy(splittingRegex).build();

        // then
        assertThat(config.getSentenceSplittingRegex()).isEqualTo(splittingRegex);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("provideInvalidConfig")
    void test_invalid_config(Config.Builder invalidConfigToBuild, String expectedMessage) {
        assertThatThrownBy(invalidConfigToBuild::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideInvalidConfig() {
        return Stream.of(
                Arguments.of(Config.builder().percentile(0), "The percentile must be greater than 0"),
                Arguments.of(Config.builder().percentile(100), "The percentile must be less than 100"),
                Arguments.of(Config.builder().bufferSize(0), "The bufferSize must be greater than 0"));
    }
}
