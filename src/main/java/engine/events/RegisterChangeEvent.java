package engine.events;

/**
 * Event emitted when a register value changes.
 *
 * Useful for IDE integrations that want to show register
 * state in real-time during debugging.
 */
public class RegisterChangeEvent extends MachineEvent {

    private final int registerIndex;
    private final int oldValue;
    private final int newValue;

    public RegisterChangeEvent(int registerIndex, int oldValue, int newValue) {
        super();
        this.registerIndex = registerIndex;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getRegisterIndex() {
        return registerIndex;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return String.format("RegisterChangeEvent{R%d: 0x%08X -> 0x%08X}",
                registerIndex, oldValue, newValue);
    }
}
