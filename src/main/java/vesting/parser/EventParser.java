package vesting.parser;

import vesting.model.VestingEvent;
import vesting.precision.PrecisionHandler;

import java.util.List;

public interface EventParser {

    List<VestingEvent> parse(String filename, PrecisionHandler precisionHandler);
}
