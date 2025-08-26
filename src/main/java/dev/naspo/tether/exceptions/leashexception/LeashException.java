package dev.naspo.tether.exceptions.leashexception;

/**
 * When a leashing operation cannot be completed.
 */
public class LeashException extends RuntimeException {
    private final LeashErrorType leashErrorType;

    public LeashException(LeashErrorType leashErrorType) {
        this.leashErrorType = leashErrorType;
    }

    public LeashException(LeashErrorType leashErrorType, String message) {
        super(message);
        this.leashErrorType = leashErrorType;
    }

    public LeashErrorType getType() {
        return leashErrorType;
    }
}