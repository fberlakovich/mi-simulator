package engine.util;

import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.RegisterChangeEvent;
import engine.Machine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks register changes by subscribing to RegisterChangeEvent.
 * Frontends can use this to determine which registers have changed
 * since the last reset (e.g., for highlighting in the GUI).
 *
 * Usage:
 * <pre>
 * RegisterChangeTracker tracker = new RegisterChangeTracker();
 * // ... program runs, registers change ...
 * Set<Integer> changed = tracker.getChangedRegisters();
 * tracker.reset(); // Clear for next instruction/step
 * </pre>
 */
public class RegisterChangeTracker implements MachineEventListener {

    /** Set of register indices that have been modified since last reset */
    private final Set<Integer> changedRegisters = new HashSet<>();

    /**
     * Creates a new tracker and subscribes to register events.
     */
    public RegisterChangeTracker() {
        Machine.getInstance().getEventBus().subscribe(RegisterChangeEvent.class, this);
    }

    @Override
    public void onEvent(MachineEvent event) {
        if (event instanceof RegisterChangeEvent) {
            changedRegisters.add(((RegisterChangeEvent) event).getRegisterIndex());
        }
    }

    /**
     * Gets the set of register indices that have changed since last reset.
     *
     * @return unmodifiable set of changed register indices
     */
    public Set<Integer> getChangedRegisters() {
        return Collections.unmodifiableSet(changedRegisters);
    }

    /**
     * Checks if a specific register has changed since last reset.
     *
     * @param registerIndex the register index to check (0-15)
     * @return true if the register was modified
     */
    public boolean isChanged(int registerIndex) {
        return changedRegisters.contains(registerIndex);
    }

    /**
     * Returns whether any registers have changed since last reset.
     *
     * @return true if any changes occurred
     */
    public boolean hasChanges() {
        return !changedRegisters.isEmpty();
    }

    /**
     * Clears the set of changed registers.
     * Typically called after each instruction or when refreshing the display.
     */
    public void reset() {
        changedRegisters.clear();
    }

    /**
     * Unsubscribes from register events.
     * Call this when the tracker is no longer needed.
     */
    public void cleanup() {
        Machine.getInstance().getEventBus().unsubscribe(RegisterChangeEvent.class, this);
    }
}
