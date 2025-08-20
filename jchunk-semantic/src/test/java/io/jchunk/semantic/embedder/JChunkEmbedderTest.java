package io.jchunk.semantic.embedder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JChunkEmbedder}.
 * <p>
 * These tests ensure that the embedding model can be successfully initialized
 * and that it produces embeddings with the expected dimensionality (for the current embedding model being used).
 * <p>
 * Since the internal values of the embeddings depend on the model and are not deterministic
 * across versions, only structural properties (such as dimension size) are asserted.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JChunkEmbedderTest {

    private static final int DEFAULT_EMBEDDER_DIM = 384;

    @Test
    @SuppressWarnings("java:S1612")
    void embedding_model_loads_successfully() {
        assertDoesNotThrow(() -> new JChunkEmbedder());
    }

    @Test
    void embedding_model_produces_expected_embedding_dimension() throws Exception {
        var embedder = new JChunkEmbedder();
        var embedding = embedder.embed("this is some text to test");

        assertThat(embedding).hasSize(DEFAULT_EMBEDDER_DIM);
    }
}
