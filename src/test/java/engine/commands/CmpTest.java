package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CMP instruction according to MI specification (page 27).
 *
 * MI Specification defines:
 *   CMP B|H|W|F|D a1,a2
 *   - zero condition code := 1, falls S[a1] = S[a2]
 *   - negative condition code := 1, falls S[a1] < S[a2]
 *   - keine Wirkung sonst (no effect otherwise)
 *
 * Condition Codes (page 35):
 *   C: - (unchanged)
 *   V: - (unchanged)
 *   Z: * (set based on result)
 *   N: * (set based on result)
 *
 * Note: This differs from SUB which computes S[a2] - S[a1].
 * CMP directly compares operands: N is set when a1 < a2.
 */
public class CmpTest extends InstructionTestBase {

    @Test
    public void cmp_equal() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 10, R1",
                "CMP W R0, R1"
        ));
        assertTrue("Zero flag should be set", isZeroFlag());
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    @Test
    public void cmp_less_than() {
        // R0=10, R1=20. R0 < R1.
        // Manual says: N=1 if S[a1] < S[a2]
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 20, R1",
                "CMP W R0, R1"
        ));
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertTrue("Negative flag should be set (R0 < R1)", isNegativeFlag());
    }

    @Test
    public void cmp_greater_than() {
        // R0=20, R1=10.
        assembleAndRun(program(
                "MOVE W I 20, R0",
                "MOVE W I 10, R1",
                "CMP W R0, R1"
        ));
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    /**
     * Per MI spec (page 35), CMP should NOT change Carry flag (C: -)
     * This test verifies the Carry flag remains unchanged after CMP.
     */
    @Test
    public void cmp_doesNotChangeCarryFlag() {
        // First set carry flag via an ADD that overflows unsigned
        // Then verify CMP doesn't clear it
        assembleAndRun(program(
                "MOVE W I -1, R0",  // 0xFFFFFFFF
                "MOVE W I 1, R1",
                "ADD W R0, R1, R2", // This sets carry
                "MOVE W I 5, R3",
                "MOVE W I 10, R4",
                "CMP W R3, R4"      // CMP should NOT change carry
        ));
        // After ADD -1 + 1 = 0 with carry, CMP should preserve carry
        assertTrue("Carry flag should remain set (CMP doesn't change C)", isCarryFlag());
    }

    /**
     * Per MI spec (page 35), CMP should NOT change Overflow flag (V: -)
     * This test verifies the Overflow flag remains unchanged after CMP.
     */
    @Test
    public void cmp_doesNotChangeOverflowFlag() {
        // First set overflow flag via an ADD that overflows signed
        // Then verify CMP doesn't clear it
        assembleAndRun(program(
                "MOVE W I 2147483647, R0",  // MAX_VALUE
                "MOVE W I 1, R1",
                "ADD W R0, R1, R2",         // This sets overflow (positive + positive = negative)
                "MOVE W I 5, R3",
                "MOVE W I 10, R4",
                "CMP W R3, R4"              // CMP should NOT change overflow
        ));
        assertTrue("Overflow flag should remain set (CMP doesn't change V)", isOverflowFlag());
    }

    /**
     * Test CMP with negative numbers per MI specification.
     */
    @Test
    public void cmp_negativeNumbers() {
        // -10 < -5 (in signed arithmetic)
        assembleAndRun(program(
                "MOVE W I -10, R0",
                "MOVE W I -5, R1",
                "CMP W R0, R1"
        ));
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertTrue("Negative flag should be set (-10 < -5)", isNegativeFlag());
    }

    /**
     * Test CMP with byte operands.
     */
    @Test
    public void cmp_byte() {
        assembleAndRun(program(
                "MOVE B I 5, R0",
                "MOVE B I 10, R1",
                "CMP B R0, R1"
        ));
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertTrue("Negative flag should be set (5 < 10)", isNegativeFlag());
    }

    /**
     * Test CMP with half-word operands.
     */
    @Test
    public void cmp_halfword() {
        assembleAndRun(program(
                "MOVE H I 1000, R0",
                "MOVE H I 1000, R1",
                "CMP H R0, R1"
        ));
        assertTrue("Zero flag should be set (equal)", isZeroFlag());
    }
}
