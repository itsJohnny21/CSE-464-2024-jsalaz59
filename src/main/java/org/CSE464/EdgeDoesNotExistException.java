package org.CSE464;

public class EdgeDoesNotExistException extends RuntimeException {
    public EdgeDoesNotExistException() {
        super("Edge does not exist.");
    }

    public EdgeDoesNotExistException(String message) {
        super(message);
    }
}