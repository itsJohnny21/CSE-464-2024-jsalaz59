package org;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static void hasMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getMethod(methodName, parameters);
            assertNotNull(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method '" + methodName + "' with parameter signature '"
                    + Arrays.toString(parameters) + "' not found.");
        }
    }

    public static String getFileNameWithoutExtension(Path fileName) {
        int dotIndex = fileName.toString().lastIndexOf('.');
        return (dotIndex == -1) ? fileName.toString() : fileName.toString().substring(0, dotIndex);
    }

    public static void verifyDOTFile(String dotFilepath, String outputFilepath)
            throws IOException, InterruptedException {
        if (!Files.exists(Paths.get(dotFilepath))) {
            throw new RuntimeException("DOT file does not exist: " + dotFilepath);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("dot", dotFilepath);
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);

        if (!finished) {
            process.destroy();
            throw new RuntimeException("Graphviz command timed out.");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("Process failed with exit code: " + exitCode);
        }
    }

    public static void createDirectory(Path path, boolean failIfExists) throws IOException {
        if (failIfExists && Files.exists(path)) {
            fail("The directory already exists: " + path.toString());
        }
        Files.createDirectory(path);
    }

    public static void removeDirectory(Path filepath, boolean force) throws IOException {
        if (force) {
            if (Files.exists(filepath)) {
                Files.walkFileTree(filepath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } else {
            Files.deleteIfExists(filepath);
        }
    }

    public static String getDOTFilepathFromTestDirectory(Path testDirectory) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(testDirectory)) {
            for (Path p : stream) {
                if (p.getFileName().toString().endsWith(".dot")) {
                    return p.toString();
                }
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }

        throw new Exception("DOT file not found.");
    }
}