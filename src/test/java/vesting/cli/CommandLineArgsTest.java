package vesting.cli;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineArgsTest {

    @Test
    void parse_twoArgs_defaultPrecision() {
        CommandLineArgs args = CommandLineArgs.parse(new String[]{"file.csv", "2020-04-01"});
        assertEquals("file.csv", args.filename());
        assertEquals(LocalDate.of(2020, 4, 1), args.targetDate());
        assertEquals(0, args.precision());
    }

    @Test
    void parse_threeArgs() {
        CommandLineArgs args = CommandLineArgs.parse(new String[]{"file.csv", "2020-04-01", "2"});
        assertEquals("file.csv", args.filename());
        assertEquals(LocalDate.of(2020, 4, 1), args.targetDate());
        assertEquals(2, args.precision());
    }

    @Test
    void parse_tooFewArgs_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv"}));
        assertTrue(ex.getMessage().contains("Usage"));
    }

    @Test
    void parse_tooManyArgs_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "2", "extra"}));
    }

    @Test
    void parse_noArgs_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{}));
    }

    @Test
    void parse_invalidDate_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv", "not-a-date"}));
        assertTrue(ex.getMessage().contains("Invalid date"));
    }

    @Test
    void parse_precisionTooHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "7"}));
    }

    @Test
    void parse_precisionNegative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "-1"}));
    }

    @Test
    void parse_precisionNotInteger_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "abc"}));
    }

    @Test
    void parse_precisionBoundary0() {
        CommandLineArgs args = CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "0"});
        assertEquals(0, args.precision());
    }

    @Test
    void parse_precisionBoundary6() {
        CommandLineArgs args = CommandLineArgs.parse(new String[]{"file.csv", "2020-01-01", "6"});
        assertEquals(6, args.precision());
    }
}
