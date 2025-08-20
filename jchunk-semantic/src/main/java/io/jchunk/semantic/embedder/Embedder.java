package io.jchunk.semantic.embedder;

import java.util.List;

/**
 * Defines an embedding model interface for transforming text into numerical vector
 * representations (embedding). An {@code Embedder} maps natural language into a continuous
 * vector space that can be used for similarity comparisons, clustering, or
 * downstream semantic tasks.
 *
 * <p>Implementations are responsible for providing consistent vector dimensions
 * and reproducible results for the same input.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * Embedder embedder = ...; // your implementation
 * float[] vector = embedder.embed("Hello world");
 * int dimension = embedder.getDimension();
 * }</pre>
 *
 * @author Pablo Sanchidrian Herrera
 */
public interface Embedder {

    /**
     * Generates an embedding for a single text input.
     *
     * <p>This is a convenience method that delegates to
     * {@link #embed(List)} with a singleton list and returns the first result.</p>
     *
     * @param text the text to embed, must not be {@code null}
     * @return the embedding vector for the input text
     */
    default float[] embed(String text) {
        return this.embed(List.of(text)).getFirst();
    }

    /**
     * Generates embeddings for a list of text inputs.
     *
     * <p>The order of the returned vectors corresponds exactly to the
     * order of the input strings.</p>
     *
     * @param text a list of text strings to embed, must not be {@code null}
     * @return a list of embedding vectors, one for each input string
     */
    List<float[]> embed(List<String> text);

    /**
     * Returns the dimensionality of the embedding space.
     *
     * <p>The default implementation infers the dimension by embedding
     * a sample string. Implementations may override this for efficiency.</p>
     *
     * @return the fixed number of dimensions of the embedding vectors
     */
    default int getDimension() {
        return this.embed("a").length;
    }
}
