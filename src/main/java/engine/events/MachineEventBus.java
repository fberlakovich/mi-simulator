package engine.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central event bus for the MI simulator.
 *
 * Provides a publish-subscribe mechanism for machine events. Frontends
 * subscribe to events they care about, and the core library publishes
 * events as state changes occur.
 *
 * <p>Thread-safe: Multiple threads can publish and subscribe concurrently.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * MachineEventBus bus = new MachineEventBus();
 *
 * // Subscribe to specific event types
 * bus.subscribe(MemoryErrorEvent.class, event -> {
 *     System.err.println("Memory error: " + event.getMessage());
 * });
 *
 * // Subscribe to all events
 * bus.subscribeAll(event -> log.debug("Event: " + event));
 *
 * // Publish an event
 * bus.publish(new MemoryErrorEvent(address, "Invalid access"));
 * }</pre>
 */
public class MachineEventBus {

    /**
     * A no-op event bus that ignores all operations.
     * Use this as a default instead of null to avoid null checks.
     */
    public static final MachineEventBus NO_OP = new MachineEventBus() {
        @Override
        public <T extends MachineEvent> void subscribe(Class<T> eventType, MachineEventListener listener) {
            // No-op
        }

        @Override
        public void subscribeAll(MachineEventListener listener) {
            // No-op
        }

        @Override
        public <T extends MachineEvent> void unsubscribe(Class<T> eventType, MachineEventListener listener) {
            // No-op
        }

        @Override
        public void unsubscribeAll(MachineEventListener listener) {
            // No-op
        }

        @Override
        public void publish(MachineEvent event) {
            // No-op
        }

        @Override
        public void clear() {
            // No-op
        }
    };

    private final Map<Class<? extends MachineEvent>, List<MachineEventListener>> listeners;
    private final List<MachineEventListener> globalListeners;

    public MachineEventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.globalListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Subscribe to a specific event type.
     *
     * @param eventType The class of events to listen for
     * @param listener  The listener to notify
     * @param <T>       The event type
     */
    public <T extends MachineEvent> void subscribe(Class<T> eventType, MachineEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Subscribe to all events.
     *
     * @param listener The listener to notify for all events
     */
    public void subscribeAll(MachineEventListener listener) {
        globalListeners.add(listener);
    }

    /**
     * Unsubscribe from a specific event type.
     *
     * @param eventType The class of events to stop listening for
     * @param listener  The listener to remove
     * @param <T>       The event type
     */
    public <T extends MachineEvent> void unsubscribe(Class<T> eventType, MachineEventListener listener) {
        List<MachineEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Unsubscribe from all events.
     *
     * @param listener The listener to remove
     */
    public void unsubscribeAll(MachineEventListener listener) {
        globalListeners.remove(listener);
        listeners.values().forEach(list -> list.remove(listener));
    }

    /**
     * Publish an event to all interested listeners.
     *
     * @param event The event to publish
     */
    public void publish(MachineEvent event) {
        // Notify type-specific listeners
        List<MachineEventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (MachineEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }

        // Notify global listeners
        for (MachineEventListener listener : globalListeners) {
            listener.onEvent(event);
        }
    }

    /**
     * Remove all listeners.
     */
    public void clear() {
        listeners.clear();
        globalListeners.clear();
    }
}
