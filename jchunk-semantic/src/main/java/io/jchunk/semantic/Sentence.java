package io.jchunk.semantic;

/**
 * Sentence class to represent a sentence during the splitting process
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Sentence {

    private final int index;

    private final String content;

    private String combined;

    private float[] embedding;

    /**
     * Constructs a Sentence
     *
     * @param index index to identify sentence position
     * @param content sentence content
     *
     * @implNote by default, combined is set to content if combined is null
     */
    private Sentence(int index, String content) {
        this.index = index;
        this.content = content;
        this.combined = content;
        this.embedding = new float[0];
    }

    public static Sentence of(final int index, final String content) {
        return new Sentence(index, content);
    }

    public int getIndex() {
        return index;
    }

    public String getContent() {
        return content;
    }

    public String getCombined() {
        return combined == null ? content : combined;
    }

    public void setCombined(String combined) {
        this.combined = combined;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public static Builder builder(final int index, final String content) {
        return new Builder(index, content);
    }

    public static class Builder {

        private final Sentence sentence;

        public Builder(final int index, final String content) {
            this.sentence = Sentence.of(index, content);
        }

        public Builder combined(final String combined) {
            this.sentence.setCombined(combined);
            return this;
        }

        public Builder embedding(final float[] embedding) {
            this.sentence.setEmbedding(embedding);
            return this;
        }

        public Sentence build() {
            return this.sentence;
        }
    }
}
