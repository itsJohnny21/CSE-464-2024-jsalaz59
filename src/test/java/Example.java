import org.CSE464.Main;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class Example {

    @Test
    public void hasMain() {
        try {
            Method mainMethod = Main.class.getMethod("main", String[].class);

            assertTrue(Modifier.isPublic(mainMethod.getModifiers()), "Main method should be public.");
            assertTrue(Modifier.isStatic(mainMethod.getModifiers()), "Main method should be static");
            assertEquals(void.class, mainMethod.getReturnType(), "Main method should return void");
        } catch (NoSuchMethodException e) {
            fail("Main method not found.");
        }
    }
}
