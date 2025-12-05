package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the SUB instruction.
 *
 * SUB computes: op2 - op1 -> result
 * 2-address form: SUB size op1, op2  (result in op2)
 * 3-address form: SUB size op1, op2, op3 (result in op3)
 */
public class SubTest extends InstructionTestBase {

    // ==================== Word (32-bit) Integer Tests ====================

    @Test
    public void sub_word_twoAddress_positiveNumbers() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 30, R1",
                "SUB W R0, R1"  // R1 = R1 - R0 = 30 - 10 = 20
        ));
        assertEquals(10, getRegister(0));
        assertEquals(20, getRegister(1));
    }

    @Test
    public void sub_word_threeAddress_positiveNumbers() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 30, R1",
                "SUB W R0, R1, R2"  // R2 = R1 - R0 = 30 - 10 = 20
        ));
        assertEquals(10, getRegister(0));
        assertEquals(30, getRegister(1));
        assertEquals(20, getRegister(2));
    }

    @Test
    public void sub_word_negativeResult() {
        assembleAndRun(program(
                "MOVE W I 30, R0",
                "MOVE W I 10, R1",
                "SUB W R0, R1, R2"  // R2 = 10 - 30 = -20
        ));
        assertEquals(-20, getRegister(2));
        assertTrue("Negative flag should be set", isNegativeFlag());
    }

    @Test
    public void sub_word_resultIsZero() {
        assembleAndRun(program(
                "MOVE W I 42, R0",
                "MOVE W I 42, R1",
                "SUB W R0, R1, R2"  // R2 = 42 - 42 = 0
        ));
        assertEquals(0, getRegister(2));
        assertTrue("Zero flag should be set", isZeroFlag());
    }

    @Test
    public void sub_word_overflow() {
        // MIN_VALUE - 1 should overflow to MAX_VALUE
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "MOVE W I -2147483648, R1", // MIN_VALUE
                "SUB W R0, R1, R2"
        ));
        assertEquals(2147483647, getRegister(2));
        assertTrue("Overflow flag should be set", isOverflowFlag());
    }

    // ==================== Byte (8-bit) Tests ====================

    @Test
    public void sub_byte_simple() {
        assembleAndRun(program(
                "MOVE B I 10, R0",
                "MOVE B I 30, R1",
                "SUB B R0, R1, R2"
        ));
        assertEquals(20, getRegister(2));
    }

    // ==================== Float (32-bit) Tests ====================

    @Test
    public void sub_float_simple() {
        assembleAndRun(program(
                "MOVE F I 1.5, R0",
                "MOVE F I 4.0, R1",
                "SUB F R0, R1, R2" // 4.0 - 1.5 = 2.5
        ));
        assertEquals(2.5f, getRegisterAsFloat(2), 0.0001f);
    }
}
