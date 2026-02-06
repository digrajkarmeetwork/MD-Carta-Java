package vesting.model;

import java.math.BigDecimal;
import java.util.Objects;

public record VestingSummary(
        String employeeId,
        String employeeName,
        String awardId,
        BigDecimal totalSharesVested
) {

    public VestingSummary {
        Objects.requireNonNull(employeeId, "Employee ID must not be null");
        Objects.requireNonNull(employeeName, "Employee name must not be null");
        Objects.requireNonNull(awardId, "Award ID must not be null");
        Objects.requireNonNull(totalSharesVested, "Total shares vested must not be null");
    }
}
