package org.CSE464;

public class ParseException extends RuntimeException {
    public ParseException() {
        super("Unable to parse graph.");
    }

    public ParseException(String message) {
        super(message);
    }
}