---
sidebar_position: 2
---

# Getting Started

## Prerequisites

- Java 21+
- Maven 3.6+

## Installation

Add the JChunk module you need to your `pom.xml` (example shows the fixed chunker):

```xml
<dependency>
  <groupId>io.jchunk</groupId>
  <artifactId>jchunk-fixed</artifactId>
  <version>${jchunk.version}</version>
</dependency>
```

## Basic Usage

```java
FixedChunker chunker = new FixedChunker();

String text = "Your long text here...";
List<Chunk> chunks = chunker.split(text);

for(Chunk chunk : chunks) {
    System.out.println("ID: " + chunk.id());
    System.out.println("CONTENT: " + chunk.content());
}
```

## Available Modules

- [jchunk-fixed](chunkers/fixed-chunker.md) - Fixed character chunking
- [jchunk-recursive-character](chunkers/recursive-chunker.md) - Recursive character chunking
- [jchunk-semantic](chunkers/semantic-chunker.md) - Semantic chunking with embeddings
