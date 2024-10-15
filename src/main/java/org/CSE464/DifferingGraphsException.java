package org.CSE464;

public class DifferingGraphsException extends RuntimeException {
    public DifferingGraphsException() {
        super("Graphs differ.");
    }

    public DifferingGraphsException(String message) {
        super(message);
    }
}