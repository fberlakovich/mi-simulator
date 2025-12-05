package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the ADD instruction.
 *
 * ADD computes: op1 + op2 -> result
 * 2-address form: ADD size op1, op2  (result in op2)
 * 3-address form: ADD size op1, op2, op3 (result in op3)
 *
 * Opcodes:
 * 2-addr: BF (B), C0 (H), C1 (W), C2 (F), C3 (D)
 * 3-addr: C4 (B), C5 (H), C6 (W), C7 (F), C8 (D)
 */
public class AddTest extends InstructionTestBase {

    // ==================== Word (32-bit) Integer Tests ====================

    @Test
    public void add_word_twoAddress_positiveNumbers() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 20, R1",
                "ADD W R0, R1"  // R1 = R0 + R1 = 10 + 20 = 30
        ));
        assertEquals(10, getRegister(0));
        assertEquals(30, getRegister(1));
    }

    @Test
    public void add_word_threeAddress_positiveNumbers() {
        assembleAndRun(program(
                "MOVE W I 10, R0",
                "MOVE W I 20, R1",
                "ADD W R0, R1, R2"  // R2 = R0 + R1 = 10 + 20 = 30
        ));
        assertEquals(10, getRegister(0));
        assertEquals(20, getRegister(1));
        assertEquals(30, getRegister(2));
    }

    @Test
    public void add_word_negativeNumbers() {
        assembleAndRun(program(
                "MOVE W I -10, R0",
                "MOVE W I -20, R1",
                "ADD W R0, R1, R2"  // R2 = -10 + -20 = -30
        ));
        assertEquals(-30, getRegister(2));
        assertTrue("Negative flag should be set", isNegativeFlag());
    }

    @Test
    public void add_word_mixedSigns() {
        assembleAndRun(program(
                "MOVE W I 100, R0",
                "MOVE W I -30, R1",
                "ADD W R0, R1, R2"  // R2 = 100 + -30 = 70
        ));
        assertEquals(70, getRegister(2));
        assertFalse("Negative flag should be clear", isNegativeFlag());
    }

    @Test
    public void add_word_resultIsZero() {
        assembleAndRun(program(
                "MOVE W I 42, R0",
                "MOVE W I -42, R1",
                "ADD W R0, R1, R2"  // R2 = 42 + -42 = 0
        ));
        assertEquals(0, getRegister(2));
        assertTrue("Zero flag should be set", isZeroFlag());
    }

    @Test
    public void add_word_overflow_positiveToNegative() {
        // Adding two positive numbers that produce a negative result = signed overflow
        // Standard CPU semantics: V flag set when sign of result differs from expected
        assembleAndRun(program(
                "MOVE W I 2147483647, R0",  // Integer.MAX_VALUE
                "MOVE W I 1, R1",
                "ADD W R0, R1, R2"
        ));
        assertEquals(-2147483648, getRegister(2));  // Wraps to MIN_VALUE
        assertTrue("Negative flag should be set", isNegativeFlag());
        assertTrue("Overflow flag should be set (positive + positive = negative)", isOverflowFlag());
    }

    @Test
    public void add_word_overflow_negativeToPositive() {
        // Adding two negative numbers that produce a positive result = signed overflow
        assembleAndRun(program(
                "MOVE W I -2147483648, R0",  // Integer.MIN_VALUE
                "MOVE W I -1, R1",
                "ADD W R0, R1, R2"
        ));
        assertEquals(2147483647, getRegister(2));  // Wraps to MAX_VALUE
        assertFalse("Negative flag should be clear", isNegativeFlag());
        assertTrue("Overflow flag should be set (negative + negative = positive)", isOverflowFlag());
    }

    @Test
    public void add_word_noOverflow_mixedSigns() {
        // Adding positive + negative never overflows (can't exceed range)
        assembleAndRun(program(
                "MOVE W I 2147483647, R0",  // MAX_VALUE
                "MOVE W I -1, R1",
                "ADD W R0, R1, R2"
        ));
        assertEquals(2147483646, getRegister(2));
        assertFalse("Overflow flag should be clear (mixed signs can't overflow)", isOverflowFlag());
    }

    @Test
    public void add_word_withImmediate() {
        assembleAndRun(program(
                "MOVE W I 100, R0",
                "ADD W I 50, R0"  // R0 = 100 + 50 = 150
        ));
        assertEquals(150, getRegister(0));
    }

    // ==================== Byte (8-bit) Integer Tests ====================

    @Test
    public void add_byte_simple() {
        assembleAndRun(program(
                "MOVE B I 10, R0",
                "MOVE B I 20, R1",
                "ADD B R0, R1, R2"
        ));
        assertEquals(30, getRegister(2));
    }

    @Test
    public void add_byte_overflow_positiveToNegative() {
        // 127 + 1 = -128 in signed byte, overflow should be set
        assembleAndRun(program(
                "MOVE B I 127, R0",
                "MOVE B I 1, R1",
                "ADD B R0, R1, R2"
        ));
        assertTrue("Overflow flag should be set (positive + positive = negative)", isOverflowFlag());
    }

    @Test
    public void add_byte_unsignedOverflow_setsCarry() {
        // 255 + 1 = 256 which sets carry (overflows unsigned byte)
        assembleAndRun(program(
                "MOVE B I 255, R0",
                "MOVE B I 1, R1",
                "ADD B R0, R1, R2"
        ));
        assertTrue("Carry flag should be set", isCarryFlag());
    }

    // ==================== Half-word (16-bit) Integer Tests ====================

    @Test
    public void add_halfword_simple() {
        assembleAndRun(program(
                "MOVE H I 1000, R0",
                "MOVE H I 2000, R1",
                "ADD H R0, R1, R2"
        ));
        assertEquals(3000, getRegister(2));
    }

    @Test
    public void add_halfword_overflow_positiveToNegative() {
        // 32767 + 1 = -32768 in signed half-word, overflow should be set
        assembleAndRun(program(
                "MOVE H I 32767, R0",
                "MOVE H I 1, R1",
                "ADD H R0, R1, R2"
        ));
        assertTrue("Overflow flag should be set (positive + positive = negative)", isOverflowFlag());
    }

    @Test
    public void add_halfword_unsignedOverflow_setsCarry() {
        // 65535 + 1 = 65536 which sets carry
        assembleAndRun(program(
                "MOVE H I 65535, R0",
                "MOVE H I 1, R1",
                "ADD H R0, R1, R2"
        ));
        assertTrue("Carry flag should be set", isCarryFlag());
    }

    // ==================== Float (32-bit) Tests ====================

    @Test
    public void add_float_simple() {
        assembleAndRun(program(
                "MOVE F I 1.5, R0",
                "MOVE F I 2.5, R1",
                "ADD F R0, R1, R2"
        ));
        assertEquals(4.0f, getRegisterAsFloat(2), 0.0001f);
    }

    @Test
    public void add_float_negative() {
        assembleAndRun(program(
                "MOVE F I 10.0, R0",
                "MOVE F I -3.5, R1",
                "ADD F R0, R1, R2"
        ));
        assertEquals(6.5f, getRegisterAsFloat(2), 0.0001f);
    }

    @Test
    public void add_float_resultZero() {
        assembleAndRun(program(
                "MOVE F I 5.5, R0",
                "MOVE F I -5.5, R1",
                "ADD F R0, R1, R2"
        ));
        assertEquals(0.0f, getRegisterAsFloat(2), 0.0001f);
        assertTrue("Zero flag should be set", isZeroFlag());
    }

    // ==================== Double (64-bit) Tests ====================

    @Test
    public void add_double_simple() {
        assembleAndRun(program(
                "MOVE D I 1.5, R0",
                "MOVE D I 2.5, R2",
                "ADD D R0, R2, R4"  // Doubles use register pairs
        ));
        assertEquals(4.0, getRegisterAsDouble(4), 0.0001);
    }

    // ==================== Carry Flag Tests ====================

    @Test
    public void add_word_carriesSet() {
        // Adding two large unsigned numbers that would set carry
        assembleAndRun(program(
                "MOVE W I -1, R0",  // 0xFFFFFFFF
                "MOVE W I 1, R1",
                "ADD W R0, R1, R2"
        ));
        // Should produce carry (overflow in unsigned sense)
        assertTrue("Carry flag should be set", isCarryFlag());
    }
}
