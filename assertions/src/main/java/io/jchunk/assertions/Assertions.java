package io.jchunk.assertions;

import java.util.Collection;

/**
 * Lightweight runtime assertion utilities for JChunk.
 *
 * <p>These checks are meant to validate JChunk must follow conditions at runtime,
 * independent of the JVM {@code -ea} flag. They throw appropriate unchecked exceptions
 * ({@link IllegalArgumentException}, {@link NullPointerException})
 * instead of using the {@code assert} keyword.</p>
 *
 * <h2>When to use</h2>
 * <ul>
 *   <li>Guarding method <strong>preconditions</strong> (non-null, non-blank, non-empty, etc.).</li>
 *   <li>Verifying simple boolean conditions for arguments.</li>
 *   <li>Prefer {@code assert} only for internal invariants that should never fail in production.</li>
 * </ul>
 *
 * @author Pablo Sanchidrián Herrera
 */
public final class Assertions {

    static final String INPUT_IS_NULL_ERROR_MSG = "Input must not be null";

    private Assertions() {}

    /**
     * Ensures the given value is not {@code null}.
     *
     * @param value   the value to check
     * @param message the exception message if {@code value} is {@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures the given {@link String} is neither {@code null} nor blank (consisting only of
     * whitespace as determined by {@link String#isBlank()}).
     *
     * @param s       the string to check
     * @param message the exception message if the check fails
     * @throws NullPointerException     if {@code s} is {@code null}
     * @throws IllegalArgumentException if {@code s} is blank
     */
    public static void notBlank(String s, String message) {
        if (s == null) throw new NullPointerException(INPUT_IS_NULL_ERROR_MSG);
        if (s.isBlank()) throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the given {@link String} is neither {@code null} nor empty (length &gt; 0).
     *
     * @param s       the string to check
     * @param message the exception message if the check fails
     * @throws NullPointerException     if {@code s} is {@code null}
     * @throws IllegalArgumentException if {@code s} is empty
     */
    public static void notEmpty(String s, String message) {
        if (s == null) throw new NullPointerException(INPUT_IS_NULL_ERROR_MSG);
        if (s.isEmpty()) throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the given array is neither {@code null} nor empty (length &gt; 0).
     *
     * @param arr     the array to check
     * @param message the exception message if the check fails
     * @param <T>     the array element type
     * @throws NullPointerException     if {@code arr} is {@code null}
     * @throws IllegalArgumentException if {@code arr.length == 0}
     */
    public static <T> void notEmpty(T[] arr, String message) {
        if (arr == null) throw new NullPointerException(INPUT_IS_NULL_ERROR_MSG);
        if (arr.length == 0) throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the given collection is neither {@code null} nor empty (size &gt; 0).
     *
     * @param c       the collection to check
     * @param message the exception message if the check fails
     * @param <T>     the collection type
     * @throws NullPointerException     if {@code c} is {@code null}
     * @throws IllegalArgumentException if {@code c.isEmpty()}
     */
    public static <T extends Collection<?>> void notEmpty(T c, String message) {
        if (c == null) throw new NullPointerException(INPUT_IS_NULL_ERROR_MSG);
        if (c.isEmpty()) throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the given condition is {@code true}.
     *
     * @param condition the boolean condition to verify
     * @param message   the exception message if the condition is {@code false}
     * @throws IllegalArgumentException if {@code condition} is {@code false}
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the given condition is {@code false}.
     *
     * @param condition the boolean condition to verify
     * @param message   the exception message if the condition is {@code true}
     * @throws IllegalArgumentException if {@code condition} is {@code true}
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) throw new IllegalArgumentException(message);
    }
}
