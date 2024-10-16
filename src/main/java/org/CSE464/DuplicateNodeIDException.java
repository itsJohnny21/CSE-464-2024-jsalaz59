package org.CSE464;

public class DuplicateNodeIDException extends RuntimeException {
    public DuplicateNodeIDException() {
        super("Duplicate node ID.");
    }

    public DuplicateNodeIDException(String message) {
        super(message);
    }
}