package engine.util;

import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryUpdateEvent;
import engine.Machine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks memory changes by subscribing to MemoryUpdateEvent.
 * Frontends can use this to determine which memory addresses have changed
 * since the last reset (e.g., for highlighting in the GUI).
 *
 * Usage:
 * <pre>
 * MemoryChangeTracker tracker = new MemoryChangeTracker();
 * // ... program runs, memory changes ...
 * Set<Integer> changed = tracker.getChangedAddresses();
 * tracker.reset(); // Clear for next instruction/step
 * </pre>
 */
public class MemoryChangeTracker implements MachineEventListener {

    /** Set of addresses that have been modified since last reset */
    private final Set<Integer> changedAddresses = new HashSet<>();

    /**
     * Creates a new tracker and subscribes to memory events.
     * Scans existing memory for non-zero values to capture already-loaded programs.
     */
    public MemoryChangeTracker() {
        Machine.getInstance().getEventBus().subscribe(MemoryUpdateEvent.class, this);
        // Scan memory for non-zero values to capture already-loaded programs
        scanExistingMemory();
    }

    /**
     * Scans memory for non-zero bytes and adds them to changedAddresses.
     * This captures memory that was written before this tracker was created.
     */
    private void scanExistingMemory() {
        byte[] memoryData = Machine.getInstance().getMemory().read(0,
                engine.MachineConstants.MEMORY_SIZE);
        for (int i = 0; i < memoryData.length; i++) {
            if (memoryData[i] != 0) {
                changedAddresses.add(i);
            }
        }
    }

    @Override
    public void onEvent(MachineEvent event) {
        if (event instanceof MemoryUpdateEvent) {
            changedAddresses.add(((MemoryUpdateEvent) event).getAddress());
        }
    }

    /**
     * Gets the set of addresses that have changed since last reset.
     *
     * @return unmodifiable set of changed addresses
     */
    public Set<Integer> getChangedAddresses() {
        return Collections.unmodifiableSet(changedAddresses);
    }

    /**
     * Checks if a specific address has changed since last reset.
     *
     * @param address the address to check
     * @return true if the address was modified
     */
    public boolean isChanged(int address) {
        return changedAddresses.contains(address);
    }

    /**
     * Returns whether any addresses have changed since last reset.
     *
     * @return true if any changes occurred
     */
    public boolean hasChanges() {
        return !changedAddresses.isEmpty();
    }

    /**
     * Clears the set of changed addresses.
     * Typically called after each instruction or when refreshing the display.
     */
    public void reset() {
        changedAddresses.clear();
    }

    /**
     * Unsubscribes from memory events.
     * Call this when the tracker is no longer needed.
     */
    public void cleanup() {
        Machine.getInstance().getEventBus().unsubscribe(MemoryUpdateEvent.class, this);
    }
}
