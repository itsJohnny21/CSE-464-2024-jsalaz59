package org.CSE464;

public class ParseDOTFileException extends RuntimeException {
    public ParseDOTFileException() {
        super("Unable to parse graph.");
    }

    public ParseDOTFileException(String message) {
        super(message);
    }
}