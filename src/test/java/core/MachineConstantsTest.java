package core;

import org.junit.Test;

import static engine.MachineConstants.*;
import static org.junit.Assert.*;

/**
 * Tests for machine constants to ensure they match the MI specification.
 */
public class MachineConstantsTest {

    @Test
    public void memorySize_shouldBe1MB() {
        assertEquals("Memory should be 1MB (1048576 bytes)", 1048576, MEMORY_SIZE);
    }

    @Test
    public void registerCount_shouldBe16() {
        assertEquals("MI has 16 registers", 16, REGISTER_COUNT);
    }

    @Test
    public void stackPointer_shouldBeR14() {
        assertEquals("Stack pointer is R14", 14, SP_REGISTER);
    }

    @Test
    public void programCounter_shouldBeR15() {
        assertEquals("Program counter is R15", 15, PC_REGISTER);
    }

    @Test
    public void wordSize_shouldBe4Bytes() {
        assertEquals("Word size is 4 bytes (32 bits)", 4, WORD_SIZE);
    }

    @Test
    public void halfWordSize_shouldBe2Bytes() {
        assertEquals("Half-word size is 2 bytes (16 bits)", 2, HALF_WORD_SIZE);
    }

    @Test
    public void byteSize_shouldBe1Byte() {
        assertEquals("Byte size is 1 byte (8 bits)", 1, BYTE_SIZE);
    }

    @Test
    public void floatSize_shouldBe4Bytes() {
        assertEquals("Float size is 4 bytes", 4, FLOAT_SIZE);
    }

    @Test
    public void doubleSize_shouldBe8Bytes() {
        assertEquals("Double size is 8 bytes", 8, DOUBLE_SIZE);
    }
}
