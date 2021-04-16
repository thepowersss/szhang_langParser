import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

// you should be able to replace all instances of Files.readString(path) with String.join("\n", Files.readAllLines(path))
// or update intelliJ's java

public class Main {

    static Parser parser = new Parser(); // FIXME change this line to use your code if necessary
    static Interpreter interpreter = new Interpreter(); // FIXME change this line to use your code if necessary

    public static class Finder extends SimpleFileVisitor<Path> {
        // from https://docs.oracle.com/javase/tutorial/essential/io/find.html

        private final PathMatcher matcher;

        Finder(String pattern) {
            this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        // invoke the pattern matching method on each file.
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
            Path name = path.getFileName();
            if (name != null && this.matcher.matches(name)) {
                testWithFile(path);
            }
            return CONTINUE;
        }

    }

    private static String fixNewlines(String string) {
        return String.join("\n", string.split("\\r?\\n"));
    }

    public static void testWithFile(Path langPath) {
        try {
            // read the program to run
            String program = fixNewlines(Files.readString(langPath));
            System.out.println();
            System.out.println("RUNNING " + langPath + ":");
            System.out.println(program);
            // parse the code
            Parse parse = Main.parser.parse(program, "sequence");
            String actualOutput = null;
            if (parse == null) {
                // if there's a syntax error, that's our only output
                actualOutput = "syntax error";
            } else {
                // otherwise, check against the intermediate representation, if it exists
                Path sexpPath = langPath.resolveSibling(langPath.getFileName().toString().replace(".lang", ".sexp"));
                if (Files.exists(sexpPath)) {
                    String expectedSExp = fixNewlines(Files.readString(sexpPath));
                    if (!parse.toString().equals(expectedSExp)) {
                        throw new AssertionError("intermediate representation does not match");
                    }
                }

                // run the program to get the output
                //actualOutput = Main.interpreter.execute(parse); FIXME UNCOMMENT WHEN USING INTERPRETER TEST CASES

            }
            return; // for using just the parser FIXME UNCOMMENT WHEN USING INTERPRETER TEST CASES
            /*
            // read the expected output
            Path outPath = langPath.resolveSibling(langPath.getFileName().toString().replace(".lang", ".out"));
            String expectedOutput = Files.readString(outPath);
            // process the expected and actual output to deal with newlines
            expectedOutput = fixNewlines(expectedOutput);
            actualOutput = fixNewlines(actualOutput);
            // check against the output
            if (!actualOutput.equals(expectedOutput)) {
                String message = "\n\n";
                message += "EXPECTED OUTPUT:\n";
                message += expectedOutput + "\n";
                message += "\n";
                message += "ACTUAL OUTPUT:\n";
                message += actualOutput + "\n";
                throw new AssertionError(message);
            }
            */ // FIXME UNCOMMENT WHEN USING INTERPRETER TEST CASES
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testWithDirectory(Path dirPath) {
        try {
            Files.walkFileTree(dirPath, new Finder("*.lang"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Finder finder = new Finder("*.lang");
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (Files.isDirectory(path)) {
                Main.testWithDirectory(path);
            } else {
                Main.testWithFile(path);
            }
        }
    }
}