package org.CSE464;

public class PathDoesNotExistException extends RuntimeException {
    public PathDoesNotExistException() {
        super("Path does not exist.");
    }

    public PathDoesNotExistException(String message) {
        super(message);
    }
}