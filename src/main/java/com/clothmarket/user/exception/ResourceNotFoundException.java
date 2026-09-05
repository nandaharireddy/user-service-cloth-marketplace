package com.clothmarket.user.exception;

/**
 * Exception thrown when a requested entity or resource is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message descriptive failure message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
