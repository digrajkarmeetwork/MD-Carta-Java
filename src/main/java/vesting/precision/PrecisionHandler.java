package vesting.precision;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PrecisionHandler {

    private final int precision;

    public PrecisionHandler(int precision) {
        if (precision < 0 || precision > 6) {
            throw new IllegalArgumentException(
                    "Precision must be between 0 and 6 (inclusive), got: " + precision);
        }
        this.precision = precision;
    }

    public BigDecimal truncate(BigDecimal value) {
        return value.setScale(precision, RoundingMode.FLOOR);
    }

    public String format(BigDecimal value) {
        BigDecimal truncated = truncate(value);
        if (precision == 0) {
            return truncated.toBigInteger().toString();
        }
        return truncated.toPlainString();
    }

    public int getPrecision() {
        return precision;
    }
}
