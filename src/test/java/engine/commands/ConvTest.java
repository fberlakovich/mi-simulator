package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CONV instruction.
 */
public class ConvTest extends InstructionTestBase {

    @Test
    public void conv_flags() {
        // CONV B to W.
        // Flags: C=0, V=0, Z=*, N=*.
        assembleAndRun(program(
                "MOVE B I -1, R0", // 0xFF
                "CONV R0, R1"      // 0xFFFFFFFF
        ));
        assertEquals(-1, getRegister(1));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertTrue("Negative flag should be 1", isNegativeFlag());
    }
}
