package jchunk.chunker.semantic;

/**
 * Sentence class to represent a sentence during the splitting process
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Sentence {

    private int index;

    private String content;

    private String combined;

    private float[] embedding;

    /**
     * Constructs a Sentence
     *
     * @param index mandatory
     * @param content
     * @param combined
     * @param embedding
     *
     * @implNote by default, combined is set to content if combined is null
     */
    private Sentence(int index, String content, String combined, float[] embedding) {
        this.index = index;
        this.content = content;
        this.combined = combined != null ? combined : content;
        this.embedding = embedding;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer index;
        private String content;
        private String combined;
        private float[] embedding = new float[] {};

        public Builder index(final int index) {
            this.index = index;
            return this;
        }

        public Builder content(final String content) {
            this.content = content;
            return this;
        }

        public Builder combined(final String combined) {
            this.combined = combined;
            return this;
        }

        public Builder embedding(final float[] embedding) {
            this.embedding = embedding;
            return this;
        }

        public Sentence build() {
            assert index != null : "sentence must have an index";
            assert content != null : "sentence must have a content";

            return new Sentence(index, content, combined, embedding);
        }
    }
}
