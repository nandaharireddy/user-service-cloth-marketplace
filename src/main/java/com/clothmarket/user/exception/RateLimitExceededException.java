package com.clothmarket.user.exception;

/**
 * Exception thrown when a client exceeds the allowed request rate on sensitive endpoints.
 */
public class RateLimitExceededException extends RuntimeException {

    /**
     * Constructs a new RateLimitExceededException with the specified detail message.
     *
     * @param message descriptive rate limit exceeded message
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
