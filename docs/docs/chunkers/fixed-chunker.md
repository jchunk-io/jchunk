# Fixed Character Chunker

## Overview

The Fixed Character Chunker is a basic text processing technique where text is divided into fixed-size chunks of characters. While simple, it serves as an excellent starting point to understand text splitting fundamentals.

## Installation

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

## Configuration

```java
// using default config
FixedChunker chunker = new FixedChunker();

// with custom config
Config config = Config.builder()
    .chunkSize(10)
    .chunkOverlap(0)
    .delimiter(";")
    .trimWhitespace(true)
    .keepDelimiter(Delimiter.START)
    .build();

FixedChunker chunker = new FixedChunker(config);
```

### Configuration Options

- `chunkSize`: Maximum number of characters per chunk. Defines the target size of each piece. If a single segment is longer than this, it may exceed the limit. 
- Default: `1000`.
- `chunkOverlap`: Number of characters to overlap between consecutive chunks (preserves context). 
- Default: `100`.
- `delimiter`: Regex string used to split text before forming chunks. Common values: `" "` for spaces, `"\n"` for newlines, `""` for character-level. 
- Default: `space (" ")`.
- `trimWhitespace`: Whether to trim leading/trailing whitespace from each chunk. 
  - Default: `true`.
- `keepDelimiter`: How to keep delimiters in chunks: `NONE`, `START`, or `END`. 
  - Default: `NONE`.

## Examples

### Basic Chunking

Chunk size of 10 and no overlap (0):

```java
Config config = Config.builder()
    .chunkSize(10)
    .chunkOverlap(0)
    .build();
FixedChunker chunker = new FixedChunker(config);
String text = "This is an example of character splitting.";

List<Chunk> chunks = chunker.split(text);

// Result: ["This is an", "example of", "character", "splitting."]
```

### With Overlap

Adding 4 characters of overlap and a custom blank delimiter:

```java
Config config = Config.builder()
    .chunkSize(35)
    .chunkOverlap(4)
    .delimiter("")
    .build();
FixedChunker chunker = new FixedChunker(config);
String text = "This is the text I would like to chunk up. It is the example text for this exercise";
List<Chunk> chunks = chunker.split(text);

// Result: ["This is the text I would like to ch", "o chunk up. It is the example text", "ext for this exercise"]
```

## Pros and Cons

### Pros
- Easy to implement and understand
- Predictable chunk sizes
- Fast processing

### Cons
- Doesn't consider text structure or context
- May split words inappropriately
- Overlap creates duplicate data
