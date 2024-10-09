package org.CSE464;

public class EdgeAlreadyExistsException extends RuntimeException {
    public EdgeAlreadyExistsException() {
        super("Edge already exists.");
    }

    public EdgeAlreadyExistsException(String message) {
        super(message);
    }
}