package io.jchunk.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that the visibility of a type or member has been relaxed
 * to make it more accessible for testing purposes.
 *
 * This annotation is a marker for developers and tools to identify elements that
 * are intentionally exposed beyond their intended encapsulation layer to allow for
 * more thorough testing. Such elements should not be considered part of the public API
 * and are subject to change or removal outside of testing contexts.
 *
 * Retention policy is set to CLASS, which means the annotation will not
 * be available at runtime but will be present in the class file for tooling purposes.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface VisibleForTesting {}
