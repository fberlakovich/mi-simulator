package engine.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CallRetTest extends InstructionTestBase {

    @Test(timeout = 5000) // 5 second timeout to detect hangs
    public void testSimpleCallAndReturn() {
        String program = """
                SEG
                MOVE W I 10, R0
                CALL func
                MOVE W I 99, R1
                HALT
        func:   MOVE W I 20, R2
                RET
                END
                """;
        assembleAndRun(program);
        assertEquals(10, getRegister(0));
        assertEquals(99, getRegister(1));
        assertEquals(20, getRegister(2));
    }

    @Test(timeout = 5000)
    public void testNestedCalls() {
        String program = """
                SEG
                MOVE W I 1, R0
                CALL f1
                HALT
        f1:     ADD W I 10, R0
                CALL f2
                ADD W I 100, R0
                RET
        f2:     ADD W I 1000, R0
                RET
                END
                """;
        assembleAndRun(program);
        assertEquals(1111, getRegister(0));
    }

    @Test(timeout = 5000)
    public void testCallPreservesRegisters() {
        String program = """
                SEG
                MOVE W I 5, R0
                MOVE W I 7, R1
                CALL func
                HALT
        func:   MOVE W I 99, R2
                RET
                END
                """;
        assembleAndRun(program);
        assertEquals(5, getRegister(0));
        assertEquals(7, getRegister(1));
        assertEquals(99, getRegister(2));
    }

    @Test(timeout = 5000)
    public void testCallWithStackManipulation() {
        String program = """
                SEG
                MOVE W I 10, R0
                MOVE W I 20, R1
                CALL func
                HALT
        func:   PUSHR
                MOVE W I 99, R0
                MOVE W I 88, R1
                POPR
                RET
                END
                """;
        assembleAndRun(program);
        assertEquals(10, getRegister(0));
        assertEquals(20, getRegister(1));
    }
}
