package engine.commands;

import engine.Machine;

/**
 * Enumeration of all MI machine opcodes with their metadata.
 *
 * Based on MI Manual Appendix B (Tables B.1-B.5, pages 89-93).
 *
 * Each opcode has:
 * - code: The byte value of the opcode (0x00-0xFF)
 * - length: Data type length in bytes (1=Byte, 2=Halfword, 4=Word/Float, 8=Double)
 * - operandCount: Number of operands (0-4)
 * - floating: Whether this is a floating-point operation
 * - decoder: Reference to the Command class's decode method
 */
public enum Opcode {

    // === Privileged instructions (Table B.1, page 89) ===
    // HALT is the only privileged instruction implemented in the simulator
    HALT(0x00, 0, 0, false, Halt::decode),

    // === Non-privileged instructions (Tables B.2-B.5, pages 90-93) ===

    // CMP - Compare (Table B.2, page 90)
    CMP_B(0x92, 1, 2, false, Cmp::decode),
    CMP_H(0x93, 2, 2, false, Cmp::decode),
    CMP_W(0x94, 4, 2, false, Cmp::decode),
    CMP_F(0x95, 4, 2, true, Cmp::decode),
    CMP_D(0x96, 8, 2, true, Cmp::decode),

    // JV, JNV - Jump on overflow (Table B.2, page 90)
    JV(0x97, 4, 1, false, Jump::decode),
    JNV(0x98, 4, 1, false, Jump::decode),

    // CLEAR - Clear operand (Table B.2, page 90)
    CLEAR_B(0x99, 1, 1, false, Clear::decode),
    CLEAR_H(0x9A, 2, 1, false, Clear::decode),
    CLEAR_W(0x9B, 4, 1, false, Clear::decode),
    CLEAR_F(0x9C, 4, 1, true, Clear::decode),
    CLEAR_D(0x9D, 8, 1, true, Clear::decode),

    // MOVE - Move data (Table B.2, page 90)
    MOVE_B(0x9E, 1, 2, false, Move::decode),
    MOVE_H(0x9F, 2, 2, false, Move::decode),
    MOVE_W(0xA0, 4, 2, false, Move::decode),
    MOVE_F(0xA1, 4, 2, true, Move::decode),
    MOVE_D(0xA2, 8, 2, true, Move::decode),

    // MOVEN - Move with negation (Table B.2, page 90)
    MOVEN_B(0xA3, 1, 2, false, MoveN::decode),
    MOVEN_H(0xA4, 2, 2, false, MoveN::decode),
    MOVEN_W(0xA5, 4, 2, false, MoveN::decode),
    MOVEN_F(0xA6, 4, 2, true, MoveN::decode),
    MOVEN_D(0xA7, 8, 2, true, MoveN::decode),

    // MOVEC - Move with complement (Table B.2, page 90)
    MOVEC_B(0xA8, 1, 2, false, MoveC::decode),
    MOVEC_H(0xA9, 2, 2, false, MoveC::decode),
    MOVEC_W(0xAA, 4, 2, false, MoveC::decode),

    // MOVEA - Move address (Table B.2, page 90)
    MOVEA(0xAB, 4, 2, false, MoveA::decode),

    // CONV - Convert (Table B.2, page 90)
    CONV(0xAC, 1, 2, false, Conv::decode),

    // OR - Bitwise OR (Table B.2, page 90)
    OR_B_2(0xAD, 1, 2, false, Or::decode),
    OR_H_2(0xAE, 2, 2, false, Or::decode),
    OR_W_2(0xAF, 4, 2, false, Or::decode),
    OR_B_3(0xB0, 1, 3, false, Or::decode),
    OR_H_3(0xB1, 2, 3, false, Or::decode),
    OR_W_3(0xB2, 4, 3, false, Or::decode),

    // ANDNOT - Bitwise AND NOT (Table B.3, page 91)
    ANDNOT_B_2(0xB3, 1, 2, false, AndNot::decode),
    ANDNOT_H_2(0xB4, 2, 2, false, AndNot::decode),
    ANDNOT_W_2(0xB5, 4, 2, false, AndNot::decode),
    ANDNOT_B_3(0xB6, 1, 3, false, AndNot::decode),
    ANDNOT_H_3(0xB7, 2, 3, false, AndNot::decode),
    ANDNOT_W_3(0xB8, 4, 3, false, AndNot::decode),

