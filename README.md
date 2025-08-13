# JChunk

[![GitHub Actions Status](https://img.shields.io/github/actions/workflow/status/jchunk-io/jchunk/build.yml?branch=main&logo=GitHub&style=for-the-badge)](https://github.com/arconia-io/arconia/actions/workflows/commit-stage.yml)
[![Apache 2.0 License](https://img.shields.io/github/license/arconia-io/arconia?style=for-the-badge&logo=apache&color=brightgreen)](https://www.apache.org/licenses/LICENSE-2.0)

## A Java Library for Text Chunking

JChunk project is simple library that enables different types of text splitting strategies.
This project begun thanks to Greg Kamradt's post [text splitting ideas](https://github.com/FullStackRetrieval-com/RetrievalTutorials/blob/main/tutorials/LevelsOfTextSplitting/5_Levels_Of_Text_Splitting.ipynb)

## ⚠️ WARNING - EARLY PHASE ⚠️

For now there is only [Pablo Sanchidrian](https://github.com/PabloSanchi) developing this project (in free time) so it might take a while to get to a first stable version.

Feel free to contribute!!


## Docs

[Click here to see documentation](docs/modules/ROOT/pages/index.adoc)

## Building

To build with running unit tests

```sh
./mvnw clean verify
```

To reformat using the java-format plugin

```sh
./mvnw spotless:apply
```

To update the year on license headers using the license-maven-plugin

```sh
./mvnw license:update-file-header -Plicense
```

To check javadocs using the javadoc:javadoc

```sh
./mvnw javadoc:javadoc -Pjavadoc
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.
