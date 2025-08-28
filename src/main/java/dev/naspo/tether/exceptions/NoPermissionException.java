package dev.naspo.tether.exceptions;

/**
 * When the player does not have the needed permissions to perform an action.
 */
public class NoPermissionException extends Exception {
    public NoPermissionException() {
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
