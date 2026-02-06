package vesting.calculator;

import vesting.model.CancelEvent;
import vesting.model.EmployeeAwardKey;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;
import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;
import vesting.validation.EventValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CumulativeVestingCalculator implements VestingCalculator {

    private final EventValidator validator;

    public CumulativeVestingCalculator(EventValidator validator) {
        this.validator = validator;
    }

    @Override
    public List<VestingSummary> calculate(
            List<VestingEvent> events,
            LocalDate targetDate,
            PrecisionHandler precisionHandler) {

        // Register ALL employee-award combinations including future events.
        // TreeMap gives natural ordering by EmployeeAwardKey (Employee ID, then Award ID).
        TreeMap<EmployeeAwardKey, String> employeeNames = new TreeMap<>();
        for (VestingEvent event : events) {
            employeeNames.put(event.key(), event.employeeName());
        }

        // Filter to events on or before the target date.
        List<VestingEvent> applicableEvents = events.stream()
                .filter(e -> !e.date().isAfter(targetDate))
                .toList();

        // Validate cancellations against vested totals.
        validator.validate(applicableEvents);

        // Accumulate totals per employee-award key.
        Map<EmployeeAwardKey, BigDecimal> totals = new TreeMap<>();
        for (EmployeeAwardKey key : employeeNames.keySet()) {
            totals.put(key, BigDecimal.ZERO);
        }

        for (VestingEvent event : applicableEvents) {
            BigDecimal current = totals.get(event.key());
            BigDecimal updated;
            if (event instanceof VestEvent) {
                updated = current.add(event.quantity());
            } else if (event instanceof CancelEvent) {
                updated = current.subtract(event.quantity());
            } else {
                throw new IllegalStateException("Unknown event type: " + event.getClass());
            }
            totals.put(event.key(), updated);
        }

        // Build output summaries with truncated values.
        List<VestingSummary> summaries = new ArrayList<>();
        for (Map.Entry<EmployeeAwardKey, BigDecimal> entry : totals.entrySet()) {
            EmployeeAwardKey key = entry.getKey();
            BigDecimal total = precisionHandler.truncate(entry.getValue());
            summaries.add(new VestingSummary(
                    key.employeeId(),
                    employeeNames.get(key),
                    key.awardId(),
                    total
            ));
        }

        return summaries;
    }
}
