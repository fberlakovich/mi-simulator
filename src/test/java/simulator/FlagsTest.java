package simulator;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlagsTest extends MIOperationTestBase {

    @Test
    public void testZeroFlagOnZeroResult() {
        assertTrue(executeProgram("SEG\nMOVE W I 0, R1\nADD W I 0, R1\nHALT"));
        assertTrue("Zero flag should be set when result is 0", isZeroFlag());
    }

    @Test
    public void testZeroFlagOnNonZeroResult() {
        assertTrue(executeProgram("SEG\nMOVE W I 1, R1\nADD W I 1, R1\nHALT"));
        assertFalse("Zero flag should not be set when result is non-zero", isZeroFlag());
    }

    @Test
    public void testNegativeFlagOnNegativeResult() {
        assertTrue(executeProgram("SEG\nMOVE W I 10, R1\nSUB W I 20, R1\nHALT"));
        assertTrue("Negative flag should be set when result is negative", isNegativeFlag());
    }

    @Test
    public void testNegativeFlagOnPositiveResult() {
        assertTrue(executeProgram("SEG\nMOVE W I 20, R1\nSUB W I 10, R1\nHALT"));
        assertFalse("Negative flag should not be set when result is positive", isNegativeFlag());
    }

    @Test
    public void testCarryFlagOnByteOverflow() {
        assertTrue(executeProgram("SEG\nMOVE B I 200, R1\nADD B I 100, R1\nHALT"));
        assertTrue("Carry flag should be set on byte overflow", isCarryFlag());
    }
}
