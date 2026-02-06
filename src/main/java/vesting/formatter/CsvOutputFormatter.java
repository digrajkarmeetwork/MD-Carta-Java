package vesting.formatter;

import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;

import java.io.PrintStream;
import java.util.List;

public final class CsvOutputFormatter implements OutputFormatter {

    private final PrintStream output;

    public CsvOutputFormatter(PrintStream output) {
        this.output = output;
    }

    public CsvOutputFormatter() {
        this(System.out);
    }

    @Override
    public void format(List<VestingSummary> summaries, PrecisionHandler precisionHandler) {
        for (VestingSummary summary : summaries) {
            output.println(String.join(",",
                    summary.employeeId(),
                    summary.employeeName(),
                    summary.awardId(),
                    precisionHandler.format(summary.totalSharesVested())
            ));
        }
    }
}
