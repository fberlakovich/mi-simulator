package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

/**
 * Helper class for decoding operands from machine code.
 * Extracts complexity from Operand.decode() to reduce cyclomatic complexity.
 */
public final class OperandDecoder {

    // Opcode range constants
    private static final int IMMEDIATE_MAX = 63;
    private static final int IMMEDIATE_FULL = 143;
    private static final int ABSOLUTE = 159;

    private static final int INDEX_MIN = 64;
    private static final int INDEX_MAX = 79;

    private static final int REGISTER_MIN = 80;
    private static final int REGISTER_MAX = 95;

    private static final int REL_NO_OFFSET_MIN = 96;
    private static final int REL_NO_OFFSET_MAX = 111;

    private static final int STACK_PRE_DEC_MIN = 112;
    private static final int STACK_PRE_DEC_MAX = 126;

    private static final int STACK_POST_INC_MIN = 128;
    private static final int STACK_POST_INC_MAX = 142;

    private static final int REL_BYTE_MIN = 160;
    private static final int REL_BYTE_MAX = 175;

    private static final int IND_BYTE_MIN = 176;
    private static final int IND_BYTE_MAX = 191;

    private static final int REL_HALF_MIN = 192;
    private static final int REL_HALF_MAX = 207;

    private static final int IND_HALF_MIN = 208;
    private static final int IND_HALF_MAX = 223;

    private static final int REL_WORD_MIN = 224;
    private static final int REL_WORD_MAX = 239;

    private static final int IND_WORD_MIN = 240;
    private static final int IND_WORD_MAX = 255;

    private OperandDecoder() {
        // Utility class
    }

    /**
     * Decodes an operand from memory at the current PC position.
     */
    public static Operand decode(Machine machine, int length) throws OpcodeDecodeException {
        int opcode = machine.fetchByte();

        // Immediate addressing (small value 0-63)
        if (opcode <= IMMEDIATE_MAX) {
            return new ImmediateOperand(machine, NumberConversion.intToByte(opcode, length), length);
        }

        // Immediate addressing (full value)
        if (opcode == IMMEDIATE_FULL) {
            return decodeImmediateFull(machine, length);
        }

        // Absolute addressing
        if (opcode == ABSOLUTE) {
            return decodeAbsolute(machine, length);
        }

        // Indexed addressing (64-79)
        if (inRange(opcode, INDEX_MIN, INDEX_MAX)) {
            return decodeIndexed(machine, opcode - INDEX_MIN, length);
        }

        // Register addressing (80-95)
        if (inRange(opcode, REGISTER_MIN, REGISTER_MAX)) {
            return new RegisterAddressing(machine, opcode - REGISTER_MIN, length);
        }

        // Relative addressing without offset (96-111)
        if (inRange(opcode, REL_NO_OFFSET_MIN, REL_NO_OFFSET_MAX)) {
            return new RelAddressing(machine, 0, opcode - REL_NO_OFFSET_MIN, length, machine.getPC());
        }

        // Stack addressing pre-decrement (112-126)
        if (inRange(opcode, STACK_PRE_DEC_MIN, STACK_PRE_DEC_MAX)) {
            return new CellarAddressing(machine, opcode - STACK_PRE_DEC_MIN, length, -length, false);
        }

        // Stack addressing post-increment (128-142)
        if (inRange(opcode, STACK_POST_INC_MIN, STACK_POST_INC_MAX)) {
            return new CellarAddressing(machine, opcode - STACK_POST_INC_MIN, length, length, true);
        }

        // Try relative/indirect with offset
        Operand result = tryDecodeRelativeOrIndirect(machine, opcode, length);
        if (result != null) {
            return result;
        }

        throw new OpcodeDecodeException("Invalid operand opcode: " + opcode, machine.getPC());
    }

    private static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private static Operand decodeImmediateFull(Machine machine, int length) {
        machine.addToPC(length);
        return new ImmediateOperand(machine,
                machine.getMemory().getContent(machine.getPC() - length, length),
                length);
    }

