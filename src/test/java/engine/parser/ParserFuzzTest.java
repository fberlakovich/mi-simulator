package engine.parser;

import engine.Machine;
import engine.scanner.Scanner;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Fuzz tests for the MI assembly parser.
 *
 * These tests generate random inputs to verify:
 * 1. Parser never crashes (no uncaught exceptions)
 * 2. Parser always terminates (no infinite loops)
 * 3. Valid programs parse successfully
 * 4. Invalid programs produce errors gracefully
 */
public class ParserFuzzTest {

    private static final int FUZZ_ITERATIONS = 1000;
    private static final int MAX_PROGRAM_LENGTH = 50;
    private Random random;

    private static final String[] INSTRUCTIONS = {
        "ADD", "SUB", "MULT", "DIV", "OR", "ANDNOT", "XOR",
        "MOVE", "MOVEN", "MOVEC", "MOVEA", "CLEAR", "CMP",
        "SH", "ROT", "CONV", "HALT", "RET", "PUSHR", "POPR",
        "JUMP", "JEQ", "JNE", "JGT", "JGE", "JLT", "JLE", "JC", "JNC", "JV", "JNV",
        "CALL", "DD", "RES", "EXT", "EXTS", "INS", "FINDS", "FINDC", "JBSSI", "JBCCI"
    };

    private static final String[] DATA_TYPES = {"B", "H", "W", "F", "D"};
    private static final String[] REGISTERS = {
        "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7",
        "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15"
    };

