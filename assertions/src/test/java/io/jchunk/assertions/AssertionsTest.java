package io.jchunk.assertions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("java:S2187")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AssertionsTest {

    private static final String INPUT_NOT_NULL_MSG = "Input must not be null";
    private static final String INPUT_NOT_BLANK_MSG = "Input must not be blank";
    private static final String INPUT_NOT_EMPTY_MSG = "Input must not be empty";
    private static final String INPUT_NOT_TRUE_MSG = "Input must not be true";
    private static final String INPUT_NOT_FALSE_MSG = "Input must not be false";

    @Test
    void not_null_ok() {
        assertDoesNotThrow(() -> Assertions.notNull("x", "must not be null"));
        assertDoesNotThrow(() -> Assertions.notNull(0, "must not be null"));
        assertDoesNotThrow(() -> Assertions.notNull(new Object(), "must not be null"));
    }

    @Test
    void not_blank_ok() {
        assertDoesNotThrow(() -> Assertions.notBlank("a", "blank"));
        assertDoesNotThrow(() -> Assertions.notBlank("  a  ", "blank"));
    }

    @Test
    void not_empty_ok() {
        assertDoesNotThrow(() -> Assertions.notEmpty("x", "empty"));
        assertDoesNotThrow(() -> Assertions.notEmpty(" ", "empty"));
    }

    @Test
    void not_empty_array_ok() {
        Integer[] arr = {1};
        assertDoesNotThrow(() -> Assertions.notEmpty(arr, "empty array"));
    }

    @Test
    void not_empty_collection_ok() {
        List<String> list = List.of("x");
        assertDoesNotThrow(() -> Assertions.notEmpty(list, "empty collection"));
    }

    @Test
    void is_true_ok() {
        assertDoesNotThrow(() -> Assertions.isTrue(true, "should be true"));
    }

    @Test
    void is_false_ok() {
        assertDoesNotThrow(() -> Assertions.isFalse(false, "should be false"));
    }

    @ParameterizedTest
    @MethodSource("provideNotSuccessfulScenarios")
    void assert_invalid_scenarios(Executable executable, Class<?> type, String msg) {
        assertThatThrownBy(executable::execute).isInstanceOf(type).hasMessage(msg);
    }

    private static Stream<Arguments> provideNotSuccessfulScenarios() {
        return Stream.of(
                Arguments.of(
                        (Executable) () -> Assertions.notNull(null, INPUT_NOT_NULL_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_NULL_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notBlank(null, INPUT_NOT_BLANK_MSG),
                        NullPointerException.class,
                        Assertions.INPUT_IS_NULL_ERROR_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notBlank("  ", INPUT_NOT_BLANK_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_BLANK_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty((String) null, INPUT_NOT_EMPTY_MSG),
                        NullPointerException.class,
                        Assertions.INPUT_IS_NULL_ERROR_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty("", INPUT_NOT_EMPTY_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_EMPTY_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty((Object[]) null, INPUT_NOT_EMPTY_MSG),
                        NullPointerException.class,
                        Assertions.INPUT_IS_NULL_ERROR_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty(new String[] {}, INPUT_NOT_EMPTY_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_EMPTY_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty((Collection<?>) null, INPUT_NOT_EMPTY_MSG),
                        NullPointerException.class,
                        Assertions.INPUT_IS_NULL_ERROR_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.notEmpty(List.of(), INPUT_NOT_EMPTY_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_EMPTY_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.isTrue(false, INPUT_NOT_TRUE_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_TRUE_MSG),
                Arguments.of(
                        (Executable) () -> Assertions.isFalse(true, INPUT_NOT_FALSE_MSG),
                        IllegalArgumentException.class,
                        INPUT_NOT_FALSE_MSG));
    }
}
