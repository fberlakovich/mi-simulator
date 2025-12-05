package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the MOVE instruction.
 */
public class MoveTest extends InstructionTestBase {

    @Test
    public void move_word_immediateToRegister() {
        assembleAndRun(program(
                "MOVE W I 12345, R0"
        ));
        assertEquals(12345, getRegister(0));
    }

    @Test
    public void move_word_registerToRegister() {
        assembleAndRun(program(
                "MOVE W I 42, R0",
                "MOVE W R0, R1"
        ));
        assertEquals(42, getRegister(0));
        assertEquals(42, getRegister(1));
    }

    @Test
    public void move_word_negative() {
        assembleAndRun(program(
                "MOVE W I -100, R0"
        ));
        assertEquals(-100, getRegister(0));
        assertTrue("Negative flag should be set", isNegativeFlag());
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertFalse("Overflow flag should be clear", isOverflowFlag());
    }

    @Test
    public void move_word_zero() {
        assembleAndRun(program(
                "MOVE W I 0, R0"
        ));
        assertEquals(0, getRegister(0));
        assertTrue("Zero flag should be set", isZeroFlag());
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    @Test
    public void move_float_simple() {
        assembleAndRun(program(
                "MOVE F I 3.14, R0"
        ));
        assertEquals(3.14f, getRegisterAsFloat(0), 0.0001f);
    }
}
