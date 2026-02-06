package vesting.model;

public enum EventType {
    VEST,
    CANCEL;

    public static EventType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event type must not be null or blank");
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + value);
        }
    }
}
