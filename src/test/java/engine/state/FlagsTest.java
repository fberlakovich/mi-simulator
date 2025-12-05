package engine.state;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the MI Flags (Carry, Zero, Overflow, Negative).
 */
public class FlagsTest {

    private Flags flags;

    @Before
    public void setUp() {
        flags = new Flags();
    }

    @Test
    public void newFlags_shouldAllBeFalse() {
        assertFalse("Carry flag should be false initially", flags.isCarry());
        assertFalse("Zero flag should be false initially", flags.isZero());
        assertFalse("Overflow flag should be false initially", flags.isOverflow());
        assertFalse("Negative flag should be false initially", flags.isNegative());
    }

    @Test
    public void setCarry_shouldUpdateFlag() {
        flags.setCarry(true);
        assertTrue(flags.isCarry());

        flags.setCarry(false);
        assertFalse(flags.isCarry());
    }

    @Test
    public void setZero_shouldUpdateFlag() {
        flags.setZero(true);
        assertTrue(flags.isZero());

        flags.setZero(false);
        assertFalse(flags.isZero());
    }

    @Test
    public void setOverflow_shouldUpdateFlag() {
        flags.setOverflow(true);
        assertTrue(flags.isOverflow());

        flags.setOverflow(false);
        assertFalse(flags.isOverflow());
    }

    @Test
    public void setNegative_shouldUpdateFlag() {
        flags.setNegative(true);
        assertTrue(flags.isNegative());

        flags.setNegative(false);
        assertFalse(flags.isNegative());
    }

    @Test
    public void flags_shouldBeIndependent() {
        // Set all flags
        flags.setCarry(true);
        flags.setZero(true);
        flags.setOverflow(true);
        flags.setNegative(true);

        // Clear one, others should remain
        flags.setCarry(false);

        assertFalse("Carry should be false", flags.isCarry());
        assertTrue("Zero should still be true", flags.isZero());
        assertTrue("Overflow should still be true", flags.isOverflow());
        assertTrue("Negative should still be true", flags.isNegative());
    }
}
