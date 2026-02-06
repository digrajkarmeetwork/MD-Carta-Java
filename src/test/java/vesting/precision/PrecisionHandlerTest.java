package vesting.precision;

import org.junit.jupiter.api.Test;

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
}
