package cli;

import engine.ProgramRunner;
import engine.commands.Command;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test wrapper that validates execution output against expected reference output.
 * Used for integration testing.
 */
class ValidatingMachine {

    private final ByteArrayOutputStream result = new ByteArrayOutputStream();
    private final PrintingMachine inner;
    private String[] referenceLines;
    private int referenceIndex = 0;

    ValidatingMachine(ProgramRunner runner, String referenceLines) {
        this.inner = new PrintingMachine(runner, new PrintStream(result), false);
        this.referenceLines = referenceLines.split(System.lineSeparator(), -1);
    }

    public boolean hasHalted() {
        return inner.hasHalted();
    }

    public Command executeNext() {
        result.reset();
        Command command = inner.executeNext();
        String[] outputLines = result.toString().split(System.lineSeparator(), -1);
        for (int i = 0; i < outputLines.length - 1; i++) {
            String outputLine = outputLines[i];
            if (!outputLine.equals(referenceLines[referenceIndex])) {
                throw new IllegalStateException("Invalid output on line " + referenceIndex + ".\n" +
                        "Expected: " + referenceLines[referenceIndex] + "\n" +
                        "Actual: " + outputLine);
            }
            referenceIndex++;
        }
        return command;
    }
}