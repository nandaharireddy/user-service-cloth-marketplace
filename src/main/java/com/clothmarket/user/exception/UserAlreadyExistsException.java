package com.clothmarket.user.exception;

/**
 * Exception thrown when attempting to register a user with an existing email or phone.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message descriptive conflict message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
