package engine;

import engine.program.Program;
import engine.commands.Command;
import engine.events.MachineEventBus;
import engine.state.Flags;
import engine.state.Memory;
import engine.state.Register;
import engine.state.RegisterBody;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;

/**
 * Represents the MI machine.
 *
 * This class encapsulates:
 * - Hardware state: memory, registers, flags
 * - Program state: loaded program, compiled status
 * - Event bus for frontend notifications
 * - Breakpoint manager
 * - Factory for creating ProgramRunner instances
 *
 * Execution state (next command, stop flag) is managed by ProgramRunner.
 */
public class Machine {

    /**
     * Global machine instance for static access.
     */
    private static Machine instance = new Machine();

    private final Memory memory;
    private final RegisterBody registers;
    private final Flags flags;
    private final MachineEventBus eventBus;
    private final BreakpointManager breakpointManager;

    private int stackBegin;
    private MemoryErrorHandler memoryErrorHandler;

    // Program state (moved from ExecutionContext)
    private Program program;

    // Current runner (only one can be active at a time)
    private ProgramRunner activeRunner;

    /**
     * Functional interface for handling memory errors.
     */
    @FunctionalInterface
    public interface MemoryErrorHandler {
        void handleError(int address, String message);
    }

    /**
     * Gets the global machine instance.
     *
     * @return the global Machine instance
     */
    public static Machine getInstance() {
        return instance;
    }

    /**
     * Resets the global machine instance (creates a fresh machine).
     */
    public static void resetInstance() {
        instance = new Machine();
    }

    /**
     * Creates a new MI machine with fresh state.
     */
    public Machine() {
        this.memory = new Memory();
        this.registers = new RegisterBody();
        this.flags = new Flags();
        this.eventBus = new MachineEventBus();
        this.breakpointManager = new BreakpointManager();
        this.stackBegin = 0;
        this.memoryErrorHandler = null;
        this.program = null;
        this.activeRunner = null;
    }

    /**
     * Resets the machine to initial state.
     */
    public void reset() {
        memory.reset();
        for (int i = 0; i < registers.getRegisterCount(); i++) {
            registers.getRegister(i).setContentAsNumber(0);
        }
        flags.reset();
        stackBegin = 0;
        memoryErrorHandler = null;
        program = null;
        activeRunner = null;
    }

    // ========================================================================
    // Hardware component accessors
    // ========================================================================

    public Memory getMemory() {
        return memory;
    }

    public RegisterBody getRegisters() {
        return registers;
    }

    public Flags getFlags() {
        return flags;
    }

    public MachineEventBus getEventBus() {
        return eventBus;
    }

    public BreakpointManager getBreakpointManager() {
        return breakpointManager;
    }

    public int getStackBegin() {
        return stackBegin;
    }

    public void setStackBegin(int stackBegin) {
        this.stackBegin = stackBegin;
    }

    // ========================================================================
    // Program Counter operations
    // ========================================================================

    /**
     * Gets the current program counter value.
     *
     * @return the PC value
     */
    public int getPC() {
        return registers.getRegister(PC_REGISTER).getContentAsNumber(4);
    }

    /**
     * Sets the program counter to a specific value.
     *
     * @param value the new PC value
     */
    public void setPC(int value) {
        registers.getRegister(PC_REGISTER).setContent(NumberConversion.intToByte(value, 4));
    }

    /**
     * Adds a value to the program counter.
     *
     * @param delta the value to add (can be negative)
     */
    public void addToPC(int delta) {
        Register pcRegister = registers.getRegister(PC_REGISTER);
        pcRegister.setContentAsNumber(pcRegister.getContentAsNumber(4) + delta);
    }

    /**
     * Reads the byte at the current PC from memory (without advancing PC).
     *
     * @return the byte value at PC
     */
    public int readByteAtPC() {
        return NumberConversion.myBytetoIntWithoutSign(
                memory.getContent(getPC(), 1));
    }

    /**
     * Fetches the next byte from memory and advances the PC.
     *
     * @return the fetched byte value
     */
    public int fetchByte() {
        int value = readByteAtPC();
        addToPC(1);
        return value;
    }

    // ========================================================================
    // Program state (moved from ExecutionContext)
    // ========================================================================

    /**
     * Gets the loaded program.
     *
     * @return the program, or null if none loaded
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Sets the loaded program.
     *
     * @param program the program to load
     */
    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * Returns whether a program has been successfully compiled/loaded.
     * Delegates to the program's compiled state.
     *
     * @return true if a program is loaded and compiled
     */
    public boolean isCompiled() {
        return program != null && program.isCompiled();
    }

    // ========================================================================
    // Runner factory
    // ========================================================================

    /**
     * Creates a new ProgramRunner for this machine.
     * Only one runner can be active at a time.
     *
     * @return the new runner
     * @throws IllegalStateException if a runner is already active
     */
    public ProgramRunner createRunner() {
        if (activeRunner != null && activeRunner.isRunning()) {
            throw new IllegalStateException("A runner is already active");
        }
        activeRunner = new ProgramRunner(this);
        return activeRunner;
    }

    /**
     * Gets the currently active runner, if any.
     *
     * @return the active runner, or null if none
     */
    public ProgramRunner getActiveRunner() {
        return activeRunner;
    }

    /**
     * Clears the active runner reference.
     * Called by ProgramRunner when execution completes.
     */
    void clearActiveRunner() {
        activeRunner = null;
    }

    // ========================================================================
    // Convenience methods (delegate to active runner)
    // ========================================================================

    /**
     * Gets the next command to be executed.
     * Delegates to the active runner if available.
     *
     * @return the next command, or null if no runner or no next command
     */
    public Command getNextCommand() {
        return activeRunner != null ? activeRunner.getNextCommand() : null;
    }

    /**
     * Gets the command at a specific address from the loaded program.
     *
     * @param address the memory address
     * @return the Command or null
     */
    public Command getCommandAtAddress(int address) {
        if (program == null) {
            return null;
        }
        java.util.ArrayList<Command> commands = program.getCommands();
        for (Command com : commands) {
            if (com.getAdress() == address) {
                return com;
            }
        }
        return null;
    }
}
