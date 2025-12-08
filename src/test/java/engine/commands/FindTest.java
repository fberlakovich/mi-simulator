package engine.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FindTest extends InstructionTestBase {

    @Test
    public void testFindsFirstSetBit() {
        String program = """
                SEG
                MOVE W I H'00000008', R0
                FINDS R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(3, getRegister(1));
    }

    @Test
    public void testFindsLowestBitSet() {
        String program = """
                SEG
                MOVE W I H'00000001', R0
                FINDS R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
    }

    @Test
    public void testFindsHighestBit() {
        String program = """
                SEG
                MOVE W I H'80000000', R0
                FINDS R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(31, getRegister(1));
    }

    @Test
    public void testFindsMultipleBitsSetReturnsFirst() {
        String program = """
                SEG
                MOVE W I H'00000018', R0
                FINDS R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(3, getRegister(1));
    }

    @Test
    public void testFindsNoBitSetReturnsNegative() {
        String program = """
                SEG
                MOVE W I H'00000000', R0
                FINDS R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(-1, getRegister(1));
    }

    @Test
    public void testFindcFirstClearBit() {
        String program = """
                SEG
                MOVE W I H'FFFFFFF7', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(3, getRegister(1));
    }

    @Test
    public void testFindcLowestBitClear() {
        String program = """
                SEG
                MOVE W I H'FFFFFFFE', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
    }

    @Test
    public void testFindcHighestBitClear() {
        String program = """
                SEG
                MOVE W I H'7FFFFFFF', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(31, getRegister(1));
    }

    @Test
    public void testFindcMultipleBitsClearReturnsFirst() {
        String program = """
                SEG
                MOVE W I H'FFFFFFE7', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(3, getRegister(1));
    }

    @Test
    public void testFindcAllBitsSetReturnsNegative() {
        String program = """
                SEG
                MOVE W I H'FFFFFFFF', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(-1, getRegister(1));
    }

    @Test
    public void testFindcAllBitsClearReturnsZero() {
        String program = """
                SEG
                MOVE W I H'00000000', R0
                FINDC R0, R1
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
    }

}
