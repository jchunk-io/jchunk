---
sidebar_position: 1
---

# JChunk Documentation

A Java Library for Text Chunking.

JChunk is a lightweight and flexible library designed to provide multiple strategies for text chunking within Java applications.

## Quick Start

Add the dependency for the chunker you need to your `pom.xml` (example shows the fixed chunker):

```xml
<dependency>
  <groupId>io.jchunk</groupId>
  <artifactId>jchunk-fixed</artifactId>
  <version>${jchunk.version}</version>
</dependency>
```

```java
// Basic usage
FixedChunker chunker = new FixedChunker();
List<Chunk> chunks = chunker.split("Your text here");
```

## Available Chunkers

- [Fixed Character Chunker](chunkers/fixed-chunker.md)
- [Recursive Character Chunker](chunkers/recursive-chunker.md)
- [Semantic Chunker](chunkers/semantic-chunker.md)

## Contributing

We welcome contributions! See the [Contributing Guide](contributing.md) for details.
