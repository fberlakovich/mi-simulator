package simulator;

import org.junit.Test;
import static org.junit.Assert.*;

public class JumpOperationTest extends MIOperationTestBase {

    @Test
    public void testUnconditionalJump() {
        assertTrue(executeProgram("SEG\nMOVE W I 1, R1\nJUMP skip\nMOVE W I 99, R1\nskip:\nMOVE W I 2, R2\nHALT"));
        assertEquals(1, getRegisterWord(1));
        assertEquals(2, getRegisterWord(2));
    }

    @Test
    public void testJumpEqualWhenEqual() {
        assertTrue(executeProgram("SEG\nMOVE W I 5, R1\nCMP W I 5, R1\nJEQ equal\nMOVE W I 1, R2\nJUMP end\nequal:\nMOVE W I 2, R2\nend:\nHALT"));
        assertEquals(2, getRegisterWord(2));
    }

    @Test
    public void testJumpEqualWhenNotEqual() {
        assertTrue(executeProgram("SEG\nMOVE W I 5, R1\nCMP W I 10, R1\nJEQ equal\nMOVE W I 1, R2\nJUMP end\nequal:\nMOVE W I 2, R2\nend:\nHALT"));
        assertEquals(1, getRegisterWord(2));
    }
}
