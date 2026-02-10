package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ROT (Rotate) instruction.
 */
public class RotTest extends InstructionTestBase {

    @Test
    public void rot_left() {
        // 0x80000001 rot left 1 -> 0x00000003
        assembleAndRun(program(
                "MOVE W I -2147483647, R1", // 0x80000001
                "MOVE W I 1, R0",
                "ROT R0, R1, R2"
        ));
        assertEquals(3, getRegister(2));
        assertFalse(isOverflowFlag());
    }

    @Test
    public void rot_right() {
        // 0x00000003 rot right 1 -> 0x80000001
        assembleAndRun(program(
                "MOVE W I 3, R1",
                "MOVE W I -1, R0",
                "ROT R0, R1, R2"
        ));
        assertEquals(-2147483647, getRegister(2));
        assertTrue(isNegativeFlag());
    }

    @Test
    public void rot_flags() {
        // Flags: C=-, V=0, Z=*, N=*.
        assembleAndRun(program(
                "MOVE W I 1, R0",  // Count
                "MOVE W I 1, R1",  // Value
                "ROT R0, R1, R2"   // Rotate Left 1 -> 2
        ));
        assertEquals(2, getRegister(2));
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertFalse("Negative flag should be 0", isNegativeFlag());
    }

    @Test
    public void rot_zero_result() {
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "MOVE W I 0, R1",
                "ROT R0, R1, R2"
        ));
        assertEquals(0, getRegister(2));
        assertTrue("Zero flag should be 1", isZeroFlag());
    }
}
