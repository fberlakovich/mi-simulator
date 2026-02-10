package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Transport instructions: CLEAR, MOVEN, MOVEC.
 * Tests unique edge cases not covered by dedicated test classes
 * (MoveNTest, MoveCTest, MoveATest, ConvTest).
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
}
