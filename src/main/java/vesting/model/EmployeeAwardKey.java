package vesting.model;

import java.util.Objects;

public record EmployeeAwardKey(String employeeId, String awardId) implements Comparable<EmployeeAwardKey> {

    public EmployeeAwardKey {
        Objects.requireNonNull(employeeId, "Employee ID must not be null");
        Objects.requireNonNull(awardId, "Award ID must not be null");
    }

    @Override
    public int compareTo(EmployeeAwardKey other) {
        int cmp = this.employeeId.compareTo(other.employeeId);
        return cmp != 0 ? cmp : this.awardId.compareTo(other.awardId);
    }
}
