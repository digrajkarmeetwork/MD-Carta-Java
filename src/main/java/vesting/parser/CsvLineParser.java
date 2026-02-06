package vesting.parser;

import vesting.model.CancelEvent;
import vesting.model.EventType;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;
import vesting.precision.PrecisionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class CsvLineParser {

    private static final int EXPECTED_FIELD_COUNT = 6;

    public VestingEvent parseLine(String line, int lineNumber, PrecisionHandler precisionHandler) {
        String[] fields = line.split(",", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + ": expected " + EXPECTED_FIELD_COUNT
                            + " fields, got " + fields.length);
        }

        EventType type = parseEventType(fields[0].trim(), lineNumber);
        String employeeId = fields[1].trim();
        String employeeName = fields[2].trim();
        String awardId = fields[3].trim();
        LocalDate date = parseDate(fields[4].trim(), lineNumber);
        BigDecimal quantity = parseQuantity(fields[5].trim(), lineNumber, precisionHandler);

        return switch (type) {
            case VEST -> new VestEvent(employeeId, employeeName, awardId, date, quantity);
            case CANCEL -> new CancelEvent(employeeId, employeeName, awardId, date, quantity);
        };
    }

    private EventType parseEventType(String value, int lineNumber) {
        try {
            return EventType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    private LocalDate parseDate(String value, int lineNumber) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + ": invalid date format '" + value + "'", e);
        }
    }

    private BigDecimal parseQuantity(String value, int lineNumber, PrecisionHandler precisionHandler) {
        try {
            BigDecimal raw = new BigDecimal(value);
            return precisionHandler.truncate(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + ": invalid quantity '" + value + "'", e);
        }
    }
}
