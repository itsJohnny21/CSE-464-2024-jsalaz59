package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class Playground_Test {

    @Test
    public void Can_Run_Main() {
        Playground p = new Playground();

        assertDoesNotThrow(() -> {
            p.main(null);
        });
    }

}
