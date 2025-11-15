# Contributing to JChunk

We welcome contributions to JChunk! This guide will help you get started.

## Getting Started

1. Fork the repository
2. Clone your fork

```bash
git clone https://github.com/your-username/jchunk.git
cd jchunk
```

3. Build the project

```bash
./mvnw clean install
```

## Code Style

We use Spotless for code formatting:

```bash
./mvnw spotless:apply
```

## Testing

All contributions must include tests. Run the test suite with:

```bash
./mvnw clean verify
```

## Pull Request Process

1. Create a feature branch
2. Make your changes
3. Add tests for new functionality
4. Ensure all tests pass
5. Update documentation if needed
6. Submit a pull request

## Documentation

When adding new features, please update the documentation:

- Update relevant Markdown files in `docosaurus/docs/`
- Add examples and usage instructions
- Update the sidebar if needed (`docosaurus/sidebars.ts`)

## Code of Conduct

Please read our [Code of Conduct](code-of-conduct.md) before contributing.

## Questions?

If you have questions, please open an issue on GitHub.
