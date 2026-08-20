This project is unmaintained, due to time JChunk is officially ended.

# JChunk

[![GitHub Actions Status](https://img.shields.io/github/actions/workflow/status/jchunk-io/jchunk/build.yml?branch=main&logo=GitHub&style=for-the-badge)](.)
[![Apache 2.0 License](https://img.shields.io/github/license/jchunk-io/jchunk?style=for-the-badge&logo=apache&color=brightgreen)](.)

## A Java Library for Text Chunking

JChunk project is simple library that enables different types of text splitting strategies, essential for RAG applications.

## Docs

[Jchunk Website](https://jchunk-io.github.io/jchunk/)

## Installing

### Fixed Chunker 

```xml
<dependency>
    <groupId>io.jchunk</groupId>
    <artifactId>jchunk-fixed</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation("io.jchunk:jchunk-fixed:${JCHUNK_VERSION}")
```

### Recursive Chunker

```xml
<dependency>
    <groupId>io.jchunk</groupId>
    <artifactId>jchunk-recursive-character</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation("io.jchunk:jchunk-recursive-character:${JCHUNK_VERSION}")
```

### Semantic Chunker

```xml
<dependency>
    <groupId>io.jchunk</groupId>
    <artifactId>jchunk-semantic</artifactId>
    <version>${jchunk.version}</version>
</dependency>
```

```groovy
implementation("io.jchunk:jchunk-semantic:${JCHUNK_VERSION}")
```

## Building

To build with tests

```sh
./mvnw clean verify -Dgpg.skip=true
```

To reformat using the java-format plugin

```sh
./mvnw spotless:apply
```

To check javadocs using the javadoc:javadoc

```sh
./mvnw javadoc:javadoc -Pjavadoc
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.
