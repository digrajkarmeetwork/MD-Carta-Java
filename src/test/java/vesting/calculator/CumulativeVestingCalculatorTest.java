package vesting.calculator;

import org.junit.jupiter.api.Test;
import vesting.model.CancelEvent;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;
import vesting.model.VestingSummary;
import vesting.precision.PrecisionHandler;
import vesting.validation.CancellationValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CumulativeVestingCalculatorTest {

    private final CumulativeVestingCalculator calculator =
            new CumulativeVestingCalculator(new CancellationValidator());

    private VestEvent vest(String empId, String name, String awardId, String date, String qty) {
        return new VestEvent(empId, name, awardId, LocalDate.parse(date), new BigDecimal(qty));
    }

    private CancelEvent cancel(String empId, String name, String awardId, String date, String qty) {
        return new CancelEvent(empId, name, awardId, LocalDate.parse(date), new BigDecimal(qty));
    }

    @Test
    void stage1_example1() {
        List<VestingEvent> events = List.of(
                vest("E001", "Alice Smith", "ISO-001", "2020-01-01", "1000"),
                vest("E001", "Alice Smith", "ISO-001", "2021-01-01", "1000"),
                vest("E001", "Alice Smith", "ISO-002", "2020-03-01", "300"),
                vest("E001", "Alice Smith", "ISO-002", "2020-04-01", "500"),
                vest("E002", "Bobby Jones", "NSO-001", "2020-01-02", "100"),
                vest("E002", "Bobby Jones", "NSO-001", "2020-02-02", "200"),
                vest("E002", "Bobby Jones", "NSO-001", "2020-03-02", "300"),
                vest("E003", "Cat Helms", "NSO-002", "2024-01-01", "100")
        );

        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2020, 4, 1), precision);

        assertEquals(4, result.size());

        assertSummary(result.get(0), "E001", "Alice Smith", "ISO-001", "1000");
        assertSummary(result.get(1), "E001", "Alice Smith", "ISO-002", "800");
        assertSummary(result.get(2), "E002", "Bobby Jones", "NSO-001", "600");
        assertSummary(result.get(3), "E003", "Cat Helms", "NSO-002", "0");
    }

    @Test
    void stage2_example2() {
        List<VestingEvent> events = List.of(
                vest("E001", "Alice Smith", "ISO-001", "2020-01-01", "1000"),
                cancel("E001", "Alice Smith", "ISO-001", "2021-02-01", "700")
        );

        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2021, 2, 1), precision);

        assertEquals(1, result.size());
        assertSummary(result.get(0), "E001", "Alice Smith", "ISO-001", "300");
    }

    @Test
    void stage3_example3() {
        List<VestingEvent> events = List.of(
                vest("E001", "Alice Smith", "ISO-001", "2020-01-01", "1000.5"),
                cancel("E001", "Alice Smith", "ISO-001", "2021-02-01", "700.7"),
                vest("E002", "Bobby Jones", "ISO-002", "2020-01-01", "234.0")
        );

        PrecisionHandler precision = new PrecisionHandler(1);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2021, 2, 1), precision);

        assertEquals(2, result.size());
        assertSummary(result.get(0), "E001", "Alice Smith", "ISO-001", "299.8");
        assertSummary(result.get(1), "E002", "Bobby Jones", "ISO-002", "234.0");
    }

    @Test
    void emptyEvents_emptyResult() {
        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(List.of(),
                LocalDate.of(2020, 1, 1), precision);
        assertEquals(0, result.size());
    }

    @Test
    void allEventsFuture_allZeroShares() {
        List<VestingEvent> events = List.of(
                vest("E001", "Alice Smith", "ISO-001", "2025-01-01", "1000")
        );

        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2020, 1, 1), precision);

        assertEquals(1, result.size());
        assertSummary(result.get(0), "E001", "Alice Smith", "ISO-001", "0");
    }

    @Test
    void targetDateInclusive() {
        List<VestingEvent> events = List.of(
                vest("E001", "Alice", "A1", "2020-06-15", "500")
        );

        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2020, 6, 15), precision);

        assertEquals(1, result.size());
        assertSummary(result.get(0), "E001", "Alice", "A1", "500");
    }

    @Test
    void sortedByEmployeeIdThenAwardId() {
        List<VestingEvent> events = List.of(
                vest("E002", "Bob", "B2", "2020-01-01", "100"),
                vest("E001", "Alice", "A2", "2020-01-01", "200"),
                vest("E001", "Alice", "A1", "2020-01-01", "300")
        );

        PrecisionHandler precision = new PrecisionHandler(0);
        List<VestingSummary> result = calculator.calculate(events,
                LocalDate.of(2020, 12, 31), precision);

        assertEquals(3, result.size());
        assertEquals("E001", result.get(0).employeeId());
        assertEquals("A1", result.get(0).awardId());
        assertEquals("E001", result.get(1).employeeId());
        assertEquals("A2", result.get(1).awardId());
        assertEquals("E002", result.get(2).employeeId());
        assertEquals("B2", result.get(2).awardId());
    }

    private void assertSummary(VestingSummary summary, String empId, String name,
                               String awardId, String totalShares) {
        assertEquals(empId, summary.employeeId());
        assertEquals(name, summary.employeeName());
        assertEquals(awardId, summary.awardId());
        assertEquals(0, new BigDecimal(totalShares).compareTo(summary.totalSharesVested()),
                "Expected " + totalShares + " but got " + summary.totalSharesVested());
    }
}
