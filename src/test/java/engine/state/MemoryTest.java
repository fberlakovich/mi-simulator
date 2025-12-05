package engine.state;

import engine.Machine;
import engine.util.MemoryChangeTracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Memory operations.
 */
public class MemoryTest {

    private MemoryChangeTracker tracker;

    @Before
    public void setUp() {
        Machine.resetInstance();
        tracker = new MemoryChangeTracker();
    }

    @After
    public void tearDown() {
        if (tracker != null) {
            tracker.cleanup();
        }
    }

    @Test
    public void getContent_shouldReturnCorrectBytes() {
        MyByte[] data = {new MyByte(0x12), new MyByte(0x34), new MyByte(0x56), new MyByte(0x78)};
        Machine.getInstance().getMemory().setContent(100, data);

        MyByte[] result = Machine.getInstance().getMemory().getContent(100, 4);

        assertEquals(4, result.length);
        assertEquals(0x12, result[0].getContent());
        assertEquals(0x34, result[1].getContent());
        assertEquals(0x56, result[2].getContent());
        assertEquals(0x78, result[3].getContent());
    }

    @Test
    public void getContentAsInt_shouldReturnCorrectSignedValue() {
        // Set bytes representing -1 (0xFFFFFFFF in two's complement)
        MyByte[] data = {new MyByte(0xFF), new MyByte(0xFF), new MyByte(0xFF), new MyByte(0xFF)};
        Machine.getInstance().getMemory().setContent(200, data);

        int result = Machine.getInstance().getMemory().getContentAsInt(200, 4);

        assertEquals(-1, result);
    }

    @Test
    public void setContent_shouldTrackChanges() {
        assertTrue("Changes should be empty initially", tracker.getChangedAddresses().isEmpty());

        MyByte[] data = {new MyByte(0x42)};
        Machine.getInstance().getMemory().setContent(300, data);

        assertTrue("Changes should track modified addresses", tracker.isChanged(300));
    }

    @Test
    public void resetChanges_shouldClearChangeTracking() {
        MyByte[] data = {new MyByte(0x42)};
        Machine.getInstance().getMemory().setContent(400, data);
        assertFalse("Changes should not be empty after write", tracker.getChangedAddresses().isEmpty());

        tracker.reset();

        assertTrue("Changes should be empty after reset", tracker.getChangedAddresses().isEmpty());
    }

    @Test
    public void getRawByte_shouldReturnBytesAtAddress() {
        MyByte[] data = {new MyByte(0xAB)};
        Machine.getInstance().getMemory().setContent(500, data);

        MyByte result = Machine.getInstance().getMemory().getRawByte(500);

        assertEquals((byte) 0xAB, (byte) result.getContent());
    }

    @Test
    public void getRawByte_shouldReturnZeroForOutOfBounds() {
        // Addresses outside valid range should return 0 without error
        MyByte result = Machine.getInstance().getMemory().getRawByte(-1);
        assertEquals(0, result.getContent());

        MyByte result2 = Machine.getInstance().getMemory().getRawByte(Integer.MAX_VALUE);
        assertEquals(0, result2.getContent());
    }

    @Test
    public void memory_shouldBeInitializedToZero() {
        // Check a few random addresses
        assertEquals(0, Machine.getInstance().getMemory().getRawByte(0).getContent());
        assertEquals(0, Machine.getInstance().getMemory().getRawByte(1000).getContent());
        assertEquals(0, Machine.getInstance().getMemory().getRawByte(500000).getContent());
    }
}
