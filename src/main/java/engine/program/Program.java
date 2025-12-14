package engine.program;

import engine.exceptions.NoCommandException;
import engine.Machine;
import engine.commands.Command;
import engine.state.MyByte;
import engine.parser.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an assembled program with commands and labels.
 */
public class Program {

    /**
     * The machine this program belongs to
     */
    private final Machine machine;

    /**
     * Whether this program has been successfully compiled and loaded
     */
    private boolean compiled;

    /**
     * List of commands in the program
     */
    private ArrayList<Command> program = new ArrayList<Command>();

    /**
     * List of defined labels
     */
    private ArrayList<Label> definedLabels = new ArrayList<Label>();

    /**
     * List of labels in use
     */
    private ArrayList<LabelInUse> usedLabels = new ArrayList<LabelInUse>();

    /**
     * Creates a new program for the given machine.
     *
     * @param machine the machine this program will run on
     */
    public Program(Machine machine) {
        this.machine = machine;
        this.compiled = false;
    }

    /**
     * Fügt einen weiteren Befehl hinzu
     *
     * @param comm der neue Befehl
     */
    public void add(Command comm) {
        program.add(comm);
    }

    /**
     * Gibt den ersten Befehl in einer Programmzeile wieder
     *
     * @param line Programmzeile
     * @return erster Befehl in der Zeile oder null
     */
    public Command getCommandPerLine(int line) {
        for (Command com : program) {
            if (com.getLine() == line) {
                return com;
            }
        }

        return null;
    }

    /**
     * Adds a defined label.
     *
     * @param label the label to add
     * @param errors error collector for duplicate label errors
     */
    public void addLabel(Label label, ErrorMessage errors) {
        for (Label existing : definedLabels) {
            if (existing.getName().equals(label.getName())) {
                if (errors != null) {
                    errors.append("Label " + label.getName() + " was defined multiple times");
                }
                return;
            }
        }
        definedLabels.add(label);
    }

    /**
     * Adds a label in use.
     *
     * @param label the label usage to add
     */
    public void addLabelInUse(LabelInUse label) {
        usedLabels.add(label);
    }

    /**
     * Gets the list of defined labels.
     *
     * @return the defined labels
     */
    public ArrayList<Label> getLabels() {
        return definedLabels;
    }

    /**
     * Validates that all used labels are defined.
     *
     * @param errors error collector
     * @return true if all labels are valid
     */
    public boolean validateLabels(ErrorMessage errors) {
        for (LabelInUse labelUsed : usedLabels) {
            boolean found = false;
            for (Label defined : definedLabels) {
                if (defined.getName().equals(labelUsed.getName())) {
                    labelUsed.setLabel(defined);
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (errors != null) {
                    errors.append("Label not found: " + labelUsed.getName());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves label addresses after all commands have been added.
     */
    public void resolveLabels() {
        boolean done = false;
        while (!done) {
            for (LabelInUse labelUsed : usedLabels) {
                Label labelDefined = labelUsed.getLabel();
                if (labelDefined == null) {
                    continue;
                }
                int opcodeLengthOld = labelUsed.getOp().encode().length;
                int address = labelUsed.getAdress() + labelUsed.getOp().encode().length;

                // Defined label address doesn't match used label address
                if (labelDefined.getAdress() != labelUsed.getOp().getAdress()) {
                    // Set new address
                    labelUsed.getOp().setAdress(labelDefined.getAdress());

                    // Set addresses in command that uses the label
                    labelUsed.getCommand().setAdress(labelUsed.getCommand().getAdress());
                }

                // Shift following commands by offset
                updateAddresses(address, labelUsed.getOp().encode().length - opcodeLengthOld);
            }

            // Check if all labels are set correctly
            done = checkLabels();
        }

        // Update addresses for all commands
        updateAddresses(0, 0);

        // Emit event for label resolution
        Map<String, Integer> labelMap = new HashMap<>();
        for (Label label : definedLabels) {
            labelMap.put(label.getName(), label.getAdress());
        }
        machine.getEventBus().publish(
                new engine.events.AssemblyEvent(
                        engine.events.AssemblyEvent.Type.LABELS_RESOLVED,
                        "Labels resolved",
                        labelMap));
    }

    /**
     * Updates command addresses starting from a given position.
     */
    private void updateAddresses(int begin, int offset) {
        Command last = null;
        for (Command comm : program) {
            if (comm.getAdress() >= begin) {
                comm.setAdress(last == null ?
                        comm.getAdress() + offset :
                        last.getAdress() + last.encode().length);
                last = comm;
            }
        }
    }

    /**
     * Checks if all labels point to correct memory locations.
     */
    private boolean checkLabels() {
        for (Command comm : program) {
            if (comm.hasLabel()) {
                ArrayList<LabelInUse> labels = comm.getLabel();
                for (LabelInUse labelUsed : labels) {
                    String name = labelUsed.getName();
                    for (Label labelDefined : definedLabels) {
                        if (labelDefined.getName().equals(name)
                                && labelDefined.getAdress() != labelUsed.getOp().getAdress()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Encodes the entire program to bytes.
     *
     * @return the encoded bytes
     */
    public byte[] encode() {
        int totalSize = 0;
        for (Command command : program) {
            totalSize += command.encode().length;
        }

        byte[] result = new byte[totalSize];
        int offset = 0;
        for (Command command : program) {
            byte[] encoded = command.encode();
            System.arraycopy(encoded, 0, result, offset, encoded.length);
            offset += encoded.length;
        }
        return result;
    }

    /**
     * Gibt die Liste der Befehle zurück
     *
     * @return Liste der Befehle
     */
    public ArrayList<Command> getCommands() {

        return program;
    }

    /**
     * Compiles and loads this program into the machine's memory.
     * Sets the program on the machine and marks it as compiled.
     *
     * @return true if compilation succeeded (program has commands), false otherwise
     */
    public boolean compile() {
        if (program.isEmpty()) {
            compiled = false;
            return false;
        }
        machine.setProgram(this);
        machine.getMemory().setContent(0, MyByte.fromByteArray(encode()));
        compiled = true;
        return true;
    }

    /**
     * Returns whether this program has been successfully compiled and loaded.
     *
     * @return true if compiled
     */
    public boolean isCompiled() {
        return compiled;
    }

    /**
     * Gets the machine this program belongs to.
     *
     * @return the machine
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * Sets or removes a breakpoint on a specific source line.
     *
     * @param line  source line number
     * @param value true to set, false to remove
     * @throws NoCommandException if no command exists at this line
     */
    public void setBreakPoint(int line, boolean value) throws NoCommandException {
        Command cmd = getCommandPerLine(line);
        if (cmd == null) {
            throw new NoCommandException();
        }
        if (value) {
            machine.getBreakpointManager().setBreakpoint(cmd.getAdress());
        } else {
            machine.getBreakpointManager().removeBreakpoint(cmd.getAdress());
        }
    }

}
