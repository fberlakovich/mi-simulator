package engine.commands;

import org.junit.Test;
import static org.junit.Assert.*;

public class CallRetDebugTest extends InstructionTestBase {

    @Test(timeout = 2000)
    public void testJustCall() {
        // Test if CALL works without RET
        String program = """
                SEG
                MOVE W I 10, R0
                CALL func
                MOVE W I 99, R1
                HALT
        func:   MOVE W I 20, R2
                HALT
                END
                """;
        assembleAndRun(program);
        assertEquals(10, getRegister(0));
        assertEquals(0, getRegister(1));  // Should not reach the MOVE after CALL
        assertEquals(20, getRegister(2));  // Should execute func
    }

    @Test(timeout = 2000)
    public void testCallAndRet() {
        // Test if CALL and RET work together
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
        assertEquals(99, getRegister(1));  // Should reach this after RET
        assertEquals(20, getRegister(2));
    }
}
