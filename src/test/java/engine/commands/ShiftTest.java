package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SH (Shift) instruction.
 * ROT (Rotate) tests are in RotTest.
 */
public class ShiftTest extends InstructionTestBase {

    @Test
    public void sh_left_positive() {
        assembleAndRun(program(
                "MOVE W I 2, R0",
                "MOVE W I 3, R1",   // 0011
                "SH R0, R1, R2"     // 0011 << 2 = 1100 = 12
        ));
        assertEquals(12, getRegister(2));
        assertFalse(isOverflowFlag());
    }

    @Test
    public void sh_right_arithmetic_positive() {
        assembleAndRun(program(
                "MOVE W I -2, R0",
                "MOVE W I 12, R1",  // 1100
                "SH R0, R1, R2"     // 1100 >> 2 = 0011 = 3
        ));
        assertEquals(3, getRegister(2));
    }

    @Test
    public void sh_right_arithmetic_negative() {
        assembleAndRun(program(
                "MOVE W I -1, R0",
                "MOVE W I -4, R1",  // ...11111100
                "SH R0, R1, R2"     // ...11111110 = -2
        ));
        assertEquals(-2, getRegister(2));
        assertTrue("Negative flag should be set", isNegativeFlag());
    }

    @Test
    public void sh_overflow() {
        // Left shift that changes sign bit should set overflow
        // 0x40000000 (positive) << 1 = 0x80000000 (negative)
        assembleAndRun(program(
                "MOVE W I 1073741824, R1", // 2^30
                "MOVE W I 1, R0",
                "SH R0, R1, R2"
        ));
        assertEquals(-2147483648, getRegister(2));
        assertTrue("Overflow flag should be set", isOverflowFlag());
    }
}
