package cli;

import engine.Machine;
import engine.MachineContext;
import engine.ProgramRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility to generate expected output files for integration tests.
 * Run this after creating a new .mi test program to generate its .expected file.
 */
public class GenerateExpectedOutput {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: GenerateExpectedOutput <testname>");
            System.err.println("Example: GenerateExpectedOutput arithmetic");
            System.exit(1);
        }

        String testName = args[0];
        ClassLoader classLoader = GenerateExpectedOutput.class.getClassLoader();
        Path programsDir = Paths.get(classLoader.getResource("programs").toURI());

        Path programFile = programsDir.resolve(testName + ".mi");
        // Write to src/test/resources instead of build/resources
        Path srcTestResources = Paths.get("src/test/resources/programs");
        Path expectedFile = srcTestResources.resolve(testName + ".expected");

        if (!Files.exists(programFile)) {
            System.err.println("Program file not found: " + programFile);
            System.exit(1);
        }

        String programText = Files.readString(programFile);

        Machine.resetInstance();
        MachineContext context = Machine.getInstance();
        MachineUtils.assembleAndLoad(context, programText);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ProgramRunner runner = context.createRunner();
        PrintingMachine machine = new PrintingMachine(context, runner, new PrintStream(result), false);

        while (!machine.hasHalted()) {
            machine.executeNext();
        }

        String output = result.toString();
        Files.writeString(expectedFile, output);

        System.out.println("Generated: " + expectedFile);
        System.out.println("Content:");
        System.out.println(output);
    }
}