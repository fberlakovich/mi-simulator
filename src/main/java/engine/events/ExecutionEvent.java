package engine.events;

import engine.commands.Command;

/**
 * Event published during program execution to notify frontends of state changes.
 */
public class ExecutionEvent extends MachineEvent {

    /** Type of execution event */
    public enum Type {
        /** Execution has started */
        STARTED,
        /** Execution has stopped (breakpoint, halt, or stop requested) */
        STOPPED,
        /** A single step/command has completed */
        STEP_COMPLETED,
        /** Breakpoint was hit */
        BREAKPOINT_HIT,
        /** Program reached HALT instruction */
        PROGRAM_ENDED
    }

    private final Type type;
    private final int currentAddress;
    private final int nextAddress;
    private final String message;
    private final Command command;

    /**
     * Creates an execution event.
     *
     * @param type the event type
     * @param currentAddress the current PC address
     * @param nextAddress the next instruction address (or -1 if unknown/ended)
     * @param message optional message
     * @param command the command that was executed (may be null for some event types)
     */
    public ExecutionEvent(Type type, int currentAddress, int nextAddress, String message, Command command) {
        this.type = type;
        this.currentAddress = currentAddress;
        this.nextAddress = nextAddress;
        this.message = message;
        this.command = command;
    }

    /**
     * Creates an execution event without message or command.
     *
     * @param type the event type
     * @param currentAddress the current PC address
     * @param nextAddress the next instruction address
     */
    public ExecutionEvent(Type type, int currentAddress, int nextAddress) {
        this(type, currentAddress, nextAddress, null, null);
    }

    /**
     * Creates an execution event with a command.
     *
     * @param type the event type
     * @param currentAddress the current PC address
     * @param nextAddress the next instruction address
     * @param command the command that was executed
     */
    public ExecutionEvent(Type type, int currentAddress, int nextAddress, Command command) {
        this(type, currentAddress, nextAddress, null, command);
    }

    /**
     * Creates a simple execution event.
     *
     * @param type the event type
     */
    public ExecutionEvent(Type type) {
        this(type, -1, -1, null, null);
    }

    public Type getType() {
        return type;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public int getNextAddress() {
        return nextAddress;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Gets the command that was executed.
     *
     * @return the executed command, or null if not applicable
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns whether execution has ended (either stopped or program ended).
     *
     * @return true if execution is no longer running
     */
    public boolean isExecutionEnded() {
        return type == Type.STOPPED || type == Type.PROGRAM_ENDED;
    }

    /**
     * Returns whether the program reached its natural end (HALT).
     *
     * @return true if program reached HALT
     */
    public boolean isProgramEnded() {
        return type == Type.PROGRAM_ENDED;
    }

    @Override
    public String toString() {
        return "ExecutionEvent{type=" + type +
               ", currentAddress=" + currentAddress +
               ", nextAddress=" + nextAddress +
               (message != null ? ", message='" + message + "'" : "") +
               (command != null ? ", command=" + command : "") + "}";
    }
}