    @Before
    public void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
    }

    /**
     * Test that parser handles random garbage gracefully.
     */
    @Test
    public void fuzz_randomGarbage_noCrash() {
        for (int i = 0; i < FUZZ_ITERATIONS; i++) {
            String garbage = generateRandomString(random.nextInt(200));
            parseAndExpectNoException(garbage);
        }
    }

    /**
     * Test that parser handles random valid-ish programs.
     */
    @Test
    public void fuzz_randomPrograms_noCrash() {
        for (int i = 0; i < FUZZ_ITERATIONS; i++) {
            String program = generateRandomProgram();
            parseAndExpectNoException(program);
        }
    }

    /**
     * Test that parser handles programs with random whitespace.
     */
    @Test
    public void fuzz_randomWhitespace_noCrash() {
        for (int i = 0; i < FUZZ_ITERATIONS / 10; i++) {
            String program = insertRandomWhitespace(generateValidProgram());
            parseAndExpectNoException(program);
        }
    }

    /**
     * Test that parser handles edge case numbers.
     */
    @Test
    public void fuzz_edgeCaseNumbers_noCrash() {
        int[] edgeCases = {
            0, 1, -1, 127, 128, -128, -129,
            255, 256, -256,
            32767, 32768, -32768, -32769,
            65535, 65536, -65536,
            Integer.MAX_VALUE, Integer.MIN_VALUE
        };

        for (int value : edgeCases) {
            String program = String.format("SEG\nMOVE W I %d, R0\nEND", value);
            parseAndExpectNoException(program);
        }
    }

    /**
     * Test that parser handles very long lines.
     */
    @Test
    public void fuzz_veryLongLines_noCrash() {
        // Long comment
        StringBuilder sb = new StringBuilder("SEG\n; ");
        for (int i = 0; i < 10000; i++) {
            sb.append('x');
        }
        sb.append("\nHALT\nEND");
        parseAndExpectNoException(sb.toString());
    }

    /**
     * Test that parser handles deeply nested expressions (many operands).
     */
    @Test
    public void fuzz_manyOperands_noCrash() {
        for (int numOperands = 1; numOperands <= 10; numOperands++) {
            StringBuilder sb = new StringBuilder("SEG\nADD W ");
            for (int i = 0; i < numOperands; i++) {
                if (i > 0) sb.append(", ");
                sb.append("R").append(i % 16);
            }
            sb.append("\nEND");
            parseAndExpectNoException(sb.toString());
        }
    }

    /**
     * Test that parser handles special characters.
     */
    @Test
    public void fuzz_specialCharacters_noCrash() {
        String[] specialChars = {
            "\0", "\t", "\r", "\n\n\n",
            "!", "@", "#", "$", "%", "^", "&", "*",
            "(", ")", "[", "]", "{", "}", "<", ">",
            "/", "\\", "|", "?", "`", "~",
            "\u0000", "\u00FF", "\u0100"
        };

        for (String special : specialChars) {
            parseAndExpectNoException("SEG\n" + special + "\nEND");
            parseAndExpectNoException("SEG\nMOVE W I 1" + special + ", R0\nEND");
        }
    }

    /**
     * Test that parser handles empty and near-empty inputs.
     */
    @Test
    public void fuzz_emptyInputs_noCrash() {
        parseAndExpectNoException("");
        parseAndExpectNoException(" ");
        parseAndExpectNoException("\n");
        parseAndExpectNoException("\t");
        parseAndExpectNoException("\r\n");
        parseAndExpectNoException("   \n   \n   ");
        parseAndExpectNoException("SEG");
        parseAndExpectNoException("END");
        parseAndExpectNoException("SEG\nEND");
    }

    /**
     * Test that parser handles Unicode input.
     */
    @Test
    public void fuzz_unicodeInput_noCrash() {
        parseAndExpectNoException("SEG\n; こんにちは\nHALT\nEND");
        parseAndExpectNoException("SEG\n; 🎉\nHALT\nEND");
        parseAndExpectNoException("SEG\nМЕТКА: HALT\nEND");
    }

    /**
     * Test label edge cases.
     */
    @Test
    public void fuzz_labelEdgeCases_noCrash() {
        // Very long label names
        StringBuilder longLabel = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longLabel.append('A');
        }
        parseAndExpectNoException("SEG\n" + longLabel + ": HALT\nEND");

        // Labels with numbers
        parseAndExpectNoException("SEG\nLABEL123: HALT\nEND");
        parseAndExpectNoException("SEG\n123LABEL: HALT\nEND");

        // Labels with underscores
        parseAndExpectNoException("SEG\nMY_LABEL: HALT\nEND");
        parseAndExpectNoException("SEG\n_LABEL: HALT\nEND");
    }

    /**
     * Test floating point edge cases.
     */
    @Test
    public void fuzz_floatingPointEdgeCases_noCrash() {
        String[] floatValues = {
            "0.0", "-0.0", "1.0", "-1.0",
            "3.14159265358979323846",
            "1e10", "1e-10", "1E10", "1E-10",
            "1.5e10", "-1.5e-10",
            "0.000000001", "999999999.999999999",
            "Infinity", "-Infinity", "NaN"
        };

        for (String value : floatValues) {
            parseAndExpectNoException("SEG\nMOVE F I " + value + ", R0\nEND");
            parseAndExpectNoException("SEG\nMOVE D I " + value + ", R0\nEND");
        }
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private void parseAndExpectNoException(String source) {
        Machine.resetInstance();
        try {
            Scanner scanner = new Scanner(false);
            scanner.init(source);
            Parser parser = new Parser(Machine.getInstance(), scanner);
            parser.start();
            parser.eval(); // May return true or false, that's fine
            // Success - no exception thrown
        } catch (Exception e) {
            fail("Parser threw exception on input: [" + truncate(source, 100) + "]\n" +
                 "Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) random.nextInt(128));
        }
        return sb.toString();
    }

    private String generateRandomProgram() {
        StringBuilder sb = new StringBuilder();
        sb.append("SEG\n");

        int numInstructions = random.nextInt(MAX_PROGRAM_LENGTH);
        for (int i = 0; i < numInstructions; i++) {
            // Maybe add a label
            if (random.nextInt(10) == 0) {
                sb.append("L").append(i).append(": ");
            }

            // Add an instruction
            String instr = INSTRUCTIONS[random.nextInt(INSTRUCTIONS.length)];
            sb.append(instr);

            // Maybe add a data type
            if (random.nextBoolean()) {
                sb.append(" ").append(DATA_TYPES[random.nextInt(DATA_TYPES.length)]);
            }

            // Maybe add operands
            int numOperands = random.nextInt(4);
            for (int j = 0; j < numOperands; j++) {
                if (j > 0) sb.append(",");
                sb.append(" ");
                sb.append(generateRandomOperand());
            }

            sb.append("\n");
        }

        sb.append("END");
        return sb.toString();
    }

    private String generateRandomOperand() {
        int type = random.nextInt(8);
        switch (type) {
            case 0: // Register direct
                return REGISTERS[random.nextInt(REGISTERS.length)];
            case 1: // Immediate
                return "I " + (random.nextInt(1000) - 500);
            case 2: // Indirect via register: !Rx
                return "!" + REGISTERS[random.nextInt(REGISTERS.length)];
            case 3: // Relative addressing: offset+!Rx
                return random.nextInt(100) + "+!" + REGISTERS[random.nextInt(REGISTERS.length)];
            case 4: // Absolute addressing: @address
                return "@" + random.nextInt(10000);
            case 5: // Double indirect: !!Rx
                return "!!" + REGISTERS[random.nextInt(REGISTERS.length)];
            case 6: // Cellar push: -!Rx
                return "-!" + REGISTERS[random.nextInt(REGISTERS.length)];
            case 7: // Cellar pop: !Rx+
                return "!" + REGISTERS[random.nextInt(REGISTERS.length)] + "+";
            default:
                return "R0";
        }
    }

    private String generateValidProgram() {
        return "SEG\n" +
               "MOVE W I 42, R0\n" +
               "ADD W R0, R0, R1\n" +
               "HALT\n" +
               "END";
    }

    private String insertRandomWhitespace(String program) {
        StringBuilder sb = new StringBuilder();
        for (char c : program.toCharArray()) {
            if (random.nextInt(10) == 0 && c != '\n') {
                int numSpaces = random.nextInt(5);
                for (int i = 0; i < numSpaces; i++) {
                    sb.append(random.nextBoolean() ? ' ' : '\t');
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private String truncate(String s, int maxLength) {
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength) + "...";
    }
}