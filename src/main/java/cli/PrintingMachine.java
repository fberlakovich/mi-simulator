package cli;

import engine.MachineContext;
import engine.ProgramRunner;
import engine.util.MemoryChangeTracker;
import engine.state.Register;
import static engine.MachineConstants.REGISTER_COUNT;
import engine.commands.Command;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Decorator that prints machine state changes after each instruction execution.
 * Works directly with ProgramRunner.
 */
class PrintingMachine {
    private final MachineContext machine;
    private final ProgramRunner runner;
    private final PrintStream out;
    private final boolean printHex;

    private final int[] previousRegValues = new int[REGISTER_COUNT];
    private final Map<Integer, Byte> previousMemValues = new HashMap<>();
    private final Map<String, Boolean> previousFlags = new HashMap<>();
    private final MemoryChangeTracker memoryTracker;
    private boolean initialized;
    private boolean halted;

    PrintingMachine(MachineContext machine, ProgramRunner runner, PrintStream out, boolean printHex) {
        this.machine = machine;
        this.runner = runner;
        this.out = out;
        this.printHex = printHex;
        this.memoryTracker = new MemoryChangeTracker(machine);
    }

    public boolean hasHalted() {
        return halted;
    }

    public Command executeNext() {
        if (halted) {
            return null;
        }

        if (!initialized) {
            Set<Integer> changedAddresses = memoryTracker.getChangedAddresses();
            for (Integer address : changedAddresses) {
                previousMemValues.put(address, machine.getMemory().readByte(address));
            }
            fillCurrentFlags(previousFlags);
            initialized = true;
        }

        boolean hasMore = runner.step();
        Command executed = runner.getLastExecuted();

        if (!hasMore) {
            halted = true;
        }

        out.println("INS: " + executed);
        printRegisterValues(previousRegValues);
        out.println();
        printFlags(previousFlags);
        out.println();
        printMemoryValues(previousMemValues);
        out.println();
        out.println();
        return executed;
    }

    private void fillCurrentFlags(Map<String, Boolean> flags) {
        flags.put("C", machine.getFlags().isCarry());
        flags.put("N", machine.getFlags().isNegative());
        flags.put("V", machine.getFlags().isOverflow());
        flags.put("Z", machine.getFlags().isZero());
    }

    static class Separator {
        private boolean first;
        private final PrintStream out;

        Separator(PrintStream out) {
            this.out = out;
            this.first = true;
        }

        void printColumn(String column) {
            if (!first) {
                out.print("; ");
            }
            out.print(column);
            first = false;
        }
    }

    private void printFlags(Map<String, Boolean> previousFlags) {
        Map<String, Boolean> flags = new HashMap<>();
        fillCurrentFlags(flags);
        Separator separator = new Separator(out);
        for (String flag : flags.keySet()) {
            if (!java.util.Objects.equals(flags.get(flag), previousFlags.get(flag))) {
                String format;
                if (!printHex) {
                    format = "%s: %d -> %d";
                } else {
                    format = "%s: %02X -> %02X";
                }
                separator.printColumn(String.format(format, flag, asBit(previousFlags.get(flag)), asBit(flags.get(flag))));
            } else {
                String format;
                if (!printHex) {
                    format = "%s: %d";
                } else {
                    format = "%s: %02X";
                }
                separator.printColumn(String.format(format, flag, asBit(flags.get(flag))));
            }
        }
        fillCurrentFlags(previousFlags);
    }


    private void printMemoryValues(Map<Integer, Byte> previousMemValues) {
        Set<Integer> changedAddresses = memoryTracker.getChangedAddresses();
        Separator separator = new Separator(out);

        // Sort addresses for consistent output
        java.util.List<Integer> sortedAddresses = new java.util.ArrayList<>(changedAddresses);
        java.util.Collections.sort(sortedAddresses);

        for (Integer address : sortedAddresses) {
            byte currentByte = machine.getMemory().readByte(address);
            int currentValue = currentByte & 0xFF;
            int previousValue = previousMemValues.containsKey(address) ? (previousMemValues.get(address) & 0xFF) : 0;

            if (previousValue == 0 && currentValue == 0)
                continue;

            previousMemValues.put(address, currentByte);
            if (previousValue != currentValue) {
                String format;
                if (!printHex) {
                    format = "%d: %d -> %d";
                } else {
                    format = "%02X: %02X -> %02X";
                }
                separator.printColumn(String.format(format, address, previousValue, currentValue));
            } else {
                String format;
                if (!printHex) {
                    format = "%d: %d";
                } else {
                    format = "%02X: %02X";
                }

                separator.printColumn(String.format(format, address, currentValue));
            }
        }
    }

    private void printRegisterValues(int[] previousRegValues) {
        Separator separator = new Separator(out);
        for (int i = 0; i < REGISTER_COUNT; i++) {
            Register register = machine.getRegisters().getRegister(i);
            int regValue = register.getContentAsNumber(4);
            if (previousRegValues[i] == regValue && regValue == 0)
                continue;
            if (previousRegValues[i] != regValue) {
                String format;
                if (!printHex) {
                    format = "R%s: %d -> %d";
                } else {
                    format = "R%s: %02X -> %02X";
                }
                separator.printColumn(String.format(format, i, previousRegValues[i], regValue));
            } else {
                String format;
                if (!printHex) {
                    format = "R%s: %d";
                } else {
                    format = "R%s: %02X";
                }
                separator.printColumn(String.format(format, i, regValue));
            }
            previousRegValues[i] = regValue;
        }
    }

    private static int asBit(boolean value) {
        return value ? 1 : 0;
    }

}