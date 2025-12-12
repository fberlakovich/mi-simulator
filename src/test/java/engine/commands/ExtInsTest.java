package engine.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtInsTest extends InstructionTestBase {

    @Test
    public void testExtractMiddleBits() {
        // MI spec: EXT P, S, source, dest (MSB-0 numbering)
        // For 0xFF00FF00, bits 8-15 (from MSB) are 0x00
        // To get 0xFF, we need bits 0-7 (the high byte) or bits 16-23 (third byte)
        // 0xFF00FF00 = [FF][00][FF][00] in bytes
        // Bits 0-7 (MSB) = 0xFF, bits 8-15 = 0x00, bits 16-23 = 0xFF, bits 24-31 = 0x00
        String program = """
                SEG
                MOVE W I H'FF00FF00', R0
                EXT I 0, I 8, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xFF, getRegister(1));
    }

    @Test
    public void testExtractLowBits() {
        // 0x12345678: bits 24-31 (from MSB) = 0x78, bits 16-31 = 0x5678
        String program = """
                SEG
                MOVE W I H'12345678', R0
                EXT I 16, I 16, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x5678, getRegister(1));
    }

    @Test
    public void testExtractHighBits() {
        // 0x12345678: bits 0-15 (from MSB) = 0x1234
        String program = """
                SEG
                MOVE W I H'12345678', R0
                EXT I 0, I 16, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x1234, getRegister(1));
    }

    @Test
    public void testExtractSingleBit() {
        // 0x00000008 = bit 28 (from MSB) is set
        String program = """
                SEG
                MOVE W I H'00000008', R0
                EXT I 28, I 1, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(1, getRegister(1));
    }

    @Test
    public void testExtSignedPositive() {
        // 0x0000007F: bits 24-31 (from MSB) = 0x7F (positive in 8-bit signed)
        String program = """
                SEG
                MOVE W I H'0000007F', R0
                EXTS I 24, I 8, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(127, getRegister(1));
    }

    @Test
    public void testExtSignedNegative() {
        // 0x000000FF: bits 24-31 (from MSB) = 0xFF (negative in 8-bit signed = -1)
        String program = """
                SEG
                MOVE W I H'000000FF', R0
                EXTS I 24, I 8, R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(-1, getRegister(1));
    }

    @Test
    public void testInsertBits() {
        String program = """
                SEG
                MOVE W I H'FFFFFFFF', R0
                MOVE W I H'00000000', R1
                INS I 8, I 8, R1, R0
                HALT
                END
                """;
        assembleAndRun(program);
        // With LSB-0 bit numbering, bits 8-15 are cleared (second byte from right)
        assertEquals(0xFFFF00FF, getRegister(0));
    }

    @Test
    public void testInsertLowByte() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                MOVE W I H'000000AB', R1
                INS I 0, I 8, R1, R0
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x123456AB, getRegister(0));
    }

    @Test
    public void testInsertHighByte() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                MOVE W I H'000000CD', R1
                INS I 24, I 8, R1, R0
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xCD345678, getRegister(0));
    }

}
