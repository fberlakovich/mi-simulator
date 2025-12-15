package engine.parser;

import engine.Machine;
import engine.program.Program;
import engine.scanner.Scanner;
import engine.commands.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for the MI assembly parser.
 *
 * Note: MI assembly requires specific formatting:
 * - Labels must start at column 0, followed by colon
 * - Instructions must be indented (whitespace before the instruction)
 * - Indirect addressing uses '!' not '@'
 */
public class ParserTest {

    // Indentation prefix required for instructions (10 spaces)
    private static final String I = "          ";

    @Before
    public void setUp() {
        Machine.resetInstance();
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private Parser createParser(String source) {
        Scanner scanner = new Scanner(false);
        scanner.init(source);
        Parser parser = new Parser(Machine.getInstance(), scanner);
        parser.start();
        return parser;
    }

    private Program parseSuccessfully(String source) {
        Parser parser = createParser(source);
        assertTrue("Expected successful parse but got error: " + parser.getErrorMessage().getErrorMessage(),
                parser.eval());
        return parser.getProgramm();
    }

    private void expectParseError(String source) {
        Parser parser = createParser(source);
        assertFalse("Expected parse error", parser.eval());
    }

    private void expectParseError(String source, String expectedErrorSubstring) {
        Parser parser = createParser(source);
        assertFalse("Expected parse error", parser.eval());
        String error = parser.getErrorMessage().getErrorMessage();
        assertTrue("Expected error containing '" + expectedErrorSubstring + "' but got: " + error,
                error.contains(expectedErrorSubstring));
    }

    // ========================================================================
    // Basic structure tests
    // ========================================================================

    @Test
    public void parse_minimalProgram() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "HALT\n" + I + "END");
        assertEquals(1, program.getCommands().size());
        assertTrue(program.getCommands().get(0) instanceof Halt);
    }

    @Test
    public void parse_emptySegment() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "END");
        assertEquals(0, program.getCommands().size());
    }

    @Test
    public void parse_multipleInstructions() {
        Program program = parseSuccessfully(
                I + "SEG\n" +
                I + "MOVE W I 42, R0\n" +
                I + "MOVE W I 100, R1\n" +
                I + "ADD W R0, R1\n" +
                I + "HALT\n" +
                I + "END");
        assertEquals(4, program.getCommands().size());
    }

    @Test
    public void parse_missingSEG_accepted() {
        // Parser is lenient - SEG is optional
        Program program = parseSuccessfully(I + "HALT\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_missingEND_accepted() {
        // Parser is lenient - END is optional (EOF terminates)
        Program program = parseSuccessfully(I + "SEG\n" + I + "HALT");
        assertEquals(1, program.getCommands().size());
    }

    // ========================================================================
    // Data type tests (B, H, W, F, D)
    // ========================================================================

    @Test
    public void parse_moveB_byte() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE B I 42, R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Move);
    }

    @Test
    public void parse_moveH_halfword() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE H I 1000, R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Move);
    }

    @Test
    public void parse_moveW_word() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W I 100000, R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Move);
    }

    @Test
    public void parse_moveF_float() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE F I 3.14, R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Move);
    }

    @Test
    public void parse_moveD_double() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE D I 3.14159265359, R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Move);
    }

    // ========================================================================
    // Addressing mode tests
    // ========================================================================

    @Test
    public void parse_immediateAddressing() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W I 42, R0\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_registerAddressing() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W R0, R1\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_indirectAddressing() {
        // MI uses !R for indirect addressing
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W !R0, R1\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_indirectWithOffset() {
        // MI uses offset+!R syntax for relative addressing with offset
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W 100+!R0, R1\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_absoluteAddressing() {
        // MI uses plain numbers for absolute addressing
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W 1000, R0\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_cellarAddressing_push() {
        // Cellar addressing: pre-decrement for push uses -!R syntax
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W R0, -!R14\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_cellarAddressing_pop() {
        // Cellar addressing: post-increment for pop uses !R+ syntax
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W !R14+, R0\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    // ========================================================================
    // Arithmetic instructions
    // ========================================================================

    @Test
    public void parse_add_twoOperands() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "ADD W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Add);
    }

    @Test
    public void parse_add_threeOperands() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "ADD W R0, R1, R2\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Add);
    }

    @Test
    public void parse_sub() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "SUB W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Sub);
    }

    @Test
    public void parse_mult() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MULT W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Mult);
    }

    @Test
    public void parse_div() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "DIV W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Div);
    }

    // ========================================================================
    // Logical instructions
    // ========================================================================

    @Test
    public void parse_or() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "OR W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Or);
    }

    @Test
    public void parse_andnot() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "ANDNOT W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof AndNot);
    }

    @Test
    public void parse_xor() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "XOR W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Xor);
    }

    // ========================================================================
    // Shift/rotate instructions
    // ========================================================================

    @Test
    public void parse_sh() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "SH R0, R1, R2\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Sh);
    }

    @Test
    public void parse_rot() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "ROT R0, R1, R2\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Rot);
    }

    // ========================================================================
    // Compare instruction
    // ========================================================================

    @Test
    public void parse_cmp() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "CMP W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Cmp);
    }

    // ========================================================================
    // Jump instructions
    // ========================================================================

    @Test
    public void parse_jump_unconditional() {
        // Labels start at column 0, followed by colon, then instruction
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JUMP L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jeq() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JEQ L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jne() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JNE L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jgt() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JGT L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jge() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JGE L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jlt() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JLT L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jle() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JLE L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jc() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JC L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    @Test
    public void parse_jnc() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "JNC L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Jump);
    }

    // ========================================================================
    // Call/Return instructions
    // ========================================================================

    @Test
    public void parse_call() {
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "CALL L\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Call);
    }

    @Test
    public void parse_ret() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "RET\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Ret);
    }

    @Test
    public void parse_pushr() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "PUSHR\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Pushr);
    }

    @Test
    public void parse_popr() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "POPR\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Popr);
    }

    // ========================================================================
    // Other instructions
    // ========================================================================

    @Test
    public void parse_clear() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "CLEAR W R0\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Clear);
    }

    @Test
    public void parse_conv() {
        // CONV takes a source operand and destination register (conversion with sign extension)
        Program program = parseSuccessfully(I + "SEG\n" + I + "CONV R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Conv);
    }

    @Test
    public void parse_moven() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVEN W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof MoveN);
    }

    @Test
    public void parse_movec() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVEC W R0, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof MoveC);
    }

    @Test
    public void parse_movea() {
        // MOVEA loads addresses of label references
        Program program = parseSuccessfully(I + "SEG\n" + "L:" + I + "MOVEA L, R1\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof MoveA);
    }

    // ========================================================================
    // Label tests
    // ========================================================================

    @Test
    public void parse_labelDefinition() {
        Program program = parseSuccessfully(I + "SEG\n" + "LABEL:" + I + "HALT\n" + I + "END");
        assertEquals(1, program.getLabels().size());
        assertEquals("LABEL", program.getLabels().get(0).getName());
    }

    @Test
    public void parse_multipleLabels() {
        Program program = parseSuccessfully(
                I + "SEG\n" +
                "START:" + I + "MOVE W I 0, R0\n" +
                "LOOP:" + I + "ADD W I 1, R0\n" +
                I + "CMP W R0, I 10\n" +
                I + "JLT LOOP\n" +
                "DONE:" + I + "HALT\n" +
                I + "END");
        assertEquals(3, program.getLabels().size());
    }

    @Test
    public void parse_forwardReference() {
        Program program = parseSuccessfully(
                I + "SEG\n" +
                I + "JUMP SKIP\n" +
                I + "MOVE W I 1, R0\n" +
                "SKIP:" + I + "HALT\n" +
                I + "END");
        assertEquals(3, program.getCommands().size());
    }

    @Test
    public void parse_undefinedLabel_error() {
        expectParseError(
                I + "SEG\n" +
                I + "JUMP UNDEFINED\n" +
                I + "END");
    }

    @Test
    public void parse_duplicateLabel_error() {
        expectParseError(
                I + "SEG\n" +
                "L:" + I + "MOVE W I 1, R0\n" +
                "L:" + I + "HALT\n" +
                I + "END");
    }

    // ========================================================================
    // DD (Define Data) tests
    // ========================================================================

    @Test
    public void parse_dd_byte() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "DD B 42\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof DD);
    }

    @Test
    public void parse_dd_word() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "DD W 100000\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof DD);
    }

    @Test
    public void parse_dd_withLabel() {
        // Data is referenced via label name in indirect addressing
        Program program = parseSuccessfully(I + "SEG\n" + "DATA:" + I + "DD W 42\n" + I + "MOVE W DATA, R0\n" + I + "END");
        assertEquals(1, program.getLabels().size());
    }

    @Test
    public void parse_res_reserve() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "RES 100\n" + I + "END");
        assertTrue(program.getCommands().get(0) instanceof Res);
    }

    // ========================================================================
    // Negative number tests
    // ========================================================================

    @Test
    public void parse_negativeImmediate() {
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W I -42, R0\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_negativeOffset() {
        // MI uses -offset+!R syntax for negative offset relative addressing
        Program program = parseSuccessfully(I + "SEG\n" + I + "MOVE W -10+!R0, R1\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    // ========================================================================
    // Comment tests
    // ========================================================================

    @Test
    public void parse_commentAfterInstruction() {
        // Comments in MI use ';' (apostrophe token) at end of line
        // The parser treats ';' as end-of-statement (like newline)
        Program program = parseSuccessfully(
                I + "SEG\n" +
                I + "MOVE W I 42, R0;\n" +
                I + "HALT\n" +
                I + "END");
        assertEquals(2, program.getCommands().size());
    }

    // ========================================================================
    // Error handling tests
    // ========================================================================

    @Test
    public void parse_invalidInstruction_error() {
        expectParseError(I + "SEG\n" + I + "INVALID\n" + I + "END");
    }

    @Test
    public void parse_invalidRegister_error() {
        expectParseError(I + "SEG\n" + I + "MOVE W R99, R0\n" + I + "END");
    }

    @Test
    public void parse_missingOperand_error() {
        expectParseError(I + "SEG\n" + I + "MOVE W R0\n" + I + "END");
    }

    @Test
    public void parse_invalidDataType_error() {
        expectParseError(I + "SEG\n" + I + "MOVE X I 42, R0\n" + I + "END");
    }

    // ========================================================================
    // Whitespace handling
    // ========================================================================

    @Test
    public void parse_extraWhitespace() {
        Program program = parseSuccessfully("   SEG   \n   MOVE   W   I   42  ,  R0   \n   END   ");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_tabsAndSpaces() {
        Program program = parseSuccessfully("\tSEG\n\tMOVE\tW\tI\t42,\tR0\n\tEND");
        assertEquals(1, program.getCommands().size());
    }

    @Test
    public void parse_indentedSeg() {
        // SEG can also be indented (not required at column 0)
        Program program = parseSuccessfully(I + "SEG\n" + I + "HALT\n" + I + "END");
        assertEquals(1, program.getCommands().size());
    }

    // ========================================================================
    // Case sensitivity
    // ========================================================================

    @Test
    public void parse_lowercaseInstructions_error() {
        // MI assembly keywords are case-sensitive (uppercase only)
        expectParseError(I + "seg\n" + I + "move w i 42, r0\n" + I + "halt\n" + I + "end");
    }

    @Test
    public void parse_mixedCaseInstructions_error() {
        // MI assembly keywords are case-sensitive (uppercase only)
        expectParseError(I + "Seg\n" + I + "Move W I 42, R0\n" + I + "Halt\n" + I + "End");
    }
}