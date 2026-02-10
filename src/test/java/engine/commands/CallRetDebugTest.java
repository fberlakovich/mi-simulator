package engine.commands;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests CALL behavior without RET (halting inside the function).
 * Complements CallRetTest which tests CALL+RET together.
 */
public class CallRetDebugTest extends InstructionTestBase {

    @Test(timeout = 2000)
    public void testCallWithoutReturn() {
        // CALL transfers control to func, which HALTs without returning.
        // The MOVE after CALL should NOT execute.
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
}
