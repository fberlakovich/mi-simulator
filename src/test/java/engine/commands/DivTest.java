package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the DIV instruction.
 *
 * DIV computes: op2 / op1 -> result (Integer division) or Float division
 * Flags:
 *  C: 0 (cleared)
 *  V: Set if overflow (e.g. MIN_VALUE / -1) or divide by zero? 
 *     Manual says for Arith Alarm: Typ=2 (Div by Zero), Typ=4 (Float Div by Zero).
 *     V flag manual says: "Overflow Condition (bei arithmetischen Operationen)".
 *     But for Div by Zero, it usually triggers an interrupt.
 *  Z: Set if result is zero
 *  N: Set if result is negative
 */
public class DivTest extends InstructionTestBase {

    // ==================== Word (32-bit) Integer Tests ====================

    @Test
    public void div_word_positive() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 200, R1",
                "DIV W R0, R1"  // R1 = 200 / 10 = 20
        ));
        assertEquals(20, getRegister(1));
        assertFalse("Carry flag should be clear", isCarryFlag());
        assertFalse("Overflow flag should be clear", isOverflowFlag());
        assertFalse("Zero flag should be clear", isZeroFlag());
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    @Test
    public void div_word_negativeResult() {
        assembleAndRun(program(
                "MOVE W I -10, R0",
                "MOVE W I 200, R1",
                "DIV W R0, R1"  // R1 = 200 / -10 = -20
        ));
        assertEquals(-20, getRegister(1));
        assertTrue("Negative flag should be set", isNegativeFlag());
        assertFalse("Carry flag should be clear", isCarryFlag());
    }

    @Test
    public void div_word_resultZero() {
        assembleAndRun(program(
                "MOVE W I 100, R0",
                "MOVE W I 10, R1",
                "DIV W R0, R1"  // R1 = 10 / 100 = 0 (integer division)
        ));
        assertEquals(0, getRegister(1));
        assertTrue("Zero flag should be set", isZeroFlag());
    }

    // ==================== Float (32-bit) Tests ====================

    @Test
    public void div_float_simple() {
        assembleAndRun(program(
                "MOVE F I 2.0, R0",
                "MOVE F I 5.0, R1",
                "DIV F R0, R1" // R1 = 5.0 / 2.0 = 2.5
        ));
        assertEquals(2.5f, getRegisterAsFloat(1), 0.0001f);
        assertFalse("Carry flag should be clear", isCarryFlag());
    }
}
