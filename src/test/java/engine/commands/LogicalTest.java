package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Logical instructions: OR, XOR, ANDNOT.
 * Flags:
 *  C: Unchanged (-)
 *  V: 0
 *  Z: Set if result is zero
 *  N: Set if result is negative
 */
public class LogicalTest extends InstructionTestBase {

    @Test
    public void or_word() {
        assembleAndRun(program(
                "MOVE W I 5, R0",   // 0101
                "MOVE W I 3, R1",   // 0011
                "OR W R0, R1"       // 0111 = 7
        ));
        assertEquals(7, getRegister(1));
        assertFalse(isOverflowFlag());
        assertFalse(isZeroFlag());
        assertFalse(isNegativeFlag());
    }

    @Test
    public void xor_word() {
        assembleAndRun(program(
                "MOVE W I 5, R0",   // 0101
                "MOVE W I 3, R1",   // 0011
                "XOR W R0, R1"      // 0110 = 6
        ));
        assertEquals(6, getRegister(1));
        assertFalse(isOverflowFlag());
    }

    @Test
    public void andnot_word() {
        // ANDNOT: R1 = R1 AND (NOT R0)
        assembleAndRun(program(
                "MOVE W I 3, R0",   // 0011
                "MOVE W I 7, R1",   // 0111
                "ANDNOT W R0, R1"   // 0111 AND 1100 = 0100 = 4
        ));
        assertEquals(4, getRegister(1));
        assertFalse(isOverflowFlag());
    }

    @Test
    public void or_flags_zero() {
        assembleAndRun(program(
                "MOVE W I 0, R0",
                "MOVE W I 0, R1",
                "OR W R0, R1"
        ));
        assertTrue(isZeroFlag());
        assertFalse(isNegativeFlag());
    }

    @Test
    public void xor_flags_negative() {
        assembleAndRun(program(
                "MOVE W I -1, R0",  // 111...111
                "MOVE W I 0, R1",
                "XOR W R0, R1"
        ));
        assertEquals(-1, getRegister(1));
        assertTrue(isNegativeFlag());
        assertFalse(isZeroFlag());
    }
}
