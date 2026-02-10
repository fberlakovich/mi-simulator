package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for PUSHR/POPR instructions.
 * CALL/RET tests are in CallRetTest and CallRetDebugTest.
 */
public class StackTest extends InstructionTestBase {

    @Test
    public void pushr_popr() {
        assembleAndRun(program(
                "MOVE W I 1000, SP", // Initialize SP
                "MOVE W I 10, R0",
                "MOVE W I 20, R1",
                "PUSHR",
                "CLEAR W R0",
                "CLEAR W R1",
                "POPR"
        ));
        assertEquals(10, getRegister(0));
        assertEquals(20, getRegister(1));
    }
}