    // XOR - Bitwise XOR (Table B.3, page 91)
    XOR_B_2(0xB9, 1, 2, false, Xor::decode),
    XOR_H_2(0xBA, 2, 2, false, Xor::decode),
    XOR_W_2(0xBB, 4, 2, false, Xor::decode),
    XOR_B_3(0xBC, 1, 3, false, Xor::decode),
    XOR_H_3(0xBD, 2, 3, false, Xor::decode),
    XOR_W_3(0xBE, 4, 3, false, Xor::decode),

    // ADD - Addition (Table B.3, page 91)
    ADD_B_2(0xBF, 1, 2, false, Add::decode),
    ADD_H_2(0xC0, 2, 2, false, Add::decode),
    ADD_W_2(0xC1, 4, 2, false, Add::decode),
    ADD_F_2(0xC2, 4, 2, true, Add::decode),
    ADD_D_2(0xC3, 8, 2, true, Add::decode),
    ADD_B_3(0xC4, 1, 3, false, Add::decode),
    ADD_H_3(0xC5, 2, 3, false, Add::decode),
    ADD_W_3(0xC6, 4, 3, false, Add::decode),
    ADD_F_3(0xC7, 4, 3, true, Add::decode),
    ADD_D_3(0xC8, 8, 3, true, Add::decode),

    // SUB - Subtraction (Table B.3, page 91)
    SUB_B_2(0xC9, 1, 2, false, Sub::decode),
    SUB_H_2(0xCA, 2, 2, false, Sub::decode),
    SUB_W_2(0xCB, 4, 2, false, Sub::decode),
    SUB_F_2(0xCC, 4, 2, true, Sub::decode),
    SUB_D_2(0xCD, 8, 2, true, Sub::decode),
    SUB_B_3(0xCE, 1, 3, false, Sub::decode),
    SUB_H_3(0xCF, 2, 3, false, Sub::decode),
    SUB_W_3(0xD0, 4, 3, false, Sub::decode),
    SUB_F_3(0xD1, 4, 3, true, Sub::decode),
    SUB_D_3(0xD2, 8, 3, true, Sub::decode),

    // MULT - Multiplication (Table B.4, page 92)
    MULT_B_2(0xD3, 1, 2, false, Mult::decode),
    MULT_H_2(0xD4, 2, 2, false, Mult::decode),
    MULT_W_2(0xD5, 4, 2, false, Mult::decode),
    MULT_F_2(0xD6, 4, 2, true, Mult::decode),
    MULT_D_2(0xD7, 8, 2, true, Mult::decode),
    MULT_B_3(0xD8, 1, 3, false, Mult::decode),
    MULT_H_3(0xD9, 2, 3, false, Mult::decode),
    MULT_W_3(0xDA, 4, 3, false, Mult::decode),
    MULT_F_3(0xDB, 4, 3, true, Mult::decode),
    MULT_D_3(0xDC, 8, 3, true, Mult::decode),

    // DIV - Division (Table B.4, page 92)
    DIV_B_2(0xDD, 1, 2, false, Div::decode),
    DIV_H_2(0xDE, 2, 2, false, Div::decode),
    DIV_W_2(0xDF, 4, 2, false, Div::decode),
    DIV_F_2(0xE0, 4, 2, true, Div::decode),
    DIV_D_2(0xE1, 8, 2, true, Div::decode),
    DIV_B_3(0xE2, 1, 3, false, Div::decode),
    DIV_H_3(0xE3, 2, 3, false, Div::decode),
    DIV_W_3(0xE4, 4, 3, false, Div::decode),
    DIV_F_3(0xE5, 4, 3, true, Div::decode),
    DIV_D_3(0xE6, 8, 3, true, Div::decode),

    // SH - Shift (Table B.4, page 92)
    SH(0xE7, 4, 3, false, Sh::decode),

