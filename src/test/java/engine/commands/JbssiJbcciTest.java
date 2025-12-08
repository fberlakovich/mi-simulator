package engine.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JbssiJbcciTest extends InstructionTestBase {

    @Test
    public void testJbssiBitSetAndJumps() {
        String program = """
                SEG
                MOVE W I H'00000008', R0
                MOVE W I 0, R1
                JBSSI 3, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
        assertEquals(0x0000000C, getRegister(0));
    }

    @Test
    public void testJbssiBitNotSetNoJump() {
        String program = """
                SEG
                MOVE W I H'00000000', R0
                MOVE W I 0, R1
                JBSSI 3, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(99, getRegister(1));
        assertEquals(0x0000000C, getRegister(0));
    }

    @Test
    public void testJbcciBitClearAndJumps() {
        String program = """
                SEG
                MOVE W I H'FFFFFFF7', R0
                MOVE W I 0, R1
                JBCCI 3, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
        assertEquals(0xFFFFFFF3, getRegister(0));
    }

    @Test
    public void testJbcciBitNotClearNoJump() {
        String program = """
                SEG
                MOVE W I H'FFFFFFFF', R0
                MOVE W I 0, R1
                JBCCI 3, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(99, getRegister(1));
        assertEquals(0xFFFFFFFB, getRegister(0));
    }

    @Test
    public void testJbssiMultipleBits() {
        String program = """
                SEG
                MOVE W I H'00000000', R0
                JBSSI 0, R0, l1
        l1:     JBSSI 1, R0, l2
        l2:     JBSSI 2, R0, l3
        l3:     HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0x0000000F, getRegister(0));
    }

    @Test
    public void testJbcciMultipleBits() {
        String program = """
                SEG
                MOVE W I H'FFFFFFFF', R0
                JBCCI 0, R0, l1
        l1:     JBCCI 1, R0, l2
        l2:     JBCCI 2, R0, l3
        l3:     HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0xFFFFFFF0, getRegister(0));
    }

    @Test
    public void testJbssiHighBit() {
        String program = """
                SEG
                MOVE W I H'80000000', R0
                MOVE W I 0, R1
                JBSSI 31, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
    }

    @Test
    public void testJbcciLowBit() {
        String program = """
                SEG
                MOVE W I H'00000000', R0
                MOVE W I 0, R1
                JBCCI 0, R0, skip
                MOVE W I 99, R1
        skip:   HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(0, getRegister(1));
    }

}
