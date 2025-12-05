package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ROT instruction.
 */
public class RotTest extends InstructionTestBase {

    @Test
    public void rot_flags() {
        // ROT.
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