    private static Operand decodeAbsolute(Machine machine, int length) {
        machine.addToPC(4);
        return new AbsAddress(machine,
                NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 4, 4)),
                length, machine.getPC() - 4);
    }

    /**
     * Decodes indexed addressing modes (opcode 64-79).
     */
    private static Operand decodeIndexed(Machine machine, int index, int length)
            throws OpcodeDecodeException {
        int opcode = machine.fetchByte();

        // Relative addressing without offset
        if (inRange(opcode, REL_NO_OFFSET_MIN, REL_NO_OFFSET_MAX)) {
            return new RelAddressing(machine, 0, opcode - REL_NO_OFFSET_MIN, index, length, machine.getPC());
        }

        // Try relative/indirect with offset
        Operand result = tryDecodeRelativeOrIndirectIndexed(machine, opcode, index, length);
        if (result != null) {
            return result;
        }

        throw new OpcodeDecodeException("Invalid indexed operand opcode: " + opcode, machine.getPC());
    }

    /**
     * Tries to decode relative or indirect addressing with offset.
     * Returns null if opcode doesn't match any known pattern.
     */
    private static Operand tryDecodeRelativeOrIndirect(Machine machine, int opcode, int length) {
        // Relative with byte offset (160-175)
        if (inRange(opcode, REL_BYTE_MIN, REL_BYTE_MAX)) {
            return decodeRelativeWithOffset(machine, opcode - REL_BYTE_MIN, 1, length);
        }

        // Indirect with byte offset (176-191)
        if (inRange(opcode, IND_BYTE_MIN, IND_BYTE_MAX)) {
            return decodeIndirectWithOffset(machine, opcode - IND_BYTE_MIN, 1, length);
        }

        // Relative with halfword offset (192-207)
        if (inRange(opcode, REL_HALF_MIN, REL_HALF_MAX)) {
            return decodeRelativeWithOffset(machine, opcode - REL_HALF_MIN, 2, length);
        }

        // Indirect with halfword offset (208-223)
        if (inRange(opcode, IND_HALF_MIN, IND_HALF_MAX)) {
            return decodeIndirectWithOffset(machine, opcode - IND_HALF_MIN, 2, length);
        }

        // Relative with word offset (224-239)
        if (inRange(opcode, REL_WORD_MIN, REL_WORD_MAX)) {
            return decodeRelativeWithOffset(machine, opcode - REL_WORD_MIN, 4, length);
        }

        // Indirect with word offset (240-255)
        if (inRange(opcode, IND_WORD_MIN, IND_WORD_MAX)) {
            return decodeIndirectWithOffset(machine, opcode - IND_WORD_MIN, 4, length);
        }

        return null;
    }

    /**
     * Tries to decode relative or indirect addressing with offset for indexed mode.
     */
    private static Operand tryDecodeRelativeOrIndirectIndexed(Machine machine, int opcode,
                                                               int index, int length) {
        // Relative with byte offset (160-175)
        if (inRange(opcode, REL_BYTE_MIN, REL_BYTE_MAX)) {
            return decodeRelativeWithOffsetIndexed(machine, opcode - REL_BYTE_MIN, index, 1, length);
        }

        // Indirect with byte offset (176-191)
        if (inRange(opcode, IND_BYTE_MIN, IND_BYTE_MAX)) {
            return decodeIndirectWithOffsetIndexed(machine, opcode - IND_BYTE_MIN, index, 1, length);
        }

        // Relative with halfword offset (192-207)
        if (inRange(opcode, REL_HALF_MIN, REL_HALF_MAX)) {
            return decodeRelativeWithOffsetIndexed(machine, opcode - REL_HALF_MIN, index, 2, length);
        }

        // Indirect with halfword offset (208-223)
        if (inRange(opcode, IND_HALF_MIN, IND_HALF_MAX)) {
            return decodeIndirectWithOffsetIndexed(machine, opcode - IND_HALF_MIN, index, 2, length);
        }

        // Relative with word offset (224-239)
        if (inRange(opcode, REL_WORD_MIN, REL_WORD_MAX)) {
            return decodeRelativeWithOffsetIndexed(machine, opcode - REL_WORD_MIN, index, 4, length);
        }

        // Indirect with word offset (240-255)
        if (inRange(opcode, IND_WORD_MIN, IND_WORD_MAX)) {
            return decodeIndirectWithOffsetIndexed(machine, opcode - IND_WORD_MIN, index, 4, length);
        }

        return null;
    }

    /**
     * Reads an offset value from memory at the current PC and advances the PC.
     */
    private static int readOffset(Machine machine, int offsetSize) {
        machine.addToPC(offsetSize);
        return NumberConversion.myBytetoIntWithSign(
                machine.getMemory().getContent(machine.getPC() - offsetSize, offsetSize));
    }

    private static Operand decodeRelativeWithOffset(Machine machine, int reg, int offsetSize, int length) {
        int offset = readOffset(machine, offsetSize);
        return new RelAddressing(machine, offset, reg, length, machine.getPC() - offsetSize);
    }

    private static Operand decodeIndirectWithOffset(Machine machine, int reg, int offsetSize, int length) {
        int offset = readOffset(machine, offsetSize);
        return new IndAddressing(machine, offset, reg, length, machine.getPC() - offsetSize);
    }

    private static Operand decodeRelativeWithOffsetIndexed(Machine machine, int reg, int index,
                                                            int offsetSize, int length) {
        int offset = readOffset(machine, offsetSize);
        return new RelAddressing(machine, offset, reg, index, length, machine.getPC() - offsetSize);
    }

    private static Operand decodeIndirectWithOffsetIndexed(Machine machine, int reg, int index,
                                                            int offsetSize, int length) {
        int offset = readOffset(machine, offsetSize);
        return new IndAddressing(machine, offset, reg, index, length, machine.getPC() - offsetSize);
    }
}
