package simulator;

import org.junit.Test;
import static org.junit.Assert.*;

public class CmpOperationTest extends MIOperationTestBase {

    @Test
    public void testCmpEqual() {
        String program = """
            SEG
            MOVE W I 42, R1
            CMP W I 42, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        assertTrue("Zero flag should be set when values are equal", isZeroFlag());
        assertFalse("Negative flag should not be set", isNegativeFlag());
    }

    @Test
    public void testCmpByte() {
        String program = """
            SEG
            MOVE B I 100, R1
            CMP B I 50, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        // CMP should not modify the register
        assertEquals(100, getRegisterByte(1));
    }
}
