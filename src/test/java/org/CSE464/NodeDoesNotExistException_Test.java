package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NodeDoesNotExistException_Test {
    @Test
    public void Exception_Has_Default_Message() {
        NodeDoesNotExistException exception = assertThrows(NodeDoesNotExistException.class, () -> {
            throw new NodeDoesNotExistException();
        });

        assertNotNull(exception.getMessage());
        assertTrue(!exception.getMessage().isEmpty());
    }

}