package vesting.validation;

import vesting.model.VestingEvent;

import java.util.List;

public interface EventValidator {

    void validate(List<VestingEvent> events);
}
