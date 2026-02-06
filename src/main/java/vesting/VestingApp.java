package vesting;

import vesting.calculator.CumulativeVestingCalculator;
import vesting.calculator.VestingCalculator;
import vesting.cli.CommandLineArgs;
import vesting.formatter.CsvOutputFormatter;
import vesting.formatter.OutputFormatter;
import vesting.model.VestingEvent;
import vesting.model.VestingSummary;
import vesting.parser.CsvEventParser;
import vesting.parser.CsvLineParser;
import vesting.parser.EventParser;
import vesting.precision.PrecisionHandler;
import vesting.validation.CancellationValidator;
import vesting.validation.EventValidator;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

public final class VestingApp {

    private final EventParser parser;
    private final VestingCalculator calculator;
    private final OutputFormatter formatter;

    public VestingApp(EventParser parser, VestingCalculator calculator, OutputFormatter formatter) {
        this.parser = parser;
        this.calculator = calculator;
        this.formatter = formatter;
    }

    public void run(String filename, LocalDate targetDate, int precision) {
        PrecisionHandler precisionHandler = new PrecisionHandler(precision);

        List<VestingEvent> events = parser.parse(filename, precisionHandler);
        List<VestingSummary> summaries = calculator.calculate(events, targetDate, precisionHandler);
        formatter.format(summaries, precisionHandler);
    }

    public static void main(String[] args) {
        try {
            CommandLineArgs cliArgs = CommandLineArgs.parse(args);

            EventParser parser = new CsvEventParser(new CsvLineParser());
            EventValidator validator = new CancellationValidator();
            VestingCalculator calculator = new CumulativeVestingCalculator(validator);
            OutputFormatter formatter = new CsvOutputFormatter();

            VestingApp app = new VestingApp(parser, calculator, formatter);
            app.run(cliArgs.filename(), cliArgs.targetDate(), cliArgs.precision());

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IllegalStateException e) {
            System.err.println("Validation error: " + e.getMessage());
            System.exit(1);
        } catch (UncheckedIOException e) {
            System.err.println("File error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(1);
        }
    }
}
