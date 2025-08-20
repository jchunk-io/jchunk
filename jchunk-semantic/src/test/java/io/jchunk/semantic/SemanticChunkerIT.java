package io.jchunk.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import io.jchunk.core.chunk.Chunk;
import io.jchunk.semantic.embedder.Embedder;
import io.jchunk.semantic.embedder.JChunkEmbedder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the {@link SemanticChunker}.
 * <p>
 * These tests validate that the chunking pipeline works end-to-end with different types of input.
 * Since embedding models may evolve over time and produce slightly different results,
 * the assertions do not rely on exact embedding values or number of chunks, but rather on:
 * <ul>
 *   <li>Ensuring that input content is correctly loaded.</li>
 *   <li>Verifying that large or splittable content produces at least one chunk.</li>
 *   <li>Ensuring that minimal content (e.g., a single sentence) still produces a valid chunk.</li>
 * </ul>
 * <p>
 * The tests cover edge cases like:
 * <ul>
 *   <li>Large, realistic content (MIT text).</li>
 *   <li>Single-sentence input that cannot be split further</li>
 * </ul>
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SemanticChunkerIT {

    private static final String MIT_CONTENT = getText("data/mit.txt");

    private static final Embedder embedder;

    private static final SemanticChunker semanticChunker;

    static {
        try {
            embedder = new JChunkEmbedder();
            semanticChunker = new SemanticChunker(embedder);
        } catch (Exception e) {
            throw new ExceptionInInitializerError();
        }
    }

    @Test
    void document_content_loaded() {
        assertThat(MIT_CONTENT).isNotBlank();
    }

    /**
     * Verifies that a large, realistic document is split into one or more chunks.
     * <p>
     * The exact number of chunks is not asserted, since embeddings may vary between model versions,
     * but the output must never be empty.
     */
    @Test
    void mit_content_is_split() {
        // when
        var chunks = semanticChunker.split(MIT_CONTENT);

        // then
        assertThat(chunks).isNotEmpty();
    }

    /**
     * Verifies that if the input consists of a single sentence,
     * the chunker produces exactly one chunk with the original content.
     */
    @Test
    void unique_sentence_is_split() {
        // given
        var text = "This is a example test to split.";

        // when
        var chunks = semanticChunker.split(text);

        // then
        assertThat(chunks).hasSize(1).extracting(Chunk::content).containsExactly(text);
    }

    // HELPERS

    private static String getText(final String resourcePath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {

            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}
