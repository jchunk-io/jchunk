# RecursiveCharacterChunker

Splits text **recursively by multiple delimiters**, starting with bigger ones (paragraphs) and falling back to smaller ones (sentences, words, characters).

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
    <artifactId>jchunk-recursive-character</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation group: 'io.jchunk', name: 'jchunk-recursive-character', version: "${JCHUNK_VERSION}"
```


## Usage

```java
import io.jchunk.recursive.Config;
import io.jchunk.recursive.RecursiveCharacterChunker;
import io.jchunk.core.chunk.Chunk;
import io.jchunk.commons..Delimiter;

var config = Config.builder()
    .chunkSize(500)
    .chunkOverlap(50)
    .delimiters(List.of("\n\n", "\n", " ", "")) // fallback to character-level, regex-string based
    .keepDelimiter(Delimiter.START)             // NONE / START / END
    .trimWhitespace(true)
    .build();

var chunker = new RecursiveCharacterChunker(config);
List<Chunk> chunks = chunker.split("Your long text here...");
```

## Notes

- Delimiters are applied in order (first match wins).
- Last delimiter "" means character-level splitting.
- Keeps context overlap between chunks.
