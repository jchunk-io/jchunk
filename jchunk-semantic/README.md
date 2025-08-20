# SemanticChunker

Splits text into chunks based on **semantic similarity** using embeddings.  
Instead of relying only on character counts or delimiters, it groups sentences into coherent chunks that better preserve meaning — useful for **RAG pipelines**, **semantic search**, and **embedding-based retrieval**.

## Installing

Considering there is a property defined for jchunk:
```xml
<properties>
    <jchunk.version>X.X.X</jchunk.version>
</properties>
```

Then:
```xml
<dependency>
    <groupId>io.jchunk</groupId>
    <artifactId>jchunk-semantic</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation group: 'io.jchunk', name: 'jchunk-semantic', version: "${JCHUNK_VERSION}"
```

## Usage

```java
import io.jchunk.semantic.SemanticChunker;
import io.jchunk.semantic.embedder.JChunkEmbedder;
import io.jchunk.core.chunk.Chunk;

var config = Config.builder()
        .sentenceSplittingStrategy(SentenceSplittingStrategy.DEFAULT) // regex being used to split into sentences, can be user defined
        .percentile(95)                                               // similarity threshold (1–99)
        .bufferSize(1)                                                // number of neighboring sentences to include
        .build();

var embedder = new JChunkEmbedder();   // default provided embedder
var chunker = new SemanticChunker(embedder);

List<Chunk> chunks = chunker.split("Your long text here...");
```

## Notes

- The number and size of chunks depend on the embedding model.
- Preserves semantic coherence rather than enforcing strict size. 
  - The model selected must support the language being use if not chunks might not have coherence
  - If the text is in English make sure the model supports English.
- Requires an Embedder implementation (default: `JChunkEmbedder`).