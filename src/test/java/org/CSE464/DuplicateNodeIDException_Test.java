package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DuplicateNodeIDException_Test {
    @Test
    public void Exception_Has_Default_Message() {
        DuplicateNodeIDException exception = assertThrows(DuplicateNodeIDException.class, () -> {
            throw new DuplicateNodeIDException();
        });

        assertNotNull(exception.getMessage());
        assertTrue(!exception.getMessage().isEmpty());
    }

}
