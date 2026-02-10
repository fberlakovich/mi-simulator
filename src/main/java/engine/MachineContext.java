package engine;

import engine.commands.Command;
import engine.events.MachineEventBus;
import engine.program.Program;
import engine.state.Flags;
import engine.state.Memory;
import engine.state.RegisterFile;

/**
 * Interface for frontends to interact with the machine.
 * Provides both queries (state reads) and commands (state modifications).
 * Frontends should use this interface instead of Machine.getInstance().
 */
public interface MachineContext {

    // ========== EVENT BUS ==========

    /**
     * Gets the event bus for subscribing to state change notifications.
     *
     * @return the machine event bus
     */
    MachineEventBus getEventBus();

    // ========== QUERIES (Read State) ==========

    /**
     * Gets the register file.
     *
     * @return the registers
     */
    RegisterFile getRegisters();

    /**
     * Gets the memory.
     *
     * @return the memory
     */
    Memory getMemory();

    /**
     * Gets the CPU flags.
     *
     * @return the flags
     */
    Flags getFlags();

    /**
     * Checks if a program is compiled and loaded.
     *
     * @return true if a program is loaded
     */
    boolean isCompiled();

    /**
     * Gets the current program.
     *
     * @return the program, or null if none loaded
     */
    Program getProgram();

    /**
     * Gets the command at a specific memory address.
     *
     * @param address the memory address
     * @return the command at that address, or null if none
     */
    Command getCommandAtAddress(int address);

    // ========== COMMANDS (Modify State) ==========

    /**
     * Loads a compiled program.
     *
     * @param program the program to load
     */
    void setProgram(Program program);

    /**
     * Resets all machine state to initial values.
     */
    void reset();

    /**
     * Creates an execution runner for the current program.
     *
     * @return a new program runner
     */
    ProgramRunner createRunner();

    /**
     * Gets the stack begin address (for memory visualization).
     *
     * @return the stack begin address
     */
    int getStackBegin();

    /**
     * Sets the stack begin address.
     *
     * @param address the stack begin address
     */
    void setStackBegin(int address);
}
