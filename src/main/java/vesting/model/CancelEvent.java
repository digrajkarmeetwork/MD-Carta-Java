package vesting.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record CancelEvent(
        String employeeId,
        String employeeName,
        String awardId,
        LocalDate date,
        BigDecimal quantity
) implements VestingEvent {

    public CancelEvent {
        Objects.requireNonNull(employeeId, "Employee ID must not be null");
        Objects.requireNonNull(employeeName, "Employee name must not be null");
        Objects.requireNonNull(awardId, "Award ID must not be null");
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("CANCEL quantity must be non-negative, got: " + quantity);
        }
    }
}
