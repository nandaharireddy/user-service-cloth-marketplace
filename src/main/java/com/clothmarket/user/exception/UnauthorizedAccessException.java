package com.clothmarket.user.exception;

/**
 * Exception thrown when a user attempts to access or modify a resource owned by another user.
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message.
     *
     * @param message descriptive security violation message
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
