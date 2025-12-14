package engine.events;

/**
 * Event emitted when the machine state is reset.
 *
 * Frontends should subscribe to this event to refresh their displays
 * when the machine is reset to initial state.
 */
public class MachineResetEvent extends MachineEvent {

    /**
     * Creates a new MachineResetEvent.
     */
    public MachineResetEvent() {
        super();
    }

    @Override
    public String toString() {
        return "MachineResetEvent{}";
    }
}
