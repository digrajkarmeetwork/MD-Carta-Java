package vesting.calculator;

import vesting.model.VestingEvent;
import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;

import java.time.LocalDate;
import java.util.List;

public interface VestingCalculator {

    List<VestingSummary> calculate(
            List<VestingEvent> events,
            LocalDate targetDate,
            PrecisionHandler precisionHandler
    );
}
