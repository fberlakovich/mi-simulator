package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Jump instructions.
 */
public class JumpTest extends InstructionTestBase {

    @Test
    public void jump_unconditional() {
        // JUMP to label
        // R0 = 1. JUMP skip. R0 = 2. label skip: R0 = 3.
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "JUMP skip",
                "MOVE W I 2, R0",
                "skip: MOVE W I 3, R0"
        ));
        assertEquals(3, getRegister(0));
    }

    @Test
    public void jeq_taken() {
        // CMP 1, 1 -> Z=1. JEQ should jump.
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "CMP W R0, R0",
                "JEQ taken",
                "MOVE W I 2, R0",
                "taken: MOVE W I 3, R0"
        ));
        assertEquals(3, getRegister(0));
    }

    @Test
    public void jeq_not_taken() {
        // CMP 1, 2 -> Z=0. JEQ should NOT jump.
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "MOVE W I 2, R1",
                "CMP W R0, R1",
                "JEQ taken",
                "MOVE W I 3, R0",
                "JUMP end",
                "taken: MOVE W I 4, R0",
                "end: MOVE W I 0, R1"
        ));
        assertEquals(3, getRegister(0));
    }

    @Test
    public void jne_taken() {
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "MOVE W I 2, R1",
                "CMP W R0, R1", // Not Equal
                "JNE taken",
                "MOVE W I 0, R0",
                "taken: MOVE W I 1, R0"
        ));
        assertEquals(1, getRegister(0));
    }

    @Test
    public void jlt_taken() {
        // 1 < 2
        assembleAndRun(program(
                "MOVE W I 1, R0",
                "MOVE W I 2, R1",
                "CMP W R0, R1", 
                "JLT taken",
                "MOVE W I 0, R0",
                "taken: MOVE W I 1, R0"
        ));
        assertEquals(1, getRegister(0));
    }

    @Test
    public void jge_taken() {
        // 2 >= 1
        assembleAndRun(program(
                "MOVE W I 2, R0",
                "MOVE W I 1, R1",
                "CMP W R0, R1",
                "JGE taken",
                "MOVE W I 0, R0",
                "taken: MOVE W I 1, R0"
        ));
        assertEquals(1, getRegister(0));
    }
}
