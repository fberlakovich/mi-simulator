package engine;

import engine.commands.Command;
import engine.commands.Halt;
import engine.commands.Opcode;
import engine.commands.OpcodeDecodeException;
import engine.events.ExecutionEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryAccessErrorEvent;

/**
 * Executes MI programs.
 *
 * Obtained via Machine.createRunner(). Manages execution state (next command)
 * while the machine holds hardware state (registers, memory, flags) and
 * program state (loaded program).
 */
public class ProgramRunner extends Thread implements ExecutionController {

    private final Machine machine;
    private final BreakpointManager breakpointManager;

    /**
     * Next command to execute (cached decode of instruction at PC)
     */
    private Command next;

    /**
     * Last executed command (for step() callers)
     */
    private Command lastExecuted;

    /**
     * Flag to signal execution stop
     */
    private volatile boolean stopRequested = false;

    /**
     * Creates a new program runner for the given machine.
     * Package-private: use Machine.createRunner() to obtain instances.
     *
     * @param machine the machine to run on
     */
    ProgramRunner(Machine machine) {
        this.machine = machine;
        this.breakpointManager = machine.getBreakpointManager();
    }

    // ========================================================================
    // Command decoding (moved from ExecutionContext)
    // ========================================================================

    /**
     * Gets the command at a specific address from the loaded program.
     * Delegates to Machine which holds the program state.
     *
     * @param address the memory address
     * @return the Command or null
     */
    public Command getCommandAtAddress(int address) {
        return machine.getCommandAtAddress(address);
    }

    /**
     * Decodes the next command from memory at the current PC.
     *
     * @return the decoded Command
     * @throws OpcodeDecodeException if the opcode is unknown
     */
    private Command decodeNextCommand() {
        int pc = machine.getPC();
        int opcodeValue = machine.fetchByte();

        Opcode opcode = Opcode.fromCode(opcodeValue);
        if (opcode == null) {
            throw new OpcodeDecodeException("Unknown opcode", pc, opcodeValue);
        }

        return opcode.decodeCommand(machine, pc);
    }

    /**
     * Reads and decodes the next command from memory, validates against program.
     *
     * @return the next Command
     */
    public Command readNextCommand() {
        int startPC = machine.getPC();
        Command programCommand = getCommandAtAddress(startPC);

        if (programCommand != null) {
            // Use the original parsed command - this preserves parser semantics
            // (e.g., 2-operand FINDS/FINDC which decode() can't handle)
            byte[] encoded = programCommand.encode();
            machine.addToPC(encoded.length);
            next = programCommand;
            return next;
        }

        // No parsed command at this address - decode from memory
        // (handles self-modifying code or dynamic execution)
        next = decodeNextCommand();
        return next;
    }

    // ========================================================================
    // Execution control
    // ========================================================================

    private void handleMemoryError(MemoryAccessErrorEvent event) {
        stopProgram();
    }

    @Override
    public void run() {
        MachineEventListener memoryErrorListener = e -> handleMemoryError((MemoryAccessErrorEvent) e);
        machine.getEventBus().subscribe(MemoryAccessErrorEvent.class, memoryErrorListener);

        try {
            machine.getEventBus().publish(new ExecutionEvent(ExecutionEvent.Type.STARTED));

            boolean hitBreakpoint = false;
            // Initialize next command if not already set
            if (next == null) {
                next = readNextCommand();
            }

            while (next != null && !stopRequested && !hitBreakpoint) {
                Command executed = next;
                int currentAddress = executed.getAdress();
                executed.run();

                if (executed instanceof Halt) {
                    machine.getEventBus().publish(new ExecutionEvent(
                            ExecutionEvent.Type.STEP_COMPLETED, currentAddress, -1, executed));
                    break;
                }

                next = readNextCommand();
                int nextAddress = next != null ? next.getAdress() : -1;
                machine.getEventBus().publish(new ExecutionEvent(
                        ExecutionEvent.Type.STEP_COMPLETED, currentAddress, nextAddress, executed));

                if (next != null) {
                    hitBreakpoint = breakpointManager.hasBreakpoint(next.getAdress());
                }
            }

            int currentAddress = next != null ? next.getAdress() : -1;

            if (hitBreakpoint) {
                machine.getEventBus().publish(new ExecutionEvent(
                        ExecutionEvent.Type.BREAKPOINT_HIT, currentAddress, currentAddress));
            } else if (next == null || next instanceof Halt) {
                machine.getEventBus().publish(new ExecutionEvent(
                        ExecutionEvent.Type.PROGRAM_ENDED, currentAddress, -1));
            } else {
                machine.getEventBus().publish(new ExecutionEvent(
                        ExecutionEvent.Type.STOPPED, currentAddress, currentAddress));
            }
        } finally {
            machine.getEventBus().unsubscribe(MemoryAccessErrorEvent.class, memoryErrorListener);
            machine.clearActiveRunner();
        }
    }

    /**
     * Executes a single step (one instruction).
     *
     * @return true if more instructions are available, false if program ended
     */
    public boolean step() {
        // If no command is queued, read the first one
        if (next == null) {
            next = readNextCommand();
        }
        if (next == null) {
            machine.getEventBus().publish(new ExecutionEvent(
                    ExecutionEvent.Type.PROGRAM_ENDED, -1, -1));
            return false;
        }

        Command executed = next;
        lastExecuted = executed;
        executed.run();

        int currentAddress = executed.getAdress();
        boolean programEnded = executed instanceof Halt;

        if (!programEnded) {
            next = readNextCommand();
        }

        int nextAddress = (next != null && !programEnded) ? next.getAdress() : -1;

        machine.getEventBus().publish(new ExecutionEvent(
                ExecutionEvent.Type.STEP_COMPLETED, currentAddress, nextAddress, executed));

        if (programEnded) {
            machine.getEventBus().publish(new ExecutionEvent(
                    ExecutionEvent.Type.PROGRAM_ENDED, currentAddress, -1, executed));
        }

        return !programEnded && next != null;
    }

    @Override
    public void startProgram() {
        start();
    }

    @Override
    public void stopProgram() {
        stopRequested = true;
    }

    @Override
    public boolean isRunning() {
        return isAlive() && !stopRequested;
    }

    /**
     * Returns the next command that would be executed.
     *
     * @return the next command, or null if program ended
     */
    public Command getNextCommand() {
        return next;
    }

    /**
     * Returns the last command that was executed by step().
     *
     * @return the last executed command, or null if step() hasn't been called
     */
    public Command getLastExecuted() {
        return lastExecuted;
    }

    /**
     * Gets the machine this runner operates on.
     *
     * @return the machine
     */
    public Machine getMachine() {
        return machine;
    }
}
