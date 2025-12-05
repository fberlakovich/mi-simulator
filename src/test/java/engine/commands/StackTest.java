package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Stack instructions: PUSHR, POPR, CALL, RET.
 */
public class StackTest extends InstructionTestBase {

    @Test
    public void pushr_popr() {
        // Set R0, R1, R2
        // PUSHR
        // Clear R0, R1, R2
        // POPR
        // Check R0, R1, R2 restored
        
        // PUSHR pushes R0..R14. 
        // POPR pops R0..R14.
        
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

    @Test
    public void call_ret() {
        // CALL sub
        // sub: RET
        
        // R0 = 1. CALL sub. R0 = 2.
        // sub: MOVE W I 3, R0. RET.
        
        assembleAndRun(program(
                "MOVE W I 1000, SP", // Initialize SP
                "MOVE W I 1, R0",
                "CALL sub",
                "MOVE W I 2, R0",
                "JUMP end",
                "sub: MOVE W I 3, R0",
                "RET",
                "end: MOVE W I 0, R1"
        ));
        assertEquals(2, getRegister(0));
    }
}
