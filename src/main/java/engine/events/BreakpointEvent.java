package engine.events;

/**
 * Event emitted for breakpoint-related actions.
 */
public class BreakpointEvent extends MachineEvent {

    public enum Type {
        /** Breakpoint was set */
        SET,
        /** Breakpoint was removed */
        REMOVED,
        /** Execution hit a breakpoint */
        HIT,
        /** Failed to set breakpoint */
        SET_FAILED
    }

    private final Type type;
    private final int lineNumber;
    private final int address;
    private final String message;

    public BreakpointEvent(Type type, int lineNumber, int address) {
        this(type, lineNumber, address, null);
    }

    public BreakpointEvent(Type type, int lineNumber, int address, String message) {
        super();
        this.type = type;
        this.lineNumber = lineNumber;
        this.address = address;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getAddress() {
        return address;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("BreakpointEvent{type=%s, line=%d, addr=0x%08X, message='%s'}",
                type, lineNumber, address, message);
    }
}
