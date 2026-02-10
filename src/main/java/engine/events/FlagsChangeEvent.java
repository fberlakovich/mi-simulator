package engine.events;

/**
 * Event emitted when a CPU flag changes.
 *
 * Useful for frontends that want to display flag state changes
 * in real-time during debugging.
 */
public class FlagsChangeEvent extends MachineEvent {

    /**
     * Enum representing the CPU flags.
     */
    public enum Flag {
        CARRY,
        ZERO,
        OVERFLOW,
        NEGATIVE
    }

    private final Flag flag;
    private final boolean oldValue;
    private final boolean newValue;

    /**
     * Creates a new FlagsChangeEvent.
     *
     * @param flag     the flag that changed
     * @param oldValue the previous value
     * @param newValue the new value
     */
    public FlagsChangeEvent(Flag flag, boolean oldValue, boolean newValue) {
        super();
        this.flag = flag;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * @return the flag that changed
     */
    public Flag getFlag() {
        return flag;
    }

    /**
     * @return the previous flag value
     */
    public boolean getOldValue() {
        return oldValue;
    }

    /**
     * @return the new flag value
     */
    public boolean getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return String.format("FlagsChangeEvent{%s: %b -> %b}", flag, oldValue, newValue);
    }
}
