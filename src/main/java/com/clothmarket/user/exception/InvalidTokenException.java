package com.clothmarket.user.exception;

/**
 * Exception thrown when a JWT or refresh token is invalid, expired, malformed, or revoked.
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with the specified detail message.
     *
     * @param message descriptive failure message
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
