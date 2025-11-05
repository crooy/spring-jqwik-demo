# Spring Boot jqwik Demo

A simple Spring Boot web server demonstrating property-based testing with jqwik.

## Features

- **GET /api/health**: Health check endpoint
- **POST /api/words**: Processes an array of strings and returns an array of strings

## Business Logic

The POST endpoint processes input strings:
- Searches for the words "aardappel" (9 letters) or "pieper" (6 letters)
- For each occurrence of these words, outputs the word length times the word "friet"
- Example: `["aardappel"]` ? `["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]`
- Example: `["pieper"]` ? `["friet", "friet", "friet", "friet", "friet", "friet"]`

## Development Setup

### Using Nix Flake (Recommended)

If you have Nix installed, you can use the flake to get a development environment with Java 21 and Maven:

```bash
nix develop
```

This will provide you with Java 21 and Maven in your shell.

### Manual Setup

Ensure you have:
- Java 21
- Maven 3.6+

## Running the Application

```bash
mvn spring-boot:run
```

The server will start on port 8080.

## Testing

The project includes comprehensive jqwik property-based tests:

- **WordProcessorServiceTest**: Tests the business logic with various random inputs
- **SnackbarControllerTest**: Tests the REST endpoints with MockMvc

Run tests with:

```bash
mvn test
```

## Property-Based Testing with jqwik

The tests use jqwik to generate random test data and verify properties hold for all inputs:

- Tests verify correct behavior with random combinations of words
- Ensures "aardappel" always produces 9 "friet" entries
- Ensures "pieper" always produces 6 "friet" entries
- Verifies empty results when no target words are present
- Tests multiple occurrences and combinations
