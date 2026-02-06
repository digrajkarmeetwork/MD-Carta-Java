package vesting.cli;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public record CommandLineArgs(String filename, LocalDate targetDate, int precision) {

    public CommandLineArgs {
        Objects.requireNonNull(filename, "Filename must not be null");
        Objects.requireNonNull(targetDate, "Target date must not be null");
        if (precision < 0 || precision > 6) {
            throw new IllegalArgumentException(
                    "Precision must be between 0 and 6 (inclusive), got: " + precision);
        }
    }

    public static CommandLineArgs parse(String[] args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException(
                    "Usage: vesting_program <filename> <target_date> [precision]");
        }

        String filename = args[0];
        LocalDate targetDate = parseDate(args[1]);
        int precision = args.length == 3 ? parsePrecision(args[2]) : 0;

        return new CommandLineArgs(filename, targetDate, precision);
    }

    private static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format: '" + dateStr + "'. Expected YYYY-MM-DD.", e);
        }
    }

    private static int parsePrecision(String precStr) {
        try {
            int precision = Integer.parseInt(precStr);
            if (precision < 0 || precision > 6) {
                throw new IllegalArgumentException(
                        "Precision must be between 0 and 6 (inclusive), got: " + precision);
            }
            return precision;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid precision value: '" + precStr + "'. Must be an integer 0-6.", e);
        }
    }
}
