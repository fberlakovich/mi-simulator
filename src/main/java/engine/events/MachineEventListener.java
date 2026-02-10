package engine.events;

/**
 * Listener interface for machine events.
 *
 * Frontends implement this interface to receive notifications about
 * machine state changes, errors, and execution events.
 *
 * <p>Example implementations:</p>
 * <ul>
 *   <li>GUI: Updates Swing components, shows dialogs</li>
 *   <li>CLI: Prints to stdout/stderr</li>
 *   <li>IDE Plugin: Updates editor markers, tool windows</li>
 *   <li>Test: Captures events for assertions</li>
 * </ul>
 */
@FunctionalInterface
public interface MachineEventListener {

    /**
     * Called when a machine event occurs.
     *
     * @param event The event that occurred
     */
    void onEvent(MachineEvent event);
}