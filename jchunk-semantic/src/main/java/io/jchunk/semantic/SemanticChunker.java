package io.jchunk.semantic;

import io.jchunk.assertions.Assertions;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.core.chunk.IChunker;
import io.jchunk.core.decorators.VisibleForTesting;
import io.jchunk.semantic.embedder.Embedder;
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
     * @param content the raw text to split
     * @return a list of semantic chunks
     */
    @Override
    public List<Chunk> split(String content) {
        var sentences = splitSentences(content, config.getSentenceSplittingRegex());

        if (sentences.size() == 1) {
            var sentence = sentences.getFirst();
            var chunk = Chunk.of(0, sentence.getContent());
            return List.of(chunk);
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
     * @param content the text to split
     * @param regex the regex used for splitting
     * @return a list of {@link Sentence} objects
     *
     * @implNote The regex is passed explicitly (instead of reading from {@link Config})
     *           to simplify testing with different splitting strategies.
     */
    @VisibleForTesting
    List<Sentence> splitSentences(final String content, final String regex) {
        var index = new AtomicInteger(0);
        return Arrays.stream(content.split(regex))
                .map(sentence -> Sentence.of(index.getAndIncrement(), sentence))
                .toList();
    }

    /**
     * Combines sentences into overlapping windows according to the given buffer size.
     * Each combined sentence includes the current sentence and its surrounding context.
     *
     * @param sentences the list of sentences
     * @param bufferSize the number of sentences before and after to include
     * @return the list of sentences with combined context
     *
     * @implNote this method is implemented using the sliding window technique to reduce the time complexity
     */
    @VisibleForTesting
    List<Sentence> combineSentences(List<Sentence> sentences, Integer bufferSize) {
        Assertions.notNull(sentences, "The list of sentences cannot be null");
        Assertions.notEmpty(sentences, "The list of sentences cannot be empty");

        Assertions.notNull(bufferSize, "The buffer size cannot be null");
        Assertions.isTrue(bufferSize > 0, "The buffer size must be greater than 0");
        Assertions.isTrue(bufferSize < sentences.size(), "The buffer size must be smaller than the sentences size");

        var n = sentences.size();
        var windowSize = bufferSize * 2 + 1;
        var currentWindowSize = 0;
        var windowBuilder = new StringBuilder();

        for (int i = 0; i <= Math.min(bufferSize, n - 1); i++) {
            windowBuilder.append(sentences.get(i).getContent()).append(" ");
            currentWindowSize++;
        }

        windowBuilder.deleteCharAt(windowBuilder.length() - 1);

        for (int i = 0; i < n; ++i) {
            sentences.get(i).setCombined(windowBuilder.toString());

            if (currentWindowSize < windowSize && i + bufferSize + 1 < n) {
                windowBuilder
                        .append(" ")
                        .append(sentences.get(i + bufferSize + 1).getContent());
                currentWindowSize++;
            } else {
                windowBuilder.delete(
                        0, sentences.get(i - bufferSize).getContent().length() + 1);
                if (i + bufferSize + 1 < n) {
                    windowBuilder
                            .append(" ")
                            .append(sentences.get(i + bufferSize + 1).getContent());
                } else {
                    currentWindowSize--;
                }
            }
        }

        return sentences;
    }

    /**
     * Generates embeddings for the given sentences using the configured {@link Embedder}.
     *
     * @param embedder the embedding provider
     * @param sentences the list of sentences
     * @return the sentences enriched with embeddings
     */
    @VisibleForTesting
    List<Sentence> embedSentences(final Embedder embedder, final List<Sentence> sentences) {
        var sentencesText = sentences.stream().map(Sentence::getContent).toList();
        var embeddings = embedder.embed(sentencesText);

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
     * @return a list of similarity scores
     */
    @VisibleForTesting
    List<Double> calculateSimilarities(final List<Sentence> sentences) {
        return IntStream.range(0, sentences.size() - 1)
                .parallel()
                .mapToObj(i -> {
                    Sentence sentence1 = sentences.get(i);
                    Sentence sentence2 = sentences.get(i + 1);
                    return cosineSimilarity(sentence1.getEmbedding(), sentence2.getEmbedding());
                })
                .toList();
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
     * @param distances list of cosine similarities between consecutive sentences
     * @param percentile the percentile threshold (e.g. 95)
     * @return the list of indices representing break points
     */
    @VisibleForTesting
    List<Integer> calculateBreakPoints(final List<Double> distances, final int percentile) {
        Assertions.notNull(distances, "The list of distances cannot be null");
        Assertions.notEmpty(distances, "The list of distances cannot be empty");

        var breakpointDistanceThreshold = calculatePercentile(distances, percentile);

        return IntStream.range(0, distances.size())
                .filter(i -> distances.get(i) >= breakpointDistanceThreshold)
                .boxed()
                .toList();
    }

    /**
     * Generates the final chunks by grouping sentences according to the break points.
     *
     * @param sentences the list of sentences
     * @param breakPoints the indices where splits should occur
     * @return the final list of semantic chunks
     */
    @VisibleForTesting
    List<Chunk> generateChunks(final List<Sentence> sentences, final List<Integer> breakPoints) {
        Assertions.notNull(sentences, "The list of sentences cannot be null");
        Assertions.notEmpty(sentences, "The list of sentences cannot be empty");
        Assertions.notNull(breakPoints, "The list of break points cannot be null");

        var index = new AtomicInteger(0);

        return IntStream.range(0, breakPoints.size() + 1)
                .mapToObj(i -> {
                    int start = i == 0 ? 0 : breakPoints.get(i - 1) + 1;
                    int end = i == breakPoints.size() ? sentences.size() : breakPoints.get(i) + 1;
                    String content = sentences.subList(start, end).stream()
                            .map(Sentence::getContent)
                            .collect(Collectors.joining(" "));
                    return new Chunk(index.getAndIncrement(), content);
                })
                .toList();
    }

    private double calculatePercentile(final List<Double> distances, final int percentile) {
        Assertions.notNull(distances, "The list of distances cannot be null");

        var sortedDistances = distances.stream().sorted().toList();

        var rank = (int) Math.ceil(percentile / 100.0 * distances.size());
        return sortedDistances.get(rank - 1);
    }
}
