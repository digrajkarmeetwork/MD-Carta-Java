package vesting.parser;

import org.junit.jupiter.api.Test;
import vesting.model.CancelEvent;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;
import vesting.precision.PrecisionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvLineParserTest {

    private final CsvLineParser parser = new CsvLineParser();
    private final PrecisionHandler precision0 = new PrecisionHandler(0);
    private final PrecisionHandler precision1 = new PrecisionHandler(1);

    @Test
    void parseLine_validVestEvent() {
        VestingEvent event = parser.parseLine(
                "VEST,E001,Alice Smith,ISO-001,2020-01-01,1000", 1, precision0);

        assertInstanceOf(VestEvent.class, event);
        assertEquals("E001", event.employeeId());
        assertEquals("Alice Smith", event.employeeName());
        assertEquals("ISO-001", event.awardId());
        assertEquals(LocalDate.of(2020, 1, 1), event.date());
        assertEquals(new BigDecimal("1000"), event.quantity());
    }

    @Test
    void parseLine_validCancelEvent() {
        VestingEvent event = parser.parseLine(
                "CANCEL,E001,Alice Smith,ISO-001,2021-02-01,700", 1, precision0);

        assertInstanceOf(CancelEvent.class, event);
        assertEquals("E001", event.employeeId());
        assertEquals(new BigDecimal("700"), event.quantity());
    }

    @Test
    void parseLine_truncatesQuantityByPrecision() {
        VestingEvent event = parser.parseLine(
                "VEST,E001,Alice Smith,ISO-001,2020-01-01,1000.5", 1, precision0);
        assertEquals(new BigDecimal("1000"), event.quantity());

        VestingEvent event2 = parser.parseLine(
                "VEST,E001,Alice Smith,ISO-001,2020-01-01,700.75", 1, precision1);
        assertEquals(new BigDecimal("700.7"), event2.quantity());
    }

    @Test
    void parseLine_wrongFieldCount_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parser.parseLine("VEST,E001,Alice Smith,ISO-001,2020-01-01", 5, precision0));
        assertEquals("Line 5: expected 6 fields, got 5", ex.getMessage());
    }

    @Test
    void parseLine_tooManyFields_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parseLine("VEST,E001,Alice Smith,ISO-001,2020-01-01,1000,extra", 1, precision0));
    }

    @Test
    void parseLine_unknownEventType_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parser.parseLine("TRANSFER,E001,Alice Smith,ISO-001,2020-01-01,1000", 3, precision0));
        assertTrue(ex.getMessage().contains("Line 3"));
    }

    @Test
    void parseLine_invalidDateFormat_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parseLine("VEST,E001,Alice Smith,ISO-001,01-01-2020,1000", 1, precision0));
    }

    @Test
    void parseLine_invalidQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parseLine("VEST,E001,Alice Smith,ISO-001,2020-01-01,abc", 1, precision0));
    }

    @Test
    void parseLine_trimsWhitespace() {
        VestingEvent event = parser.parseLine(
                " VEST , E001 , Alice Smith , ISO-001 , 2020-01-01 , 1000 ", 1, precision0);

        assertEquals("E001", event.employeeId());
        assertEquals("Alice Smith", event.employeeName());
        assertEquals("ISO-001", event.awardId());
    }

    private static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
