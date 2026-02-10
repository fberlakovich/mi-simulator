package engine.events;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Tests for the MachineEventBus publish-subscribe system.
 */
public class MachineEventBusTest {

    private MachineEventBus eventBus;

    @Before
    public void setUp() {
        eventBus = new MachineEventBus();
    }

    @Test
    public void subscribe_shouldReceiveEventsOfSpecificType() {
        AtomicReference<MemoryAccessErrorEvent> received = new AtomicReference<>();
        eventBus.subscribe(MemoryAccessErrorEvent.class, event ->
            received.set((MemoryAccessErrorEvent) event));

        MemoryAccessErrorEvent event = new MemoryAccessErrorEvent(
                MemoryAccessErrorEvent.Type.OUT_OF_BOUNDS, 0x1000, "Test error", true);
        eventBus.publish(event);

        assertNotNull("Event should be received", received.get());
        assertEquals(0x1000, received.get().getAddress());
        assertEquals("Test error", received.get().getMessage());
    }

    @Test
    public void subscribe_shouldNotReceiveEventsOfDifferentType() {
        AtomicInteger callCount = new AtomicInteger(0);
        eventBus.subscribe(MemoryAccessErrorEvent.class, event -> callCount.incrementAndGet());

        // Publish a different event type
        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test"));

        assertEquals("Should not receive events of different type", 0, callCount.get());
    }

    @Test
    public void subscribeAll_shouldReceiveAllEvents() {
        AtomicInteger callCount = new AtomicInteger(0);
        eventBus.subscribeAll(event -> callCount.incrementAndGet());

        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test1"));
        eventBus.publish(new MemoryAccessErrorEvent(
                MemoryAccessErrorEvent.Type.OUT_OF_BOUNDS, 0, "Test2", false));

        assertEquals("Should receive all events", 2, callCount.get());
    }

    @Test
    public void unsubscribe_shouldStopReceivingEvents() {
        AtomicInteger callCount = new AtomicInteger(0);
        MachineEventListener listener = event -> callCount.incrementAndGet();

        eventBus.subscribe(AssemblyEvent.class, listener);
        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test1"));
        assertEquals(1, callCount.get());

        eventBus.unsubscribe(AssemblyEvent.class, listener);
        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test2"));
        assertEquals("Should not receive after unsubscribe", 1, callCount.get());
    }

    @Test
    public void clear_shouldRemoveAllListeners() {
        AtomicInteger callCount = new AtomicInteger(0);
        eventBus.subscribe(AssemblyEvent.class, event -> callCount.incrementAndGet());
        eventBus.subscribeAll(event -> callCount.incrementAndGet());

        eventBus.clear();
        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test"));

        assertEquals("No listeners should remain after clear", 0, callCount.get());
    }

    @Test
    public void multipleSubscribers_shouldAllReceiveEvents() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        eventBus.subscribe(AssemblyEvent.class, event -> count1.incrementAndGet());
        eventBus.subscribe(AssemblyEvent.class, event -> count2.incrementAndGet());

        eventBus.publish(new AssemblyEvent(AssemblyEvent.Type.SUCCESS, "Test"));

        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
    }
}
