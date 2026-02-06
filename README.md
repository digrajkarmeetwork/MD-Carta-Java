# Vesting Schedule Generator

A command-line program that generates cumulative vesting schedules from individual vesting events.

## Prerequisites

- Java 17 or later ([Download](https://adoptium.net/temurin/releases/))

Maven is **not** required. The included Maven Wrapper (`mvnw`) handles it automatically.

## Quick Start

```bash
./mvnw clean package
./vesting_program <filename> <target_date> [precision]
```

## Build

```bash
./mvnw clean package
```

This compiles the source, runs all tests, and produces an executable JAR. The Maven Wrapper automatically downloads Maven if not already present.

On Windows CMD, use `mvnw.cmd clean package` instead.

## Run

```bash
./vesting_program <filename> <target_date> [precision]
```

The script automatically builds the project on first run if needed.

On Windows CMD, use `vesting_program.bat` instead.

### Arguments

| Argument | Required | Description |
|---|---|---|
| `filename` | Yes | Path to CSV file containing vesting events |
| `target_date` | Yes | Calculate shares vested on or before this date (YYYY-MM-DD) |
| `precision` | No | Decimal digits for input/output (0-6, default: 0) |

### Examples

```bash
./vesting_program example1.csv 2020-04-01
./vesting_program example2.csv 2021-02-01
./vesting_program example3.csv 2021-02-01 1
```

## Run Tests

```bash
./mvnw test
```

## Design Decisions

### Architecture

The application follows a pipeline architecture with four stages: **parse** -> **validate** -> **calculate** -> **format**. Each stage is encapsulated behind an interface, enabling independent testing and future extension.

### Dependency Injection

All components receive their dependencies through constructor injection without any framework. `VestingApp` composes `EventParser`, `VestingCalculator`, and `OutputFormatter` via their interfaces, making unit testing straightforward with alternative implementations.

### Sealed Interface for Event Types

`VestingEvent` is a sealed interface with `VestEvent` and `CancelEvent` as permitted implementations. This provides:
- Compile-time exhaustiveness when switching on event types
- Easy extensibility: adding a new event type requires a new record and updating the sealed clause
- Immutability via Java records

### BigDecimal for Precision

All quantity arithmetic uses `BigDecimal` to avoid floating-point precision errors. The `PrecisionHandler` class applies truncation (`RoundingMode.FLOOR`) at two points:
1. **Input**: Quantities are truncated to the specified precision when parsing the CSV
2. **Output**: Final totals are truncated before formatting

### Sorted Output via TreeMap

`EmployeeAwardKey` implements `Comparable` with lexicographic ordering by Employee ID then Award ID. Using `TreeMap` with this key type guarantees output order without a separate sort step.

### Parallel Accumulation for Large Datasets

The calculation stage uses `parallelStream()` with `groupingByConcurrent` to accumulate vesting totals across employee-award keys. Each key's events are reduced independently, making the accumulation step thread-safe and scalable for large input files.

### Zero-Share Inclusion

All employee-award combinations from the input are registered before filtering by target date. This ensures employees with only future vesting events still appear in the output with 0 shares.

## Assumptions

- Employee IDs and Award IDs use consistent formatting (no mixed-case duplicates like "E001" vs "e001")
- Employee names are consistent across all rows for the same employee-award combination
- Input CSV lines use comma as the sole delimiter (no quoted fields containing commas)
- An empty or blank CSV file produces no output and exits successfully

## What I Would Change With More Time

- Add streaming/chunked parsing for very large files to reduce memory footprint
- Add more comprehensive error recovery (e.g., skip invalid lines with warnings instead of failing)
- Add a performance benchmark test with millions of events
- Consider adding a `--help` flag for usage documentation

## LLM Usage

Claude (Anthropic) was used as a coding assistant for boilerplate generation and iteration. Key prompts included:

1. "What's the best way to handle precision truncation with BigDecimal — FLOOR vs HALF_DOWN?" — used to confirm `RoundingMode.FLOOR` matches the spec's "rounded down" requirement
2. "Scaffold a Maven project structure with a sealed interface for event types" — generated initial file layout which I then restructured into the pipeline architecture
3. "Write boilerplate JUnit test cases for the CSV parser" — produced test stubs that I expanded with additional edge cases and assertions
4. "How can I make the accumulation step thread-safe for large datasets?" — explored options and chose `groupingByConcurrent` with `parallelStream`
5. "Does BigDecimal.FLOOR handle negative results correctly for cancellation subtraction?" — verified edge case behavior during manual testing

All architecture decisions (pipeline design, DI without framework, sealed interface, TreeMap for ordering) were made independently. Generated code was reviewed, tested, and refactored before inclusion.
