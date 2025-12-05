package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Shift instructions: SH, ROT.
 */
public class ShiftTest extends InstructionTestBase {

    // ==================== SH (Shift) ====================
    // SH op1, op2, op3 (shift op2 by op1 bits, store in op3)
    // If op1 > 0: Left shift, zero fill
    // If op1 < 0: Right shift, sign extend (Arithmetic)

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

    // ==================== ROT (Rotate) ====================
    // ROT op1, op2, op3
    // op1 > 0: Rotate Left
    // op1 < 0: Rotate Right

    @Test
    public void rot_left() {
        // 0x80000001 rot left 1 -> 0x00000003
        assembleAndRun(program(
                "MOVE W I -2147483647, R1", // 0x80000001
                "MOVE W I 1, R0",
                "ROT R0, R1, R2"
        ));
        assertEquals(3, getRegister(2));
        assertFalse(isOverflowFlag()); // ROT clears V? No, table says V=0.
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
}
