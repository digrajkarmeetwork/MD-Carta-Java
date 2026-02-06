package vesting.validation;

import org.junit.jupiter.api.Test;
import vesting.model.CancelEvent;
import vesting.model.VestEvent;
import vesting.model.VestingEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancellationValidatorTest {

    private final CancellationValidator validator = new CancellationValidator();

    private VestEvent vest(String empId, String awardId, String date, String qty) {
        return new VestEvent(empId, "Name", awardId, LocalDate.parse(date), new BigDecimal(qty));
    }

    private CancelEvent cancel(String empId, String awardId, String date, String qty) {
        return new CancelEvent(empId, "Name", awardId, LocalDate.parse(date), new BigDecimal(qty));
    }

    @Test
    void validate_noEvents_noError() {
        assertDoesNotThrow(() -> validator.validate(List.of()));
    }

    @Test
    void validate_onlyVests_noError() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                vest("E001", "A1", "2020-02-01", "500")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_cancelLessThanVested_noError() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "500")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_cancelEqualsVested_noError() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "1000")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_cancelExceedsVested_throws() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "1500")
        );
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validate(events));
        assertTrue(ex.getMessage().contains("E001"));
        assertTrue(ex.getMessage().contains("A1"));
    }

    @Test
    void validate_sameDayVestAndCancel_valid() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-01-01", "1000")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_sameDayCancelExceedsVest_throws() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "100"),
                cancel("E001", "A1", "2020-01-01", "200")
        );
        assertThrows(IllegalStateException.class, () -> validator.validate(events));
    }

    @Test
    void validate_multipleAwards_validatedIndependently() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "500"),
                vest("E001", "A2", "2020-01-01", "200"),
                cancel("E001", "A2", "2020-02-01", "100")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_multipleCancelsOnSameDay_summed() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "400"),
                cancel("E001", "A1", "2020-02-01", "400")
        );
        assertDoesNotThrow(() -> validator.validate(events));
    }

    @Test
    void validate_multipleCancelsExceedVested_throws() {
        List<VestingEvent> events = List.of(
                vest("E001", "A1", "2020-01-01", "1000"),
                cancel("E001", "A1", "2020-02-01", "600"),
                cancel("E001", "A1", "2020-02-01", "600")
        );
        assertThrows(IllegalStateException.class, () -> validator.validate(events));
    }
}
