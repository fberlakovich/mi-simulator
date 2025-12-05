package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Transport instructions: CLEAR, MOVEN, MOVEC, MOVEA, CONV.
 *
 * MI Specification Condition Codes (page 35):
 * - CLEAR: C=-, V=0, Z=1, N=0
 * - MOVEN: C=0, V=I*,F0, Z=*, N=*  (I* = depends on integer result, F0 = 0 for float)
 * - MOVEC: C=-, V=0, Z=*, N=*
 * - MOVEA: C=-, V=-, Z=-, N=-
 * - CONV:  C=0, V=0, Z=*, N=*
 */
public class TransportTest extends InstructionTestBase {

    /**
     * Test CLEAR instruction per MI specification (page 26).
     * CLEAR sets the operand to 0.
     * Condition Codes (page 35): C=-, V=0, Z=1, N=0
     */
    @Test
    public void clear_word() {
        assembleAndRun(program(
                "MOVE W I 123, R0",
                "CLEAR W R0"
        ));
        assertEquals(0, getRegister(0));
        assertTrue("Zero flag should be set (Z=1 per spec)", isZeroFlag());
        assertFalse("Overflow flag should be clear (V=0 per spec)", isOverflowFlag());
        assertFalse("Negative flag should be clear (N=0 per spec)", isNegativeFlag());
    }

    /**
     * Test MOVEN instruction per MI specification (page 26).
     * MOVEN: S[a2] := - S[a1] (move negated)
     * Condition Codes (page 35): C=0, V=I*,F0, Z=*, N=*
     */
    @Test
    public void moven_word() {
        // MOVEN (Move Negated)
        // R1 = -R0
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVEN W R0, R1"
        ));
        assertEquals(-10, getRegister(1));
        assertTrue("Negative flag should be set (result < 0)", isNegativeFlag());
        assertFalse("Carry flag should be clear (C=0 per spec)", isCarryFlag());
    }

    /**
     * Test MOVEN with zero produces zero and sets Z flag.
     */
    @Test
    public void moven_zero() {
        assembleAndRun(program(
                "MOVE W I 0, R0",
                "MOVEN W R0, R1"
        ));
        assertEquals(0, getRegister(1));
        assertTrue("Zero flag should be set", isZeroFlag());
        assertFalse("Carry flag should be clear (C=0 per spec)", isCarryFlag());
    }

    /**
     * Test MOVEN integer overflow: negating MIN_VALUE produces overflow.
     * Per spec (page 35): V=I* means overflow is set based on integer result.
     */
    @Test
    public void moven_overflow() {
        // Negating Integer.MIN_VALUE (-2147483648) would be 2147483648
        // which doesn't fit in 32-bit signed integer -> overflow
        assembleAndRun(program(
                "MOVE W I -2147483648, R0",
                "MOVEN W R0, R1"
        ));
        // Result wraps to MIN_VALUE again
        assertEquals(-2147483648, getRegister(1));
        assertTrue("Overflow flag should be set (can't represent -MIN_VALUE)", isOverflowFlag());
    }

    /**
     * Test MOVEC instruction per MI specification (page 26).
     * MOVEC: S[a2] := NOT S[a1] (move complemented / bitwise NOT)
     * Condition Codes (page 35): C=-, V=0, Z=*, N=*
     */
    @Test
    public void movec_word() {
        // MOVEC (Move Complemented)
        // R1 = ~R0
        assembleAndRun(program(
                "MOVE W I 0, R0", // 00...00
                "MOVEC W R0, R1"  // 11...11 (-1)
        ));
        assertEquals(-1, getRegister(1));
        assertTrue("Negative flag should be set (result < 0)", isNegativeFlag());
        assertFalse("Overflow flag should be clear (V=0 per spec)", isOverflowFlag());
    }

    /**
     * Test MOVEC with -1 produces 0.
     */
    @Test
    public void movec_allOnes() {
        assembleAndRun(program(
                "MOVE W I -1, R0", // 11...11
                "MOVEC W R0, R1"   // 00...00
        ));
        assertEquals(0, getRegister(1));
        assertTrue("Zero flag should be set", isZeroFlag());
        assertFalse("Overflow flag should be clear (V=0 per spec)", isOverflowFlag());
    }

    @Test
    public void movea() {
        // MOVEA (Move Address)
        // MOVEA label, R0 -> R0 = address of label
        assembleAndRun(program(
                "MOVEA label, R0",
                "HALT",
                "label: DD W 0"
        ));
        // We don't know the exact address, but it should be non-zero
        // Since label is after HALT (which is after MOVEA), address > 0.
        // Address of MOVEA is 0. Length 6 bytes (opcode + op1 + op2).
        // HALT is 1 byte.
        // label is at 0+6+1 = 7? 
        // Actually address is resolved by Assembler.
        // We can just check if it ran.
        assertNotEquals(0, getRegister(0));
    }

    @Test
    public void conv() {
        // CONV Byte to Word
        // Sign extension
        assembleAndRun(program(
                "MOVE B I -1, R0", // FF
                "CONV R0, R1"      // FFFFFFFF (-1)
        ));
        assertEquals(-1, getRegister(1));
    }
}
