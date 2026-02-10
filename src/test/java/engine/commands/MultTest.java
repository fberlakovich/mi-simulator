package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the MULT instruction.
 *
 * MULT computes: op1 * op2 -> result
 * Flags:
 *  C: 0 (cleared)
 *  V: Set if overflow
 *  Z: Set if result is zero
 *  N: Set if result is negative
 */
public class MultTest extends InstructionTestBase {

    // ==================== Word (32-bit) Integer Tests ====================

    @Test
    public void mult_word_positive() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 20, R1",
                "MULT W R0, R1"  // R1 = 10 * 20 = 200
        ));
        assertEquals(200, getRegister(1));
        assertFalse("Carry flag should be clear", isCarryFlag());
        assertFalse("Overflow flag should be clear", isOverflowFlag());
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    @Test
    public void mult_word_negativeResult() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I -20, R1",
                "MULT W R0, R1"  // R1 = 10 * -20 = -200
        ));
        assertEquals(-200, getRegister(1));
        assertTrue("Negative flag should be set", isNegativeFlag());
        assertFalse("Carry flag should be clear", isCarryFlag());
    }

    @Test
    public void mult_word_overflow() {
        // MAX_VALUE * 2 -> Overflow
        // 2147483647 * 2 = 4294967294 (unsigned) -> -2 (signed) in 32-bit? No.
        // 0x7FFFFFFF * 2 = 0xFFFFFFFE = -2
        assembleAndRun(program(
                "MOVE W I 2147483647, R0",
                "MULT W I 2, R0"
        ));
        // Result wraps around
        assertEquals(-2, getRegister(0));
        assertTrue("Overflow flag should be set", isOverflowFlag());
        assertFalse("Carry flag should be clear", isCarryFlag());
        assertTrue("Negative flag should be set", isNegativeFlag());
    }

    @Test
    public void mult_word_zero() {
        assembleAndRun(program(
                "MOVE W I 12345, R0",
                "MULT W I 0, R0"
        ));
        assertEquals(0, getRegister(0));
        assertTrue("Zero flag should be set", isZeroFlag());
        assertFalse("Overflow flag should be clear", isOverflowFlag());
        assertFalse("Carry flag should be clear", isCarryFlag());
    }

    // ==================== Float (32-bit) Tests ====================

    @Test
    public void mult_float_simple() {
        assembleAndRun(program(
                "MOVE F I 2.5, R0",
                "MOVE F I 4.0, R1",
                "MULT F R0, R1" // R1 = 2.5 * 4.0 = 10.0
        ));
        assertEquals(10.0f, getRegisterAsFloat(1), 0.0001f);
        assertFalse("Carry flag should be clear", isCarryFlag());
    }
}
