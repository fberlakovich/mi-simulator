package engine.commands;

import engine.Machine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveATest extends InstructionTestBase {

    @Test
    public void testMoveAddressOfLabel() {
        String program = """
                SEG
                MOVEA data, R0
                HALT
        data:   DD W 42
                END
                """;
        assembleAndRun(program);
        int addr = getRegister(0);
        assertEquals(42, Machine.getInstance().getMemory().readInt(addr));
    }

    @Test
    public void testMoveAddressUsedForIndirectAccess() {
        String program = """
                SEG
                MOVEA data, R0
                MOVE W !R0, R1
                HALT
        data:   DD W 123
                END
                """;
        assembleAndRun(program);
        assertEquals(123, getRegister(1));
    }

    @Test
    public void testMoveAddressOfArray() {
        String program = """
                SEG
                MOVEA array, R0
                MOVE W !R0, R1
                MOVE W 4+!R0, R2
                MOVE W 8+!R0, R3
                HALT
        array:  DD W 10
                DD W 20
                DD W 30
                END
                """;
        assembleAndRun(program);
        assertEquals(10, getRegister(1));
        assertEquals(20, getRegister(2));
        assertEquals(30, getRegister(3));
    }

    @Test
    public void testMoveAddressForLoopAccess() {
        String program = """
                SEG
                MOVEA array, R0
                MOVE W I 0, R1
                MOVE W I 3, R2
                MOVE W I 0, R3
        loop:   CMP W R1, R2
                JGE done
                MOVE W R0, R4
                MOVE W R1, R5
                MULT W I 4, R5
                ADD W R5, R4
                MOVE W !R4, R6
                ADD W R6, R3
                ADD W I 1, R1
                JUMP loop
        done:   HALT
        array:  DD W 5
                DD W 10
                DD W 15
                END
                """;
        assembleAndRun(program);
        assertEquals(30, getRegister(3));
    }

    @Test
    public void testMoveAddressMultipleLabels() {
        String program = """
                SEG
                MOVEA l1, R0
                MOVEA l2, R1
                MOVEA l3, R2
                HALT
        l1:     DD W 1
        l2:     DD W 2
        l3:     DD W 3
                END
                """;
        assembleAndRun(program);
        int addr1 = getRegister(0);
        int addr2 = getRegister(1);
        int addr3 = getRegister(2);
        assertEquals(1, Machine.getInstance().getMemory().readInt(addr1));
        assertEquals(2, Machine.getInstance().getMemory().readInt(addr2));
        assertEquals(3, Machine.getInstance().getMemory().readInt(addr3));
    }

}
