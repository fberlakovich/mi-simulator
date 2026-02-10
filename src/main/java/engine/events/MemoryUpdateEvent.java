package engine.events;

/**
 * Event emitted when a memory value changes.
 *
 * Used to update the GUI memory view.
 */
public class MemoryUpdateEvent extends MachineEvent {

    private final int address;
    
    public MemoryUpdateEvent(int address) {
        super();
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format("MemoryUpdateEvent{address=0x%08X}", address);
    }
}


