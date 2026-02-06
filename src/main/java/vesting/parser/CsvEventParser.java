package vesting.parser;

import vesting.model.VestingEvent;
import vesting.precision.PrecisionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvEventParser implements EventParser {

    private final CsvLineParser lineParser;

    public CsvEventParser(CsvLineParser lineParser) {
        this.lineParser = lineParser;
    }

    @Override
    public List<VestingEvent> parse(String filename, PrecisionHandler precisionHandler) {
        Path path = Path.of(filename);
        List<VestingEvent> events = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                events.add(lineParser.parseLine(trimmed, lineNumber, precisionHandler));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + filename, e);
        }

        return events;
    }
}
