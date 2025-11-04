package simulator;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoveOperationTest extends MIOperationTestBase {

    @Test
    public void testMoveImmediateByte() {
        assertTrue(executeProgram("SEG\nMOVE B I 42, R1\nHALT"));
        assertEquals(42, getRegisterByte(1));
    }

    @Test
    public void testMoveImmediateWord() {
        assertTrue(executeProgram("SEG\nMOVE W I 12345, R2\nHALT"));
        assertEquals(12345, getRegisterWord(2));
    }

    @Test
    public void testMoveRegisterToRegister() {
        assertTrue(executeProgram("SEG\nMOVE W I 999, R1\nMOVE W R1, R2\nHALT"));
        assertEquals(999, getRegisterWord(1));
        assertEquals(999, getRegisterWord(2));
    }

    @Test
    public void testMoveNegativeNumber() {
        assertTrue(executeProgram("SEG\nMOVE W I -100, R3\nHALT"));
        assertEquals(-100, getRegisterWord(3));
    }

    @Test
    public void testMoveHalfword() {
        assertTrue(executeProgram("SEG\nMOVE H I 1000, R4\nHALT"));
        assertEquals(1000, getRegisterValue(4, 2));
    }

    @Test
    public void testMoveZero() {
        assertTrue(executeProgram("SEG\nMOVE W I 123, R5\nMOVE W I 0, R5\nHALT"));
        assertEquals(0, getRegisterWord(5));
    }
}
