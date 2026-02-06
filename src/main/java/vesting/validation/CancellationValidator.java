package vesting.validation;

import vesting.model.CancelEvent;
import vesting.model.EmployeeAwardKey;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CancellationValidator implements EventValidator {

    @Override
    public void validate(List<VestingEvent> events) {
        Map<EmployeeAwardKey, List<VestingEvent>> grouped = events.stream()
                .collect(Collectors.groupingBy(VestingEvent::key));

        for (Map.Entry<EmployeeAwardKey, List<VestingEvent>> entry : grouped.entrySet()) {
            validateAward(entry.getKey(), entry.getValue());
        }
    }

    private void validateAward(EmployeeAwardKey key, List<VestingEvent> events) {
        List<VestingEvent> sorted = events.stream()
                .sorted(Comparator.comparing(VestingEvent::date))
                .toList();

        Map<LocalDate, List<VestingEvent>> byDate = sorted.stream()
                .collect(Collectors.groupingBy(
                        VestingEvent::date,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        BigDecimal cumulativeVested = BigDecimal.ZERO;
        BigDecimal cumulativeCancelled = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, List<VestingEvent>> dateEntry : byDate.entrySet()) {
            LocalDate date = dateEntry.getKey();
            BigDecimal dayVests = BigDecimal.ZERO;
            BigDecimal dayCancels = BigDecimal.ZERO;

            for (VestingEvent event : dateEntry.getValue()) {
                if (event instanceof VestEvent) {
                    dayVests = dayVests.add(event.quantity());
                } else if (event instanceof CancelEvent) {
                    dayCancels = dayCancels.add(event.quantity());
                }
            }

            cumulativeVested = cumulativeVested.add(dayVests);
            cumulativeCancelled = cumulativeCancelled.add(dayCancels);

            if (cumulativeCancelled.compareTo(cumulativeVested) > 0) {
                throw new IllegalStateException(String.format(
                        "Invalid cancellation for %s/%s on %s: "
                                + "cumulative cancelled (%s) exceeds cumulative vested (%s)",
                        key.employeeId(), key.awardId(), date,
                        cumulativeCancelled.toPlainString(),
                        cumulativeVested.toPlainString()
                ));
            }
        }
    }
}
