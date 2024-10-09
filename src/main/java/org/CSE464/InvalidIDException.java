package org.CSE464;

public class InvalidIDException extends RuntimeException {
    public InvalidIDException() {
        super("Invalid ID.");
    }

    public InvalidIDException(String message) {
        super(message);
    }
}