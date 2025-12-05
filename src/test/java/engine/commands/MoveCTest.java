package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MOVEC instruction.
 */
public class MoveCTest extends InstructionTestBase {

    @Test
    public void movec_word_flags() {
        // MOVEC W I 0, R0 -> R0 = -1 (all 1s).
        // Flags: C=-, V=0, Z=0, N=1.
        assembleAndRun(program(
                "MOVE W I 0, R0",
                "MOVEC W R0, R1"
        ));
        assertEquals(-1, getRegister(1));
        // C is unchanged, assume started at 0
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertTrue("Negative flag should be 1", isNegativeFlag());
    }
}
