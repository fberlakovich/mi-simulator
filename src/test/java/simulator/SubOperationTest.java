package simulator;

import org.junit.Test;
import static org.junit.Assert.*;

public class SubOperationTest extends MIOperationTestBase {

    @Test
    public void testSubByteImmediate() {
        String program = """
            SEG
            MOVE B I 10, R1
            MOVE B I 3, R2
            SUB B R2, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        assertEquals(7, getRegisterByte(1));
    }

    @Test
    public void testSubWordImmediate() {
        String program = """
            SEG
            MOVE W I 500, R1
            MOVE W I 200, R2
            SUB W R2, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        assertEquals(300, getRegisterWord(1));
    }

    @Test
    public void testSubThreeAddress() {
        String program = """
            SEG
            MOVE W I 100, R1
            MOVE W I 30, R2
            SUB W R2, R1, R3
            HALT
            """;

        assertTrue(executeProgram(program));
        assertEquals(70, getRegisterWord(3));
        assertEquals(100, getRegisterWord(1));
        assertEquals(30, getRegisterWord(2));
    }

    @Test
    public void testSubWithNegativeResult() {
        String program = """
            SEG
            MOVE W I 10, R1
            MOVE W I 20, R2
            SUB W R2, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        assertEquals(-10, getRegisterWord(1));
        assertTrue("Negative flag should be set", isNegativeFlag());
    }

    @Test
    public void testSubWithZeroResult() {
        String program = """
            SEG
            MOVE W I 42, R1
            MOVE W I 42, R2
            SUB W R2, R1
            HALT
            """;

        assertTrue(executeProgram(program));
        assertEquals(0, getRegisterWord(1));
        assertTrue("Zero flag should be set", isZeroFlag());
    }
}
