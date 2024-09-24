
package test;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class Utils {
    public static void hasMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getMethod(methodName, parameters);
            assertNotNull(method);
        } catch (NoSuchMethodException e) {
            fail("Method '" + methodName + "' with parameter signature '" + Arrays.toString(parameters) + "' not found.");
        }
    }
}
