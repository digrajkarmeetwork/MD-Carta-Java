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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
        TreeMap<EmployeeAwardKey, String> employeeNames = events.stream()
                .collect(Collectors.toMap(
                        VestingEvent::key,
                        VestingEvent::employeeName,
                        (existing, replacement) -> replacement,
                        TreeMap::new
                ));

        // Filter to events on or before the target date.
        List<VestingEvent> applicableEvents = events.stream()
                .filter(e -> !e.date().isAfter(targetDate))
                .toList();

        // Validate cancellations against vested totals.
        validator.validate(applicableEvents);

        // Accumulate totals per employee-award key using parallel streams.
        // Each key's events are reduced independently, enabling safe parallelism.
        ConcurrentMap<EmployeeAwardKey, BigDecimal> accumulated = applicableEvents.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        VestingEvent::key,
                        Collectors.reducing(BigDecimal.ZERO, e -> signedQuantity(e), BigDecimal::add)
                ));

        // Merge into a sorted map, initializing all keys to zero first.
        TreeMap<EmployeeAwardKey, BigDecimal> totals = new TreeMap<>();
        employeeNames.keySet().forEach(key -> totals.put(key, BigDecimal.ZERO));
        accumulated.forEach((key, value) -> totals.merge(key, value, BigDecimal::add));

        // Build output summaries with truncated values.
        return totals.entrySet().stream()
                .map(entry -> {
                    EmployeeAwardKey key = entry.getKey();
                    BigDecimal total = precisionHandler.truncate(entry.getValue());
                    return new VestingSummary(
                            key.employeeId(),
                            employeeNames.get(key),
                            key.awardId(),
                            total
                    );
                })
                .toList();
    }

    private static BigDecimal signedQuantity(VestingEvent event) {
        if (event instanceof VestEvent) {
            return event.quantity();
        } else if (event instanceof CancelEvent) {
            return event.quantity().negate();
        }
        throw new IllegalStateException("Unknown event type: " + event.getClass());
    }
}
