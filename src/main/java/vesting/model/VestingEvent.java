package vesting.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public sealed interface VestingEvent permits VestEvent, CancelEvent {

    String employeeId();

    String employeeName();

    String awardId();

    LocalDate date();

    BigDecimal quantity();

    default EmployeeAwardKey key() {
        return new EmployeeAwardKey(employeeId(), awardId());
    }
}
