package org.CSE464;

public class NodeDoesNotExistException extends RuntimeException {
    public NodeDoesNotExistException() {
        super("Node does not exist.");
    }

    public NodeDoesNotExistException(String message) {
        super(message);
    }
}