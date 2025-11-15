# Recursive Character Chunker

## Overview

The Recursive Character Chunker provides more intelligent text splitting by using a hierarchy of delimiters. It attempts to split on the most meaningful delimiters first, falling back to less meaningful ones if needed.

## Installation

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

## Configuration

```java
// using default config
RecursiveCharacterChunker chunker = new RecursiveCharacterChunker();

// with custom config
Config config = Config.builder()
    .chunkSize(10)
    .chunkOverlap(0)
    .delimiters(List.of(";"))
    .trimWhitespace(true)
    .keepDelimiter(Delimiter.START)
    .build();

RecursiveCharacterChunker chunker = new RecursiveCharacterChunker(config);
```

### Configuration Options

- `chunkSize`: Maximum number of characters per chunk. Chunks may exceed this limit if the text cannot be split further. 
  - Default: `100`.
- `chunkOverlap`: Number of characters to overlap between consecutive chunks. Helps preserve context across boundaries. 
  - Default: `20`.
- `delimiters`: Ordered list of regex strings used for splitting. The chunker tries them in sequence; if none match, it falls back to the last (`""` = character-level). 
  - Default: `["\n\n", "\n", " ", ""]`.
- `keepDelimiter`: Whether to keep delimiters in chunks: `NONE`, `START`, `END`. 
  - Default: `START`.
- `trimWhitespace`: Whether to trim leading/trailing whitespace in each chunk. 
  - Default: `true`.

### Default Delimiters

The default separator hierarchy is:

1. `\n\n` (double newlines)
2. `\n` (single newlines)
3. `\s` (single space)
4. `` (empty string)

## Example

```java
Config config = Config.builder().chunkSize(15).chunkOverlap(0).build();
RecursiveCharacterChunker chunker = new RecursiveCharacterChunker(config);
List<Chunk> chunks = chunker.split("split this text\n\nI need it\n to be done as soon as possible");

// Result: ["split this text", "I need it", "to be done as", "soon as", "possible"]
```

## Custom Separators

```java
List<String> customSeparators = List.of("-", ".");
Config config = Config.builder()
    .chunkSize(5)
    .chunkOverlap(0)
    .separators(List.of("-", "."))
    .build();

RecursiveCharacterChunker chunker = new RecursiveCharacterChunker(config);
List<Chunk> chunks = chunker.split("");

// Result: ["give", "- me a", "_ hand"]
```

## Pros and Cons

### Pros
- Easy to implement and understand
- More advanced than fixed chunking
- Predictable chunk sizes
- Fast processing

### Cons
- Doesn't consider context
- May produce larger chunks than specified
- Overlap creates duplicate data
