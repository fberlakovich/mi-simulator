package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MOVEN instruction.
 */
public class MoveNTest extends InstructionTestBase {

    @Test
    public void moven_word_flags() {
        // MOVEN W I 10, R0 -> R0 = -10.
        // Flags: C=0, V=0, Z=0, N=1.
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVEN W R0, R1"
        ));
        assertEquals(-10, getRegister(1));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertTrue("Negative flag should be 1", isNegativeFlag());
    }

    @Test
    public void moven_word_overflow() {
        // MOVEN MIN_VALUE -> Overflow
        // -(-2147483648) = 2147483648 (overflows to MIN_VALUE in 32-bit signed)
        // But theoretically it is overflow.
        assembleAndRun(program(
                "MOVE W I -2147483648, R0",
                "MOVEN W R0, R1"
        ));
        assertEquals(-2147483648, getRegister(1));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertTrue("Overflow flag should be 1", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertTrue("Negative flag should be 1", isNegativeFlag()); // Result is negative
    }
}
