package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class InvalidIDException_Test {
    @Test
    public void Exception_Has_Default_Message() {
        InvalidIDException exception = assertThrows(InvalidIDException.class, () -> {
            throw new InvalidIDException();
        });

        assertNotNull(exception.getMessage());
        assertTrue(!exception.getMessage().isEmpty());
    }

}