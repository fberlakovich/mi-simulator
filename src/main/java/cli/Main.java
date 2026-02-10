package cli;

import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryAccessErrorEvent;
import engine.MachineContext;
import engine.ProgramRunner;
import engine.Machine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static engine.Version.VERSION;

/**
 * CLI entry point for the MI simulator.
 */
public class Main {

    private static void usage(int code) {
        System.err.println("MI Simulator CLI v" + VERSION);
        System.err.println();
        System.err.println("Usage:");
        System.err.println("  cli -help              Show this help message");
        System.err.println("  cli -version           Show version information");
        System.err.println("  cli <program> [options]");
        System.err.println();
        System.err.println("Options:");
        System.err.println("  -state <file>          Load initial state from file");
        System.err.println("  -hex                   Use hexadecimal output format");
        System.err.println("  -quiet                 Only show final register state");
        System.err.println();
        System.err.println("Examples:");
        System.err.println("  cli program.mi");
        System.err.println("  cli program.mi -hex");
        System.err.println("  cli program.mi -state init.state -quiet");
        System.exit(code);
    }

    public static void main(String[] args) {
        CommandLineArgs parsedArgs = parseArgs(args);
        if (parsedArgs == null) {
            return;
        }

        // Get machine context (the interface for frontend interaction)
        MachineContext machine = Machine.getInstance();

        // Set up error handler via event bus
        CliErrorHandler errorHandler = new CliErrorHandler();
        machine.getEventBus().subscribe(MemoryAccessErrorEvent.class, errorHandler);

        try {
            // Load program
            String programText = readFile(parsedArgs.programFile);
            if (programText == null) {
                System.exit(1);
            }

            // Assemble and load
            if (!MachineUtils.assembleAndLoad(machine, programText)) {
                System.exit(1);
            }

            // Load state file if provided
            if (parsedArgs.stateFile != null) {
                String stateText = readFile(parsedArgs.stateFile);
                if (stateText == null) {
                    System.exit(1);
                }
                try {
                    MachineUtils.loadState(machine, stateText, parsedArgs.useHex);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error in state file: " + e.getMessage());
                    System.exit(1);
                }
            }

            // Run the program
            runProgram(machine, parsedArgs.useHex, parsedArgs.quiet);

            // Report any errors that occurred during execution
            if (errorHandler.hasErrors()) {
                System.err.println();
                System.err.println("Execution completed with " + errorHandler.getErrorCount() + " error(s)");
            }

            System.exit(errorHandler.hasErrors() ? 1 : 0);
        } finally {
            machine.getEventBus().unsubscribe(MemoryAccessErrorEvent.class, errorHandler);
        }
    }

    private static CommandLineArgs parseArgs(String[] args) {
        if (args.length < 1) {
            usage(1);
            return null;
        }

        if (args[0].equals("-help") || args[0].equals("--help")) {
            usage(0);
            return null;
        }

        if (args[0].equals("-version") || args[0].equals("--version")) {
            System.out.println("MI Simulator v" + VERSION);
            System.exit(0);
            return null;
        }

        CommandLineArgs result = new CommandLineArgs();
        result.programFile = args[0];

        // Check program file exists
        if (!new File(result.programFile).exists()) {
            System.err.println("Error: Program file not found: " + result.programFile);
            System.exit(1);
            return null;
        }

        // Parse remaining arguments
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-hex":
                case "--hex":
                    result.useHex = true;
                    break;
                case "-quiet":
                case "--quiet":
                    result.quiet = true;
                    break;
                case "-state":
                case "--state":
                    if (i + 1 >= args.length) {
                        System.err.println("Error: -state requires a file argument");
                        System.exit(1);
                        return null;
                    }
                    result.stateFile = args[++i];
                    if (!new File(result.stateFile).exists()) {
                        System.err.println("Error: State file not found: " + result.stateFile);
                        System.exit(1);
                        return null;
                    }
                    break;
                default:
                    // Legacy support: bare argument after program is state file
                    if (!arg.startsWith("-") && result.stateFile == null) {
                        result.stateFile = arg;
                        if (!new File(result.stateFile).exists()) {
                            System.err.println("Error: State file not found: " + result.stateFile);
                            System.exit(1);
                            return null;
                        }
                    } else {
                        System.err.println("Error: Unknown option: " + arg);
                        usage(1);
                        return null;
                    }
            }
        }

        return result;
    }

    private static String readFile(String path) {
        try {
            return Files.readString(new File(path).toPath());
        } catch (IOException e) {
            System.err.println("Error reading file '" + path + "': " + e.getMessage());
            return null;
        }
    }

    private static void runProgram(MachineContext machine, boolean useHex, boolean quiet) {
        PrintStream out = System.out;
        ProgramRunner runner = machine.createRunner();

        if (quiet) {
            QuietMachine quietMachine = new QuietMachine(machine, runner, out, useHex);
            while (!quietMachine.hasHalted()) {
                quietMachine.executeNext();
            }
            quietMachine.printRegisterState();
        } else {
            PrintingMachine printingMachine = new PrintingMachine(machine, runner, out, useHex);
            while (!printingMachine.hasHalted()) {
                printingMachine.executeNext();
            }
        }
    }

    private static class CommandLineArgs {
        String programFile;
        String stateFile;
        boolean useHex;
        boolean quiet;
    }

    /**
     * Event listener for memory access errors during CLI execution.
     */
    private static class CliErrorHandler implements MachineEventListener {
        private final List<String> errors = new ArrayList<>();

        @Override
        public void onEvent(MachineEvent event) {
            MemoryAccessErrorEvent mae = (MemoryAccessErrorEvent) event;
            String errorMsg = String.format("Memory error at address 0x%X: %s",
                    mae.getAddress(), mae.getType());
            errors.add(errorMsg);
            System.err.println(errorMsg);
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        int getErrorCount() {
            return errors.size();
        }
    }
}