    // ROT - Rotate (Table B.4, page 92)
    ROT(0xE8, 4, 3, false, Rot::decode),

    // Conditional jumps (Table B.4, page 92)
    JEQ(0xE9, 4, 1, false, Jump::decode),
    JNE(0xEA, 4, 1, false, Jump::decode),
    JGT(0xEB, 4, 1, false, Jump::decode),
    JGE(0xEC, 4, 1, false, Jump::decode),
    JLT(0xED, 4, 1, false, Jump::decode),
    JLE(0xEE, 4, 1, false, Jump::decode),
    JC(0xEF, 4, 1, false, Jump::decode),
    JNC(0xF0, 4, 1, false, Jump::decode),

    // JUMP - Unconditional jump (Table B.4, page 92)
    JUMP(0xF1, 4, 1, false, Jump::decode),

    // CALL - Call subroutine (Table B.4, page 92)
    CALL(0xF2, 4, 1, false, Call::decode),

    // RET - Return from subroutine (Table B.4, page 92)
    RET(0xF3, 0, 0, false, Ret::decode),

    // PUSHR - Push registers (Table B.4, page 92)
    PUSHR(0xF4, 4, 0, false, Pushr::decode),

    // POPR - Pop registers (Table B.4, page 92)
    POPR(0xF5, 0, 0, false, Popr::decode),

    // EXTS - Extract signed (Table B.5, page 93)
    EXTS(0xF6, 4, 4, false, Exts::decode),

    // EXT - Extract unsigned (Table B.5, page 93)
    EXT(0xF7, 4, 4, false, Ext::decode),

    // INS - Insert (Table B.5, page 93)
    INS(0xF8, 4, 4, false, Ins::decode),

    // FINDS - Find string (Table B.5, page 93)
    FINDS(0xF9, 4, 4, false, Finds::decode),

    // FINDC - Find character (Table B.5, page 93)
    FINDC(0xFA, 4, 4, false, Findc::decode),

    // JBSSI - Jump if bit set and set interlocked (Table B.5, page 93)
    JBSSI(0xFB, 4, 3, false, Jbssi::decode),

    // JBCCI - Jump if bit clear and clear interlocked (Table B.5, page 93)
    JBCCI(0xFC, 4, 3, false, Jbcci::decode);

    /** The opcode byte value */
    public final int code;

    /** Data type length in bytes (1=B, 2=H, 4=W/F, 8=D) */
    public final int length;

    /** Number of operands */
    public final int operandCount;

    /** Whether this is a floating-point operation */
    public final boolean floating;

    /** The decoder function that creates the Command */
    private final CommandDecoder decoder;

    /** Lookup table for O(1) opcode resolution */
    private static final Opcode[] BY_CODE = new Opcode[256];

    static {
        for (Opcode op : values()) {
            if (BY_CODE[op.code] != null) {
                throw new IllegalStateException("Duplicate opcode: " + op.code);
            }
            BY_CODE[op.code] = op;
        }
    }

    Opcode(int code, int length, int operandCount, boolean floating, CommandDecoder decoder) {
        this.code = code;
        this.length = length;
        this.operandCount = operandCount;
        this.floating = floating;
        this.decoder = decoder;
    }

    /**
     * Looks up an Opcode by its byte value.
     *
     * @param code the opcode byte value
     * @return the Opcode, or null if not found
     */
    public static Opcode fromCode(int code) {
        if (code < 0 || code > 255) {
            return null;
        }
        return BY_CODE[code];
    }

    /**
     * Decodes the next command from memory starting at the given PC.
     *
     * @param machine the machine to decode from
     * @param pc the program counter (address of the opcode byte)
     * @return the decoded Command
     * @throws OpcodeDecodeException if decoding fails
     */
    public Command decodeCommand(Machine machine, int pc) throws OpcodeDecodeException {
        return decoder.decode(machine, pc, this);
    }

    /**
     * Functional interface for command decoders.
     */
    @FunctionalInterface
    public interface CommandDecoder {
        Command decode(Machine machine, int pc, Opcode opcode) throws OpcodeDecodeException;
    }
}