# JChunk

[![GitHub Actions Status](https://img.shields.io/github/actions/workflow/status/jchunk-io/jchunk/build.yml?branch=main&logo=GitHub&style=for-the-badge)](.)
[![Apache 2.0 License](https://img.shields.io/github/license/arconia-io/arconia?style=for-the-badge&logo=apache&color=brightgreen)](.)

## A Java Library for Text Chunking

JChunk project is simple library that enables different types of text splitting strategies, essential for RAG applications.

## Docs

### Chunkers
 - [Fixed Chunker](jchunk-fixed/README.md)
 - [Recursive Character Chunker](jchunk-recursive-character/README.md)
 - [Semantic Chunker](jchunk-semantic/README.md)

### More
 - [Jchunk Documentation](docs/modules/ROOT/pages/index.adoc)

## Installing

### Maven

```xml
<dependency>
    <groupId>io.jchunk</groupId>
    <artifactId>jchunk-...</artifactId> <!-- replace dots with desired module name -->
    <version>${jchunk.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: 'io.jchunk', name: 'jchunk-...', version: "${JCHUNK_VERSION}" // replace dots with desired module name
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
