package vesting.precision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrecisionHandlerTest {

    @Test
    void truncate_precision0_truncatesDecimal() {
        PrecisionHandler handler = new PrecisionHandler(0);
        assertEquals(new BigDecimal("100"), handler.truncate(new BigDecimal("100.5")));
        assertEquals(new BigDecimal("100"), handler.truncate(new BigDecimal("100.4567")));
        assertEquals(new BigDecimal("100"), handler.truncate(new BigDecimal("100.9999")));
    }

    @Test
    void truncate_precision1_truncatesToOneDecimal() {
        PrecisionHandler handler = new PrecisionHandler(1);
        assertEquals(new BigDecimal("700.7"), handler.truncate(new BigDecimal("700.75")));
        assertEquals(new BigDecimal("1000.5"), handler.truncate(new BigDecimal("1000.5")));
        assertEquals(new BigDecimal("100.4"), handler.truncate(new BigDecimal("100.4567")));
    }

    @Test
    void truncate_precision2_truncatesToTwoDecimals() {
        PrecisionHandler handler = new PrecisionHandler(2);
        assertEquals(new BigDecimal("100.45"), handler.truncate(new BigDecimal("100.4567")));
        assertEquals(new BigDecimal("100.50"), handler.truncate(new BigDecimal("100.5")));
    }

    @Test
    void truncate_precision6_retainsMaxPrecision() {
        PrecisionHandler handler = new PrecisionHandler(6);
        assertEquals(new BigDecimal("100.456789"), handler.truncate(new BigDecimal("100.4567891")));
    }

    @Test
    void truncate_wholeNumber_noChange() {
        PrecisionHandler handler = new PrecisionHandler(0);
        assertEquals(new BigDecimal("234"), handler.truncate(new BigDecimal("234")));
    }

    @Test
    void format_precision0_noDecimalPoint() {
        PrecisionHandler handler = new PrecisionHandler(0);
        assertEquals("1000", handler.format(new BigDecimal("1000")));
        assertEquals("100", handler.format(new BigDecimal("100.5")));
        assertEquals("0", handler.format(new BigDecimal("0")));
    }

    @Test
    void format_precision1_trailingZero() {
        PrecisionHandler handler = new PrecisionHandler(1);
        assertEquals("234.0", handler.format(new BigDecimal("234")));
        assertEquals("299.8", handler.format(new BigDecimal("299.8")));
        assertEquals("0.0", handler.format(new BigDecimal("0")));
    }

    @Test
    void format_precision2_trailingZeros() {
        PrecisionHandler handler = new PrecisionHandler(2);
        assertEquals("234.00", handler.format(new BigDecimal("234")));
        assertEquals("100.45", handler.format(new BigDecimal("100.4567")));
    }

    @Test
    void constructor_invalidPrecision_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PrecisionHandler(-1));
        assertThrows(IllegalArgumentException.class, () -> new PrecisionHandler(7));
    }

    @Test
    void constructor_boundaryValues_valid() {
        new PrecisionHandler(0);
        new PrecisionHandler(6);
    }

    @ParameterizedTest(name = "precision {0}: truncate({1}) = {2}")
    @CsvSource({
            "0, 123.456789, 123",
            "1, 123.456789, 123.4",
            "2, 123.456789, 123.45",
            "3, 123.456789, 123.456",
            "4, 123.456789, 123.4567",
            "5, 123.456789, 123.45678",
            "6, 123.456789, 123.456789",
    })
    void truncate_allPrecisionLevels(int precision, String input, String expected) {
        PrecisionHandler handler = new PrecisionHandler(precision);
        assertEquals(new BigDecimal(expected), handler.truncate(new BigDecimal(input)));
    }

    @ParameterizedTest(name = "precision {0}: format({1}) = {2}")
    @CsvSource({
            "0, 500.999, 500",
            "1, 500.999, 500.9",
            "2, 500.999, 500.99",
            "3, 500.999, 500.999",
            "4, 500.999, 500.9990",
            "5, 500.999, 500.99900",
            "6, 500.999, 500.999000",
    })
    void format_allPrecisionLevels(int precision, String input, String expected) {
        PrecisionHandler handler = new PrecisionHandler(precision);
        assertEquals(expected, handler.format(new BigDecimal(input)));
    }

    @ParameterizedTest(name = "precision {0}: whole number 1000 formatted correctly")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void format_wholeNumber_allPrecisionLevels(int precision) {
        PrecisionHandler handler = new PrecisionHandler(precision);
        String result = handler.format(new BigDecimal("1000"));
        if (precision == 0) {
            assertEquals("1000", result);
        } else {
            assertEquals("1000." + "0".repeat(precision), result);
        }
    }
}
