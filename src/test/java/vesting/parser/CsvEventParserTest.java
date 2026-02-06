package vesting.parser;

import org.junit.jupiter.api.Test;
import vesting.model.VestingEvent;
import vesting.precision.PrecisionHandler;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvEventParserTest {

    private final CsvEventParser parser = new CsvEventParser(new CsvLineParser());
    private final PrecisionHandler precision0 = new PrecisionHandler(0);

    private String testResourcePath(String filename) {
        return Path.of("src", "test", "resources", filename).toString();
    }

    @Test
    void parse_example1() {
        List<VestingEvent> events = parser.parse(testResourcePath("example1.csv"), precision0);
        assertEquals(8, events.size());
    }

    @Test
    void parse_example2() {
        List<VestingEvent> events = parser.parse(testResourcePath("example2.csv"), precision0);
        assertEquals(2, events.size());
    }

    @Test
    void parse_example3() {
        PrecisionHandler precision1 = new PrecisionHandler(1);
        List<VestingEvent> events = parser.parse(testResourcePath("example3.csv"), precision1);
        assertEquals(3, events.size());
    }

    @Test
    void parse_fileNotFound_throws() {
        UncheckedIOException ex = assertThrows(UncheckedIOException.class, () ->
                parser.parse("nonexistent.csv", precision0));
        assertTrue(ex.getMessage().contains("nonexistent.csv"));
    }
}
