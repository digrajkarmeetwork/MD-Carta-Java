package vesting.formatter;

import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;

import java.util.List;

public interface OutputFormatter {

    void format(List<VestingSummary> summaries, PrecisionHandler precisionHandler);
}
