# FixedChunker

Splits text into **fixed-size chunks** using a single delimiter.

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
    <artifactId>jchunk-fixed</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation group: 'io.jchunk', name: 'jchunk-fixed', version: "${JCHUNK_VERSION}"
```

## Usage

```java
import io.jchunk.fixed.Config;
import io.jchunk.fixed.FixedChunker;
import io.jchunk.core.chunk.Chunk;
import io.jcunk.commons.Delimiter;

var config = Config.builder()
    .chunkSize(1000)               // max characters per chunk
    .chunkOverlap(100)             // overlapping characters between chunks
    .delimiter("\\.")              // split on dots (this is regex based)
    .keepDelimiter(Delimiter.NONE) // NONE / START / END
    .trimWhitespace(true)
    .build();

var chunker = new FixedChunker(config); // or new FixedChunker() using default config
List<Chunk> chunks = chunker.split("Your long text here...");
```
## Notes

- Chunk size is a target, not guaranteed if input cannot be split further.
- Overlap keeps context between chunks.
- Delimiter is regex based.