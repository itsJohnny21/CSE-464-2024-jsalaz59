package org.CSE464;

public class NodeAlreadyExistsException extends RuntimeException {
    public NodeAlreadyExistsException() {
        super("Node already exists.");
    }

    public NodeAlreadyExistsException(String message) {
        super(message);
    }
}