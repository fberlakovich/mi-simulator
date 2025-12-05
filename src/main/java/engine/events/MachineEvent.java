package engine.events;

/**
 * Base class for all machine events.
 *
 * The MI simulator uses an event-driven architecture where the core library
 * emits events that frontends (GUI, CLI, IDE plugins) can observe and react to.
 */
public abstract class MachineEvent {

    private final long timestamp;

    protected MachineEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return The timestamp when this event was created
     */
    public long getTimestamp() {
        return timestamp;
    }
}