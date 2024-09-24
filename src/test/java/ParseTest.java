package test;

import org.CSE464.MyGraph;
import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test
    public void hasParseGraph() {
        Utils.hasMethod(MyGraph.class, "parseGraph", String.class);
    }
}