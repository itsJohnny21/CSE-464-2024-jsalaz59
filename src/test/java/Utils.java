import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static void hasMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getMethod(methodName, parameters);
            assertNotNull(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Method '" + methodName + "' with parameter signature '" + Arrays.toString(parameters)
                            + "' not found.");
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

    public static void createDirectory(Path path) throws IOException {
        Files.createDirectory(path);
    }

    public static void removeDirectoryIfExists(Path filepath, boolean force) throws IOException {
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

    public static Set<String> parseDelimitedFileToSet(Path filepath) {
        Set<String> answer = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                answer.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }

    public static HashMap<String, String> parseDelimitedFileToHashMap(Path filepath) {
        HashMap<String, String> answer = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String delimiter = ", ";
                String[] parts = line.split(delimiter);
                String[] remainingParts = Arrays.copyOfRange(parts, 1, parts.length);

                String first = parts[0];
                String second = String.join(delimiter, remainingParts);
                answer.put(first, second);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
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

    public static void main(String[] args) {
        Path path = Path.of(
                "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/DOT/valid/someNodesZeroEdges/someNodesZeroEdges.edges.txt");
        // HashMap<String, String> nodeLabels = parseDelimitedFileToHashMap(path);
        // Set<String> nodesNames = parseDelimitedFileToSet(path);
        Set<String> edgeDirections = parseDelimitedFileToSet(path);
        // System.out.println(nodeLabels);
        // System.out.println(nodesNames);
        System.out.println(edgeDirections);
    }
}