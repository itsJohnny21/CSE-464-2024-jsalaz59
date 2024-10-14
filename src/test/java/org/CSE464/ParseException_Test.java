package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ParseException_Test {
    @Test
    public void Exception_Has_Default_Message() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            throw new ParseException();
        });

        assertNotNull(exception.getMessage());
        assertTrue(!exception.getMessage().isEmpty());
    }

}