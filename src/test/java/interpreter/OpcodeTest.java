package interpreter;

import engine.commands.*;
import engine.Machine;
import engine.ProgramRunner;
import engine.state.MyByte;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Opcode enum to verify correct opcode metadata and decoding.
 *
 * References MI Manual Appendix B (Tables B.1-B.5, pages 89-93).
 *
 * The tests verify that:
 * 1. Each opcode has the correct metadata (code, length, operandCount, floating)
 * 2. Each opcode decodes to the correct Command type
 */
public class OpcodeTest {

    private ProgramRunner runner;

    @Before
    public void setUp() {
        Machine.resetInstance();
    }

    /**
     * Writes bytes to memory at the given address.
     */
    private void writeToMemory(int address, int... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            Machine.getInstance().getMemory().setContent(address + i, new MyByte[]{new MyByte(bytes[i])});
        }
    }

    /**
     * Sets up the PC and memory for decoding a command.
     */
    private void setupForDecode(int... bytes) {
        writeToMemory(0, bytes);
        Machine.getInstance().getRegisters().getRegister(15).setContentAsNumber(0);
        runner = Machine.getInstance().createRunner();
    }

    /**
     * Decodes the next command using the test runner.
     */
    private Command decodeNextCommand() {
        return runner.readNextCommand();
    }

    // ========================================================================
    // HALT - Table B.1, page 89
    // Privileged instruction, opcode 0x00
    // ========================================================================

    @Test
    public void testHaltMetadata() {
        // MI Manual Table B.1, page 89: HALT has opcode 0x00
        Opcode halt = Opcode.fromCode(0x00);
        assertNotNull("HALT opcode should exist", halt);
        assertEquals("HALT", halt.name());
        assertEquals(0x00, halt.code);
        assertEquals(0, halt.length);
        assertEquals(0, halt.operandCount);
        assertFalse(halt.floating);
    }

    @Test
    public void testHaltDecode() {
        setupForDecode(0x00);
        Command cmd = decodeNextCommand();
        assertTrue("HALT should decode to Halt", cmd instanceof Halt);
    }

    // ========================================================================
    // CMP - Table B.2, page 90
    // Opcodes 0x92-0x96: CMP B/H/W/F/D
    // ========================================================================

    @Test
    public void testCmpBMetadata() {
        // MI Manual Table B.2, page 90: CMP B has opcode 0x92
        Opcode op = Opcode.fromCode(0x92);
        assertNotNull("CMP_B opcode should exist", op);
        assertEquals(0x92, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testCmpHMetadata() {
        // MI Manual Table B.2, page 90: CMP H has opcode 0x93
        Opcode op = Opcode.fromCode(0x93);
        assertNotNull("CMP_H opcode should exist", op);
        assertEquals(0x93, op.code);
        assertEquals(2, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testCmpWMetadata() {
        // MI Manual Table B.2, page 90: CMP W has opcode 0x94
        Opcode op = Opcode.fromCode(0x94);
        assertNotNull("CMP_W opcode should exist", op);
        assertEquals(0x94, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testCmpFMetadata() {
        // MI Manual Table B.2, page 90: CMP F has opcode 0x95
        Opcode op = Opcode.fromCode(0x95);
        assertNotNull("CMP_F opcode should exist", op);
        assertEquals(0x95, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testCmpDMetadata() {
        // MI Manual Table B.2, page 90: CMP D has opcode 0x96
        Opcode op = Opcode.fromCode(0x96);
        assertNotNull("CMP_D opcode should exist", op);
        assertEquals(0x96, op.code);
        assertEquals(8, op.length);
        assertEquals(2, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testCmpDecode() {
        // CMP B R0, R1 (0x92, 0x50, 0x51)
        setupForDecode(0x92, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("CMP should decode to Cmp", cmd instanceof Cmp);
    }

    // ========================================================================
    // JV/JNV - Table B.2, page 90
    // Opcodes 0x97-0x98: JV, JNV
    // ========================================================================

    @Test
    public void testJvMetadata() {
        // MI Manual Table B.2, page 90: JV has opcode 0x97
        Opcode op = Opcode.fromCode(0x97);
        assertNotNull("JV opcode should exist", op);
        assertEquals(0x97, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testJnvMetadata() {
        // MI Manual Table B.2, page 90: JNV has opcode 0x98
        Opcode op = Opcode.fromCode(0x98);
        assertNotNull("JNV opcode should exist", op);
        assertEquals(0x98, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    // ========================================================================
    // CLEAR - Table B.2, page 90
    // Opcodes 0x99-0x9D: CLEAR B/H/W/F/D
    // ========================================================================

    @Test
    public void testClearBMetadata() {
        // MI Manual Table B.2, page 90: CLEAR B has opcode 0x99
        Opcode op = Opcode.fromCode(0x99);
        assertNotNull("CLEAR_B opcode should exist", op);
        assertEquals(0x99, op.code);
        assertEquals(1, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testClearHMetadata() {
        // MI Manual Table B.2, page 90: CLEAR H has opcode 0x9A
        Opcode op = Opcode.fromCode(0x9A);
        assertNotNull("CLEAR_H opcode should exist", op);
        assertEquals(0x9A, op.code);
        assertEquals(2, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testClearWMetadata() {
        // MI Manual Table B.2, page 90: CLEAR W has opcode 0x9B
        Opcode op = Opcode.fromCode(0x9B);
        assertNotNull("CLEAR_W opcode should exist", op);
        assertEquals(0x9B, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testClearFMetadata() {
        // MI Manual Table B.2, page 90: CLEAR F has opcode 0x9C
        Opcode op = Opcode.fromCode(0x9C);
        assertNotNull("CLEAR_F opcode should exist", op);
        assertEquals(0x9C, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testClearDMetadata() {
        // MI Manual Table B.2, page 90: CLEAR D has opcode 0x9D
        Opcode op = Opcode.fromCode(0x9D);
        assertNotNull("CLEAR_D opcode should exist", op);
        assertEquals(0x9D, op.code);
        assertEquals(8, op.length);
        assertEquals(1, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testClearDecode() {
        // CLEAR W R0 (0x9B, 0x50)
        setupForDecode(0x9B, 0x50);
        Command cmd = decodeNextCommand();
        assertTrue("CLEAR should decode to Clear", cmd instanceof Clear);
    }

    // ========================================================================
    // MOVE - Table B.2, page 90
    // Opcodes 0x9E-0xA2: MOVE B/H/W/F/D
    // ========================================================================

    @Test
    public void testMoveBMetadata() {
        // MI Manual Table B.2, page 90: MOVE B has opcode 0x9E
        Opcode op = Opcode.fromCode(0x9E);
        assertNotNull("MOVE_B opcode should exist", op);
        assertEquals(0x9E, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMoveHMetadata() {
        // MI Manual Table B.2, page 90: MOVE H has opcode 0x9F
        Opcode op = Opcode.fromCode(0x9F);
        assertNotNull("MOVE_H opcode should exist", op);
        assertEquals(0x9F, op.code);
        assertEquals(2, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMoveWMetadata() {
        // MI Manual Table B.2, page 90: MOVE W has opcode 0xA0
        Opcode op = Opcode.fromCode(0xA0);
        assertNotNull("MOVE_W opcode should exist", op);
        assertEquals(0xA0, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMoveFMetadata() {
        // MI Manual Table B.2, page 90: MOVE F has opcode 0xA1
        Opcode op = Opcode.fromCode(0xA1);
        assertNotNull("MOVE_F opcode should exist", op);
        assertEquals(0xA1, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testMoveDMetadata() {
        // MI Manual Table B.2, page 90: MOVE D has opcode 0xA2
        Opcode op = Opcode.fromCode(0xA2);
        assertNotNull("MOVE_D opcode should exist", op);
        assertEquals(0xA2, op.code);
        assertEquals(8, op.length);
        assertEquals(2, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testMoveDecode() {
        // MOVE W R0, R1 (0xA0, 0x50, 0x51)
        setupForDecode(0xA0, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("MOVE should decode to Move", cmd instanceof Move);
    }

    // ========================================================================
    // MOVEN - Table B.2, page 90
    // Opcodes 0xA3-0xA7: MOVEN B/H/W/F/D
    // ========================================================================

    @Test
    public void testMovenBMetadata() {
        // MI Manual Table B.2, page 90: MOVEN B has opcode 0xA3
        Opcode op = Opcode.fromCode(0xA3);
        assertNotNull("MOVEN_B opcode should exist", op);
        assertEquals(0xA3, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMovenDecode() {
        // MOVEN W R0, R1 (0xA5, 0x50, 0x51)
        setupForDecode(0xA5, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("MOVEN should decode to MoveN", cmd instanceof MoveN);
    }

    // ========================================================================
    // MOVEC - Table B.2, page 90
    // Opcodes 0xA8-0xAA: MOVEC B/H/W
    // ========================================================================

    @Test
    public void testMovecBMetadata() {
        // MI Manual Table B.2, page 90: MOVEC B has opcode 0xA8
        Opcode op = Opcode.fromCode(0xA8);
        assertNotNull("MOVEC_B opcode should exist", op);
        assertEquals(0xA8, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMovecDecode() {
        // MOVEC W R0, R1 (0xAA, 0x50, 0x51)
        setupForDecode(0xAA, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("MOVEC should decode to MoveC", cmd instanceof MoveC);
    }

    // ========================================================================
    // MOVEA - Table B.2, page 90
    // Opcode 0xAB: MOVEA
    // ========================================================================

    @Test
    public void testMoveaMetadata() {
        // MI Manual Table B.2, page 90: MOVEA has opcode 0xAB
        Opcode op = Opcode.fromCode(0xAB);
        assertNotNull("MOVEA opcode should exist", op);
        assertEquals(0xAB, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMoveaDecode() {
        // MOVEA R0, R1 (0xAB, 0x50, 0x51)
        setupForDecode(0xAB, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("MOVEA should decode to MoveA", cmd instanceof MoveA);
    }

    // ========================================================================
    // CONV - Table B.2, page 90
    // Opcode 0xAC: CONV
    // ========================================================================

    @Test
    public void testConvMetadata() {
        // MI Manual Table B.2, page 90: CONV has opcode 0xAC
        Opcode op = Opcode.fromCode(0xAC);
        assertNotNull("CONV opcode should exist", op);
        assertEquals(0xAC, op.code);
        assertEquals(1, op.length); // Conv has special handling for type codes
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testConvDecode() {
        // CONV type, R0 (0xAC, type_byte, 0x50)
        setupForDecode(0xAC, 0x00, 0x50);
        Command cmd = decodeNextCommand();
        assertTrue("CONV should decode to Conv", cmd instanceof Conv);
    }

    // ========================================================================
    // OR - Table B.2, page 90
    // Opcodes 0xAD-0xB2: OR B/H/W with 2 or 3 operands
    // ========================================================================

    @Test
    public void testOrB2Metadata() {
        // MI Manual Table B.2, page 90: OR B (2 operands) has opcode 0xAD
        Opcode op = Opcode.fromCode(0xAD);
        assertNotNull("OR_B_2 opcode should exist", op);
        assertEquals(0xAD, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testOrB3Metadata() {
        // MI Manual Table B.2, page 90: OR B (3 operands) has opcode 0xB0
        Opcode op = Opcode.fromCode(0xB0);
        assertNotNull("OR_B_3 opcode should exist", op);
        assertEquals(0xB0, op.code);
        assertEquals(1, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testOrDecode() {
        // OR W R0, R1 (0xAF, 0x50, 0x51)
        setupForDecode(0xAF, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("OR should decode to Or", cmd instanceof Or);
    }

    // ========================================================================
    // ANDNOT - Table B.3, page 91
    // Opcodes 0xB3-0xB8: ANDNOT B/H/W with 2 or 3 operands
    // ========================================================================

    @Test
    public void testAndnotB2Metadata() {
        // MI Manual Table B.3, page 91: ANDNOT B (2 operands) has opcode 0xB3
        Opcode op = Opcode.fromCode(0xB3);
        assertNotNull("ANDNOT_B_2 opcode should exist", op);
        assertEquals(0xB3, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testAndnotDecode() {
        // ANDNOT W R0, R1 (0xB5, 0x50, 0x51)
        setupForDecode(0xB5, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("ANDNOT should decode to AndNot", cmd instanceof AndNot);
    }

    // ========================================================================
    // XOR - Table B.3, page 91
    // Opcodes 0xB9-0xBE: XOR B/H/W with 2 or 3 operands
    // ========================================================================

    @Test
    public void testXorB2Metadata() {
        // MI Manual Table B.3, page 91: XOR B (2 operands) has opcode 0xB9
        Opcode op = Opcode.fromCode(0xB9);
        assertNotNull("XOR_B_2 opcode should exist", op);
        assertEquals(0xB9, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testXorDecode() {
        // XOR W R0, R1 (0xBB, 0x50, 0x51)
        setupForDecode(0xBB, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("XOR should decode to Xor", cmd instanceof Xor);
    }

    // ========================================================================
    // ADD - Table B.3, page 91
    // Opcodes 0xBF-0xC8: ADD B/H/W/F/D with 2 or 3 operands
    // ========================================================================

    @Test
    public void testAddB2Metadata() {
        // MI Manual Table B.3, page 91: ADD B (2 operands) has opcode 0xBF
        Opcode op = Opcode.fromCode(0xBF);
        assertNotNull("ADD_B_2 opcode should exist", op);
        assertEquals(0xBF, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testAddW2Metadata() {
        // MI Manual Table B.3, page 91: ADD W (2 operands) has opcode 0xC1
        Opcode op = Opcode.fromCode(0xC1);
        assertNotNull("ADD_W_2 opcode should exist", op);
        assertEquals(0xC1, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testAddF2Metadata() {
        // MI Manual Table B.3, page 91: ADD F (2 operands) has opcode 0xC2
        Opcode op = Opcode.fromCode(0xC2);
        assertNotNull("ADD_F_2 opcode should exist", op);
        assertEquals(0xC2, op.code);
        assertEquals(4, op.length);
        assertEquals(2, op.operandCount);
        assertTrue(op.floating);
    }

    @Test
    public void testAddW3Metadata() {
        // MI Manual Table B.3, page 91: ADD W (3 operands) has opcode 0xC6
        Opcode op = Opcode.fromCode(0xC6);
        assertNotNull("ADD_W_3 opcode should exist", op);
        assertEquals(0xC6, op.code);
        assertEquals(4, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testAddDecode2Op() {
        // ADD W R0, R1 (0xC1, 0x50, 0x51)
        setupForDecode(0xC1, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("ADD should decode to Add", cmd instanceof Add);
    }

    @Test
    public void testAddDecode3Op() {
        // ADD W R0, R1, R2 (0xC6, 0x50, 0x51, 0x52)
        setupForDecode(0xC6, 0x50, 0x51, 0x52);
        Command cmd = decodeNextCommand();
        assertTrue("ADD should decode to Add", cmd instanceof Add);
    }

    // ========================================================================
    // SUB - Table B.3, page 91
    // Opcodes 0xC9-0xD2: SUB B/H/W/F/D with 2 or 3 operands
    // ========================================================================

    @Test
    public void testSubB2Metadata() {
        // MI Manual Table B.3, page 91: SUB B (2 operands) has opcode 0xC9
        Opcode op = Opcode.fromCode(0xC9);
        assertNotNull("SUB_B_2 opcode should exist", op);
        assertEquals(0xC9, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testSubDecode() {
        // SUB W R0, R1 (0xCB, 0x50, 0x51)
        setupForDecode(0xCB, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("SUB should decode to Sub", cmd instanceof Sub);
    }

    // ========================================================================
    // MULT - Table B.4, page 92
    // Opcodes 0xD3-0xDC: MULT B/H/W/F/D with 2 or 3 operands
    // ========================================================================

    @Test
    public void testMultB2Metadata() {
        // MI Manual Table B.4, page 92: MULT B (2 operands) has opcode 0xD3
        Opcode op = Opcode.fromCode(0xD3);
        assertNotNull("MULT_B_2 opcode should exist", op);
        assertEquals(0xD3, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testMultDecode() {
        // MULT W R0, R1 (0xD5, 0x50, 0x51)
        setupForDecode(0xD5, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("MULT should decode to Mult", cmd instanceof Mult);
    }

    // ========================================================================
    // DIV - Table B.4, page 92
    // Opcodes 0xDD-0xE6: DIV B/H/W/F/D with 2 or 3 operands
    // ========================================================================

    @Test
    public void testDivB2Metadata() {
        // MI Manual Table B.4, page 92: DIV B (2 operands) has opcode 0xDD
        Opcode op = Opcode.fromCode(0xDD);
        assertNotNull("DIV_B_2 opcode should exist", op);
        assertEquals(0xDD, op.code);
        assertEquals(1, op.length);
        assertEquals(2, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testDivDecode() {
        // DIV W R0, R1 (0xDF, 0x50, 0x51)
        setupForDecode(0xDF, 0x50, 0x51);
        Command cmd = decodeNextCommand();
        assertTrue("DIV should decode to Div", cmd instanceof Div);
    }

    // ========================================================================
    // SH - Table B.4, page 92
    // Opcode 0xE7: SH
    // ========================================================================

    @Test
    public void testShMetadata() {
        // MI Manual Table B.4, page 92: SH has opcode 0xE7
        Opcode op = Opcode.fromCode(0xE7);
        assertNotNull("SH opcode should exist", op);
        assertEquals(0xE7, op.code);
        assertEquals(4, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testShDecode() {
        // SH R0, R1, R2 (0xE7, 0x50, 0x51, 0x52)
        setupForDecode(0xE7, 0x50, 0x51, 0x52);
        Command cmd = decodeNextCommand();
        assertTrue("SH should decode to Sh", cmd instanceof Sh);
    }

    // ========================================================================
    // ROT - Table B.4, page 92
    // Opcode 0xE8: ROT
    // ========================================================================

    @Test
    public void testRotMetadata() {
        // MI Manual Table B.4, page 92: ROT has opcode 0xE8
        Opcode op = Opcode.fromCode(0xE8);
        assertNotNull("ROT opcode should exist", op);
        assertEquals(0xE8, op.code);
        assertEquals(4, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testRotDecode() {
        // ROT R0, R1, R2 (0xE8, 0x50, 0x51, 0x52)
        setupForDecode(0xE8, 0x50, 0x51, 0x52);
        Command cmd = decodeNextCommand();
        assertTrue("ROT should decode to Rot", cmd instanceof Rot);
    }

    // ========================================================================
    // Conditional Jumps - Table B.4, page 92
    // Opcodes 0xE9-0xF0: JEQ, JNE, JGT, JGE, JLT, JLE, JC, JNC
    // ========================================================================

    @Test
    public void testJeqMetadata() {
        // MI Manual Table B.4, page 92: JEQ has opcode 0xE9
        Opcode op = Opcode.fromCode(0xE9);
        assertNotNull("JEQ opcode should exist", op);
        assertEquals(0xE9, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testJneMetadata() {
        // MI Manual Table B.4, page 92: JNE has opcode 0xEA
        Opcode op = Opcode.fromCode(0xEA);
        assertNotNull("JNE opcode should exist", op);
        assertEquals(0xEA, op.code);
    }

    @Test
    public void testJgtMetadata() {
        // MI Manual Table B.4, page 92: JGT has opcode 0xEB
        Opcode op = Opcode.fromCode(0xEB);
        assertNotNull("JGT opcode should exist", op);
        assertEquals(0xEB, op.code);
    }

    @Test
    public void testJgeMetadata() {
        // MI Manual Table B.4, page 92: JGE has opcode 0xEC
        Opcode op = Opcode.fromCode(0xEC);
        assertNotNull("JGE opcode should exist", op);
        assertEquals(0xEC, op.code);
    }

    @Test
    public void testJltMetadata() {
        // MI Manual Table B.4, page 92: JLT has opcode 0xED
        Opcode op = Opcode.fromCode(0xED);
        assertNotNull("JLT opcode should exist", op);
        assertEquals(0xED, op.code);
    }

    @Test
    public void testJleMetadata() {
        // MI Manual Table B.4, page 92: JLE has opcode 0xEE
        Opcode op = Opcode.fromCode(0xEE);
        assertNotNull("JLE opcode should exist", op);
        assertEquals(0xEE, op.code);
    }

    @Test
    public void testJcMetadata() {
        // MI Manual Table B.4, page 92: JC has opcode 0xEF
        Opcode op = Opcode.fromCode(0xEF);
        assertNotNull("JC opcode should exist", op);
        assertEquals(0xEF, op.code);
    }

    @Test
    public void testJncMetadata() {
        // MI Manual Table B.4, page 92: JNC has opcode 0xF0
        Opcode op = Opcode.fromCode(0xF0);
        assertNotNull("JNC opcode should exist", op);
        assertEquals(0xF0, op.code);
    }

    @Test
    public void testJumpsDecode() {
        // JEQ target (0xE9, abs_address)
        setupForDecode(0xE9, 0x9F, 0x00, 0x00, 0x01, 0x00); // Absolute address 0x100
        Command cmd = decodeNextCommand();
        assertTrue("JEQ should decode to Jump", cmd instanceof Jump);
    }

    // ========================================================================
    // JUMP - Table B.4, page 92
    // Opcode 0xF1: JUMP (unconditional)
    // ========================================================================

    @Test
    public void testJumpMetadata() {
        // MI Manual Table B.4, page 92: JUMP has opcode 0xF1
        Opcode op = Opcode.fromCode(0xF1);
        assertNotNull("JUMP opcode should exist", op);
        assertEquals(0xF1, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testJumpDecode() {
        // JUMP target (0xF1, abs_address)
        setupForDecode(0xF1, 0x9F, 0x00, 0x00, 0x01, 0x00);
        Command cmd = decodeNextCommand();
        assertTrue("JUMP should decode to Jump", cmd instanceof Jump);
    }

    // ========================================================================
    // CALL - Table B.4, page 92
    // Opcode 0xF2: CALL
    // ========================================================================

    @Test
    public void testCallMetadata() {
        // MI Manual Table B.4, page 92: CALL has opcode 0xF2
        Opcode op = Opcode.fromCode(0xF2);
        assertNotNull("CALL opcode should exist", op);
        assertEquals(0xF2, op.code);
        assertEquals(4, op.length);
        assertEquals(1, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testCallDecode() {
        // CALL target (0xF2, abs_address)
        setupForDecode(0xF2, 0x9F, 0x00, 0x00, 0x01, 0x00);
        Command cmd = decodeNextCommand();
        assertTrue("CALL should decode to Call", cmd instanceof Call);
    }

    // ========================================================================
    // RET - Table B.4, page 92
    // Opcode 0xF3: RET
    // ========================================================================

    @Test
    public void testRetMetadata() {
        // MI Manual Table B.4, page 92: RET has opcode 0xF3
        Opcode op = Opcode.fromCode(0xF3);
        assertNotNull("RET opcode should exist", op);
        assertEquals(0xF3, op.code);
        assertEquals(0, op.length);
        assertEquals(0, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testRetDecode() {
        // RET (0xF3)
        setupForDecode(0xF3);
        Command cmd = decodeNextCommand();
        assertTrue("RET should decode to Ret", cmd instanceof Ret);
    }

    // ========================================================================
    // PUSHR - Table B.4, page 92
    // Opcode 0xF4: PUSHR
    // ========================================================================

    @Test
    public void testPushrMetadata() {
        // MI Manual Table B.4, page 92: PUSHR has opcode 0xF4
        Opcode op = Opcode.fromCode(0xF4);
        assertNotNull("PUSHR opcode should exist", op);
        assertEquals(0xF4, op.code);
        assertEquals(4, op.length);
        assertEquals(0, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testPushrDecode() {
        // PUSHR (0xF4)
        setupForDecode(0xF4);
        Command cmd = decodeNextCommand();
        assertTrue("PUSHR should decode to Pushr", cmd instanceof Pushr);
    }

    // ========================================================================
    // POPR - Table B.4, page 92
    // Opcode 0xF5: POPR
    // ========================================================================

    @Test
    public void testPoprMetadata() {
        // MI Manual Table B.4, page 92: POPR has opcode 0xF5
        Opcode op = Opcode.fromCode(0xF5);
        assertNotNull("POPR opcode should exist", op);
        assertEquals(0xF5, op.code);
        assertEquals(0, op.length);
        assertEquals(0, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testPoprDecode() {
        // POPR (0xF5)
        setupForDecode(0xF5);
        Command cmd = decodeNextCommand();
        assertTrue("POPR should decode to Popr", cmd instanceof Popr);
    }

    // ========================================================================
    // EXTS - Table B.5, page 93
    // Opcode 0xF6: EXTS
    // ========================================================================

    @Test
    public void testExtsMetadata() {
        // MI Manual Table B.5, page 93: EXTS has opcode 0xF6
        Opcode op = Opcode.fromCode(0xF6);
        assertNotNull("EXTS opcode should exist", op);
        assertEquals(0xF6, op.code);
        assertEquals(4, op.length);
        assertEquals(4, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testExtsDecode() {
        // EXTS R0, R1, R2, R3 (0xF6, 0x50, 0x51, 0x52, 0x53)
        setupForDecode(0xF6, 0x50, 0x51, 0x52, 0x53);
        Command cmd = decodeNextCommand();
        assertTrue("EXTS should decode to Exts", cmd instanceof Exts);
    }

    // ========================================================================
    // EXT - Table B.5, page 93
    // Opcode 0xF7: EXT
    // ========================================================================

    @Test
    public void testExtMetadata() {
        // MI Manual Table B.5, page 93: EXT has opcode 0xF7
        Opcode op = Opcode.fromCode(0xF7);
        assertNotNull("EXT opcode should exist", op);
        assertEquals(0xF7, op.code);
        assertEquals(4, op.length);
        assertEquals(4, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testExtDecode() {
        // EXT R0, R1, R2, R3 (0xF7, 0x50, 0x51, 0x52, 0x53)
        setupForDecode(0xF7, 0x50, 0x51, 0x52, 0x53);
        Command cmd = decodeNextCommand();
        assertTrue("EXT should decode to Ext", cmd instanceof Ext);
    }

    // ========================================================================
    // INS - Table B.5, page 93
    // Opcode 0xF8: INS
    // ========================================================================

    @Test
    public void testInsMetadata() {
        // MI Manual Table B.5, page 93: INS has opcode 0xF8
        Opcode op = Opcode.fromCode(0xF8);
        assertNotNull("INS opcode should exist", op);
        assertEquals(0xF8, op.code);
        assertEquals(4, op.length);
        assertEquals(4, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testInsDecode() {
        // INS R0, R1, R2, R3 (0xF8, 0x50, 0x51, 0x52, 0x53)
        setupForDecode(0xF8, 0x50, 0x51, 0x52, 0x53);
        Command cmd = decodeNextCommand();
        assertTrue("INS should decode to Ins", cmd instanceof Ins);
    }

    // ========================================================================
    // FINDS - Table B.5, page 93
    // Opcode 0xF9: FINDS
    // ========================================================================

    @Test
    public void testFindsMetadata() {
        // MI Manual Table B.5, page 93: FINDS has opcode 0xF9
        Opcode op = Opcode.fromCode(0xF9);
        assertNotNull("FINDS opcode should exist", op);
        assertEquals(0xF9, op.code);
        assertEquals(4, op.length);
        assertEquals(4, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testFindsDecode() {
        // FINDS R0, R1, R2, R3 (0xF9, 0x50, 0x51, 0x52, 0x53)
        setupForDecode(0xF9, 0x50, 0x51, 0x52, 0x53);
        Command cmd = decodeNextCommand();
        assertTrue("FINDS should decode to Finds", cmd instanceof Finds);
    }

    // ========================================================================
    // FINDC - Table B.5, page 93
    // Opcode 0xFA: FINDC
    // ========================================================================

    @Test
    public void testFindcMetadata() {
        // MI Manual Table B.5, page 93: FINDC has opcode 0xFA
        Opcode op = Opcode.fromCode(0xFA);
        assertNotNull("FINDC opcode should exist", op);
        assertEquals(0xFA, op.code);
        assertEquals(4, op.length);
        assertEquals(4, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testFindcDecode() {
        // FINDC R0, R1, R2, R3 (0xFA, 0x50, 0x51, 0x52, 0x53)
        setupForDecode(0xFA, 0x50, 0x51, 0x52, 0x53);
        Command cmd = decodeNextCommand();
        assertTrue("FINDC should decode to Findc", cmd instanceof Findc);
    }

    // ========================================================================
    // JBSSI - Table B.5, page 93
    // Opcode 0xFB: JBSSI
    // ========================================================================

    @Test
    public void testJbssiMetadata() {
        // MI Manual Table B.5, page 93: JBSSI has opcode 0xFB
        Opcode op = Opcode.fromCode(0xFB);
        assertNotNull("JBSSI opcode should exist", op);
        assertEquals(0xFB, op.code);
        assertEquals(4, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testJbssiDecode() {
        // JBSSI R0, R1, target (0xFB, 0x50, 0x51, 0x9F, addr...)
        setupForDecode(0xFB, 0x50, 0x51, 0x9F, 0x00, 0x00, 0x01, 0x00);
        Command cmd = decodeNextCommand();
        assertTrue("JBSSI should decode to Jbssi", cmd instanceof Jbssi);
    }

    // ========================================================================
    // JBCCI - Table B.5, page 93
    // Opcode 0xFC: JBCCI
    // ========================================================================

    @Test
    public void testJbcciMetadata() {
        // MI Manual Table B.5, page 93: JBCCI has opcode 0xFC
        Opcode op = Opcode.fromCode(0xFC);
        assertNotNull("JBCCI opcode should exist", op);
        assertEquals(0xFC, op.code);
        assertEquals(4, op.length);
        assertEquals(3, op.operandCount);
        assertFalse(op.floating);
    }

    @Test
    public void testJbcciDecode() {
        // JBCCI R0, R1, target (0xFC, 0x50, 0x51, 0x9F, addr...)
        setupForDecode(0xFC, 0x50, 0x51, 0x9F, 0x00, 0x00, 0x01, 0x00);
        Command cmd = decodeNextCommand();
        assertTrue("JBCCI should decode to Jbcci", cmd instanceof Jbcci);
    }

    // ========================================================================
    // Unknown opcode test
    // ========================================================================

    @Test(expected = OpcodeDecodeException.class)
    public void testUnknownOpcode() {
        // 0x01 is not a valid opcode
        setupForDecode(0x01);
        decodeNextCommand();
    }

    // ========================================================================
    // Verify all implemented opcodes have correct lookup
    // ========================================================================

    @Test
    public void testAllOpcodesMappedCorrectly() {
        // Verify that fromCode returns the correct enum for each defined opcode
        for (Opcode op : Opcode.values()) {
            Opcode lookup = Opcode.fromCode(op.code);
            assertNotNull("Opcode " + op.name() + " should be found by code", lookup);
            assertEquals("Opcode lookup should return same enum", op, lookup);
        }
    }

    @Test
    public void testNoDuplicateOpcodes() {
        // Verify no duplicate opcode values (this is also checked at static init)
        int[] counts = new int[256];
        for (Opcode op : Opcode.values()) {
            counts[op.code]++;
            assertEquals("Opcode 0x" + Integer.toHexString(op.code) + " should appear only once",
                    1, counts[op.code]);
        }
    }
}
