package vesting;

import org.junit.jupiter.api.Test;
import vesting.calculator.CumulativeVestingCalculator;
import vesting.calculator.VestingCalculator;
import vesting.formatter.CsvOutputFormatter;
import vesting.formatter.OutputFormatter;
import vesting.parser.CsvEventParser;
import vesting.parser.CsvLineParser;
import vesting.parser.EventParser;
import vesting.validation.CancellationValidator;
import vesting.validation.EventValidator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VestingAppIntegrationTest {

    private String runApp(String resourceFile, LocalDate targetDate, int precision) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        EventParser parser = new CsvEventParser(new CsvLineParser());
        EventValidator validator = new CancellationValidator();
        VestingCalculator calculator = new CumulativeVestingCalculator(validator);
        OutputFormatter formatter = new CsvOutputFormatter(ps);

        VestingApp app = new VestingApp(parser, calculator, formatter);
        String path = Path.of("src", "test", "resources", resourceFile).toString();
        app.run(path, targetDate, precision);

        return baos.toString().replace("\r\n", "\n");
    }

    @Test
    void stage1_example1_fullFlow() {
        String output = runApp("example1.csv", LocalDate.of(2020, 4, 1), 0);

        String expected = String.join("\n",
                "E001,Alice Smith,ISO-001,1000",
                "E001,Alice Smith,ISO-002,800",
                "E002,Bobby Jones,NSO-001,600",
                "E003,Cat Helms,NSO-002,0"
        ) + "\n";

        assertEquals(expected, output);
    }

    @Test
    void stage2_example2_fullFlow() {
        String output = runApp("example2.csv", LocalDate.of(2021, 2, 1), 0);

        String expected = "E001,Alice Smith,ISO-001,300\n";

        assertEquals(expected, output);
    }

    @Test
    void stage3_example3_fullFlow() {
        String output = runApp("example3.csv", LocalDate.of(2021, 2, 1), 1);

        String expected = String.join("\n",
                "E001,Alice Smith,ISO-001,299.8",
                "E002,Bobby Jones,ISO-002,234.0"
        ) + "\n";

        assertEquals(expected, output);
    }

    @Test
    void stage1_example1_defaultPrecision() {
        String output = runApp("example1.csv", LocalDate.of(2020, 4, 1), 0);

        // Verify no decimal points in output when precision is 0
        for (String line : output.trim().split("\n")) {
            String[] parts = line.split(",");
            String shares = parts[3];
            assertEquals(-1, shares.indexOf('.'), "No decimal point expected with precision 0: " + shares);
        }
    }

    @Test
    void stage3_example3_precision0_truncatesInput() {
        // When precision is 0, fractional inputs should be truncated
        String output = runApp("example3.csv", LocalDate.of(2021, 2, 1), 0);

        // 1000.5 truncated to 1000, 700.75 truncated to 700 => 1000 - 700 = 300
        // 234 stays 234
        String expected = String.join("\n",
                "E001,Alice Smith,ISO-001,300",
                "E002,Bobby Jones,ISO-002,234"
        ) + "\n";

        assertEquals(expected, output);
    }

    @Test
    void allEventsAfterTargetDate_allZero() {
        // example1.csv with a very early target date
        String output = runApp("example1.csv", LocalDate.of(2019, 01, 01), 0);

        String expected = String.join("\n",
                "E001,Alice Smith,ISO-001,0",
                "E001,Alice Smith,ISO-002,0",
                "E002,Bobby Jones,NSO-001,0",
                "E003,Cat Helms,NSO-002,0"
        ) + "\n";

        assertEquals(expected, output);
    }

    @Test
    void sameDayVestAndMultipleCancels() {
        // Vest 1000 on Jan 1, then vest 500 and cancel 200+100 on Jun 1
        // Total: 1000 + 500 - 200 - 100 = 1200
        String output = runApp("edge_same_day_cancel.csv", LocalDate.of(2020, 12, 31), 0);

        assertEquals("E001,Alice Smith,ISO-001,1200\n", output);
    }

    @Test
    void singleEvent() {
        String output = runApp("edge_single_event.csv", LocalDate.of(2020, 12, 31), 0);

        assertEquals("E001,Alice Smith,ISO-001,500\n", output);
    }

    @Test
    void emptyFile_noOutput() {
        String output = runApp("edge_empty.csv", LocalDate.of(2020, 12, 31), 0);

        assertTrue(output.isEmpty(), "Empty CSV should produce no output");
    }

    @Test
    void manyEmployeesAndAwards_withCancelsAndFutureEvents() {
        // E001: ISO-001=100, ISO-002=200-50=150, ISO-003=300
        // E002: NSO-001=400-400=0, NSO-002=500
        // E003: RSU-001=0 (future event, 2025)
        String output = runApp("edge_many_awards.csv", LocalDate.of(2020, 12, 31), 0);

        String expected = String.join("\n",
                "E001,Alice Smith,ISO-001,100",
                "E001,Alice Smith,ISO-002,150",
                "E001,Alice Smith,ISO-003,300",
                "E002,Bobby Jones,NSO-001,0",
                "E002,Bobby Jones,NSO-002,500",
                "E003,Cat Helms,RSU-001,0"
        ) + "\n";

        assertEquals(expected, output);
    }
}
