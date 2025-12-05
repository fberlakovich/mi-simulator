package engine.events;

/**
 * Event emitted when a memory access error occurs.
 *
 * Replaces the JOptionPane.showMessageDialog() calls in Memory.java.
 * Frontends can handle this appropriately:
 * - GUI: Show error dialog
 * - CLI: Print to stderr
 * - IDE: Show error notification
 * - Test: Assert expected errors
 */
public class MemoryAccessErrorEvent extends MachineEvent {

    public enum Type {
        READ_VIOLATION,
        WRITE_VIOLATION,
        ALIGNMENT_ERROR,
        OUT_OF_BOUNDS
    }

    private final Type type;
    private final int address;
    private final String message;
    private final boolean fatal;

    public MemoryAccessErrorEvent(Type type, int address, String message, boolean fatal) {
        super();
        this.type = type;
        this.address = address;
        this.message = message;
        this.fatal = fatal;
    }

    public Type getType() {
        return type;
    }

    public int getAddress() {
        return address;
    }

    public String getMessage() {
        return message;
    }

    /**
     * @return true if this error should halt program execution
     */
    public boolean isFatal() {
        return fatal;
    }

    @Override
    public String toString() {
        return String.format("MemoryAccessEvent{type=%s, address=0x%08X, message='%s', fatal=%s}",
                type, address, message, fatal);
    }
}
