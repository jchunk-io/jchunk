package io.jchunk.semantic;

import io.jchunk.assertions.Assertions;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;
import io.jchunk.core.decorators.VisibleForTesting;
import io.jchunk.semantic.embedder.Embedder;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A semantic chunker that splits input text into a list of {@link Chunk}
 * based on semantic coherence rather than fixed length or naive delimiters.
 *
 * <p>The algorithm works in several stages:
 * <ol>
 *   <li>Split the input text into sentences using a configurable regex strategy.</li>
 *   <li>Combine sentences into overlapping windows (buffers) to preserve context.</li>
 *   <li>Generate embeddings for each combined sentence using the configured {@link Embedder}.</li>
 *   <li>Compute cosine similarity between consecutive sentence embeddings.</li>
 *   <li>Determine break points by applying a percentile threshold to the similarity scores.</li>
 *   <li>Assemble the final chunks by grouping sentences between break points.</li>
 * </ol>
 *
 * <p>Configuration such as splitting strategy, buffer size, and similarity threshold
 * is provided via {@link Config}.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class SemanticChunker implements IChunker {

    private final Embedder embedder;

    private final Config config;

    public SemanticChunker(final Embedder embedder) {
        this(embedder, Config.defaultConfig());
    }

    public SemanticChunker(final Embedder embedder, final Config config) {
        this.embedder = embedder;
        this.config = config;
    }

    /**
     * Splits the given text into semantic chunks.
     *
     * @param content   the raw text to split
     * @return a list of semantic chunks
     */
    @Override
    public List<Chunk> split(String content) {
        if (content.isBlank()) {
            return List.of();
        }

        var sentences = splitSentences(content, config.getSentenceSplittingRegex());

        if (sentences.isEmpty()) {
            return List.of();
        }

        if (sentences.size() == 1) {
            var sentence = sentences.getFirst();
            return List.of(Chunk.of(0, sentence.getContent()));
        }

        sentences = combineSentences(sentences, config.getBufferSize());
        sentences = embedSentences(embedder, sentences);
        var similarities = calculateSimilarities(sentences);
        var breakPoints = calculateBreakPoints(similarities, config.getPercentile());
        return generateChunks(sentences, breakPoints);
    }

    /**
     * Splits the content into raw sentences using the given regex.
     *
     * @param content   the text to split
     * @param regex     the regex used for splitting
     * @return a list of {@link Sentence} objects
     *
     * @implNote The regex is passed explicitly (instead of reading from {@link Config})
     *           to simplify testing with different splitting strategies.
     */
    @VisibleForTesting
    List<Sentence> splitSentences(final String content, final String regex) {
        var index = new AtomicInteger(0);
        return Arrays.stream(content.split(regex))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(sentence -> Sentence.of(index.getAndIncrement(), sentence))
                .toList();
    }

    /**
     * Combines sentences into overlapping windows according to the given buffer size.
     * Each combined sentence includes the current sentence and its surrounding context.
     *
     * @param sentences     the list of sentences
     * @param bufferSize    the number of sentences before and after to include
     * @return the list of sentences with combined context
     *
     * @implNote this method is implemented using the sliding window technique to reduce the time complexity
     */
    @VisibleForTesting
    List<Sentence> combineSentences(List<Sentence> sentences, int bufferSize) {
        Assertions.notNull(sentences, "The list of sentences cannot be null");
        Assertions.notEmpty(sentences, "The list of sentences cannot be empty");

        Assertions.isTrue(bufferSize > 0, "The buffer size must be greater than 0");
        Assertions.isTrue(bufferSize < sentences.size(), "The buffer size must be smaller than the sentences size");

        final var n = sentences.size();
        final var size = Math.min(bufferSize, n - 1);
        final var window = new ArrayDeque<String>(size);

        for (int i = 0; i <= size; ++i) {
            window.addLast(sentences.get(i).getContent());
        }

        for (int i = 0; i < n; ++i) {
            sentences.get(i).setCombined(String.join(" ", window));

            var nextRight = i + bufferSize + 1;
            var nextLeft = i - bufferSize + 1;

            if (nextRight < n) window.addLast(sentences.get(nextRight).getContent());
            if (nextLeft > 0) window.removeFirst();
        }

        return sentences;
    }

    /**
     * Generates embeddings for the given sentences using the configured {@link Embedder}.
     *
     * @param embedder  the embedding provider
     * @param sentences the list of sentences
     * @return the sentences enriched with embeddings
     */
    @VisibleForTesting
    List<Sentence> embedSentences(final Embedder embedder, final List<Sentence> sentences) {
        var contents = sentences.stream().map(Sentence::getCombined).toList();
        var embeddings = embedder.embed(contents);

        return IntStream.range(0, sentences.size())
                .mapToObj(i -> {
                    var sentence = sentences.get(i);
                    sentence.setEmbedding(embeddings.get(i));
                    return sentence;
                })
                .toList();
    }

    /**
     * Computes pairwise similarities between consecutive sentences.
     *
     * @param sentences the list of sentences with embeddings
     * @return an array of similarity scores
     */
    @VisibleForTesting
    double[] calculateSimilarities(final List<Sentence> sentences) {
        return IntStream.range(0, sentences.size() - 1)
                .parallel()
                .mapToDouble(i -> {
                    Sentence sentence1 = sentences.get(i);
                    Sentence sentence2 = sentences.get(i + 1);
                    return cosineSimilarity(sentence1.getEmbedding(), sentence2.getEmbedding());
                })
                .toArray();
    }

    /**
     * Calculate the similarity between the sentences embeddings
     *
     * @param sentence1 the first sentence embedding
     * @param sentence2 the second sentence embedding
     * @return the cosine similarity between the sentences
     */
    @VisibleForTesting
    double cosineSimilarity(final float[] sentence1, final float[] sentence2) {
        Assertions.notNull(sentence1, "The first sentence embedding cannot be null");
        Assertions.notNull(sentence2, "The second sentence embedding cannot be null");
        Assertions.isTrue(sentence1.length == sentence2.length, "The sentence embeddings must have the same size");

        double dotProduct = 0.0;
        double sentence1Norm = 0.0;
        double sentence2Norm = 0.0;
        for (int i = 0; i < sentence1.length; i++) {
            dotProduct += sentence1[i] * sentence2[i];
            sentence1Norm += Math.pow(sentence1[i], 2);
            sentence2Norm += Math.pow(sentence2[i], 2);
        }

        return dotProduct / (Math.sqrt(sentence1Norm) * Math.sqrt(sentence2Norm));
    }

    /**
     * Determines break points where new chunks should begin, based on the given percentile
     * threshold applied to similarity scores.
     *
     * @param distances     list of cosine similarities between consecutive sentences
     * @param percentile    the percentile threshold (e.g. 95)
     * @return array of indices representing break points
     */
    @VisibleForTesting
    int[] calculateBreakPoints(final double[] distances, final int percentile) {
        Assertions.notNull(distances, "The list of distances cannot be null");
        Assertions.isTrue(distances.length > 0, "The list of distances cannot be empty");

        var breakpointDistanceThreshold = calculatePercentile(distances, percentile);

        return IntStream.range(0, distances.length)
                .filter(i -> distances[i] >= breakpointDistanceThreshold)
                .toArray();
    }

    /**
     * Generates the final chunks by grouping sentences according to the break points.
     *
     * @param sentences     the list of sentences
     * @param breakPoints   the indices where splits should occur
     * @return the final list of semantic chunks
     */
    @VisibleForTesting
    List<Chunk> generateChunks(final List<Sentence> sentences, final int[] breakPoints) {
        Assertions.notNull(sentences, "The list of sentences cannot be null");
        Assertions.notEmpty(sentences, "The list of sentences cannot be empty");
        Assertions.notNull(breakPoints, "The list of break points cannot be null");

        var index = new AtomicInteger(0);

        return IntStream.range(0, breakPoints.length + 1)
                .mapToObj(i -> {
                    var start = i == 0 ? 0 : breakPoints[i - 1] + 1;
                    var end = i == breakPoints.length ? sentences.size() : breakPoints[i] + 1;
                    var content = sentences.subList(start, end).stream()
                            .map(Sentence::getContent)
                            .collect(Collectors.joining(" "));
                    return new Chunk(index.getAndIncrement(), content);
                })
                .toList();
    }

    private double calculatePercentile(final double[] distances, final int percentile) {
        Assertions.notNull(distances, "The list of distances cannot be null");
        Assertions.isTrue(percentile > 0 && percentile < 100, "The percentile must be between 1 and 99");
        Assertions.isTrue(distances.length > 0, "The list of distances cannot be empty");

        var copy = distances.clone();
        Arrays.parallelSort(copy);

        var n = distances.length;
        var rank = (int) Math.ceil(percentile / 100.0 * n);
        return copy[rank - 1];
    }
}
