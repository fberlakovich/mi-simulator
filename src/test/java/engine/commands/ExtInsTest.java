package engine.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtInsTest extends InstructionTestBase {

    @Test
    public void testExtractMiddleBits() {
        String program = """
                SEG
                MOVE W I H'FF00FF00', R0
                EXT R0, I 8, I 8, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xFF, getRegister(1));
    }

    @Test
    public void testExtractLowBits() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                EXT R0, I 0, I 16, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x5678, getRegister(1));
    }

    @Test
    public void testExtractHighBits() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                EXT R0, I 16, I 16, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x1234, getRegister(1));
    }

    @Test
    public void testExtractSingleBit() {
        String program = """
                SEG
                MOVE W I H'00000008', R0
                EXT R0, I 3, I 1, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(1, getRegister(1));
    }

    @Test
    public void testExtSignedPositive() {
        String program = """
                SEG
                MOVE W I H'0000007F', R0
                EXTS R0, I 0, I 8, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(127, getRegister(1));
    }

    @Test
    public void testExtSignedNegative() {
        String program = """
                SEG
                MOVE W I H'000000FF', R0
                EXTS R0, I 0, I 8, R1
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
                INS R1, I 8, I 8, R0, R2
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xFF00FFFF, getRegister(2));
    }

    @Test
    public void testInsertLowByte() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                MOVE W I H'000000AB', R1
                INS R1, I 0, I 8, R0, R2
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x123456AB, getRegister(2));
    }

    @Test
    public void testInsertHighByte() {
        String program = """
                SEG
                MOVE W I H'12345678', R0
                MOVE W I H'000000CD', R1
                INS R1, I 24, I 8, R0, R2
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xCD345678, getRegister(2));
    }

}
