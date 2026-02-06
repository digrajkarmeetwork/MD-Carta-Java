package vesting.formatter;

import org.junit.jupiter.api.Test;
import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvOutputFormatterTest {

    private String captureOutput(List<VestingSummary> summaries, PrecisionHandler precision) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        CsvOutputFormatter formatter = new CsvOutputFormatter(ps);
        formatter.format(summaries, precision);
        return baos.toString().replace("\r\n", "\n");
    }

    @Test
    void format_singleLine_precision0() {
        List<VestingSummary> summaries = List.of(
                new VestingSummary("E001", "Alice Smith", "ISO-001", new BigDecimal("1000"))
        );
        String output = captureOutput(summaries, new PrecisionHandler(0));
        assertEquals("E001,Alice Smith,ISO-001,1000\n", output);
    }

    @Test
    void format_multipleLines() {
        List<VestingSummary> summaries = List.of(
                new VestingSummary("E001", "Alice Smith", "ISO-001", new BigDecimal("1000")),
                new VestingSummary("E002", "Bobby Jones", "NSO-001", new BigDecimal("600"))
        );
        String output = captureOutput(summaries, new PrecisionHandler(0));
        assertEquals("E001,Alice Smith,ISO-001,1000\nE002,Bobby Jones,NSO-001,600\n", output);
    }

    @Test
    void format_precision1_trailingZero() {
        List<VestingSummary> summaries = List.of(
                new VestingSummary("E001", "Alice Smith", "ISO-001", new BigDecimal("299.8")),
                new VestingSummary("E002", "Bobby Jones", "ISO-002", new BigDecimal("234"))
        );
        String output = captureOutput(summaries, new PrecisionHandler(1));
        assertEquals("E001,Alice Smith,ISO-001,299.8\nE002,Bobby Jones,ISO-002,234.0\n", output);
    }

    @Test
    void format_zeroShares_precision0() {
        List<VestingSummary> summaries = List.of(
                new VestingSummary("E003", "Cat Helms", "NSO-002", BigDecimal.ZERO)
        );
        String output = captureOutput(summaries, new PrecisionHandler(0));
        assertEquals("E003,Cat Helms,NSO-002,0\n", output);
    }

    @Test
    void format_zeroShares_precision1() {
        List<VestingSummary> summaries = List.of(
                new VestingSummary("E003", "Cat Helms", "NSO-002", BigDecimal.ZERO)
        );
        String output = captureOutput(summaries, new PrecisionHandler(1));
        assertEquals("E003,Cat Helms,NSO-002,0.0\n", output);
    }

    @Test
    void format_emptyList() {
        String output = captureOutput(List.of(), new PrecisionHandler(0));
        assertEquals("", output);
    }
}
