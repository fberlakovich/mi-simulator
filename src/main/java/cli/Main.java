package cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import gui.CONSTANTS;

public class Main {
    public static void usage(int code) {
            System.err.println("usage:");
            System.err.println("        cli -help");
            System.err.println("        cli -version");
            System.err.println("        cli <path to MI program> [-hex] [-quiet]");
            System.err.println("        cli <path to MI program> [state file] [-hex] [-quiet]");
            System.exit(code);
    }

    public static void main(String[] args) {
        // The below code is a poor man's commandline parser.
        // It's not sophisticated, but recognizes the above input format.
        if (args.length < 1) {
            Main.usage(1);
        }
        if (args[0].equals("-help")) {
            Main.usage(0);
        }
        if (args[0].equals("-version")) {
            System.out.println(CONSTANTS.VERSION);
            System.exit(0);
        }

        // load the program text
        String programText = null;
        try {
            programText = new String(Files.readAllBytes(new File(args[0]).toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // load and assemble the program
        boolean loaded = MachineUtils.assembleAndLoad(programText);
        if (!loaded) {
            System.exit(1);
        }

        // load the state file if any
        // load the additional options, if any
        boolean useHex = false;
        boolean quiet = false;

        if (args.length > 1) {
            String stateText = null;
            if (args[1].equals("-hex")) {
                useHex = true;
            } else if (args[1].equals("-quiet")) {
                quiet = true;
            } else {  // the second argument is a state file
                try {
                    stateText = new String(Files.readAllBytes(new File(args[1]).toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                MachineUtils.loadState(stateText, useHex);
            }
        }

        for (int i = 2; i < 4; i++) {
            if (args.length > i) {
                if (args[i].equals("-hex"))
                    useHex = true;
                if (args[i].equals("-quiet"))
                    quiet = true;
            }
        }

        if (quiet) {
            runQuietMachine(useHex);
        } else {
            runPrintingMachine(useHex);
        }
        System.exit(0);
    }

    private static void runPrintingMachine(boolean useHex) {
        PrintingMachine machine = new PrintingMachine(new MIMachine(), System.out, useHex);
        while (!machine.hasHalted()) {
            machine.executeNext();
        }
    }

    private static void runQuietMachine(boolean useHex) {
        QuietMachine machine = new QuietMachine(new MIMachine(), System.out, useHex);
        while (!machine.hasHalted()) {
            machine.executeNext();
        }
        machine.printRegisterState();
    }
}
