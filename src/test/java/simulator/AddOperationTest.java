package simulator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for ADD operation with various data sizes and addressing modes
 */
public class AddOperationTest extends MIOperationTestBase {

    @Test
    public void testAddByteImmediate() {
        String program = """
            SEG
            MOVE B I 5, R1
            MOVE B I 3, R2
            ADD B R1, R2
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // ADD B R1, R2 performs R2 = R2 + R1, so R2 should contain 3 + 5 = 8
        assertEquals(8, getRegisterByte(2));
    }

    @Test
    public void testAddWordImmediate() {
        String program = """
            SEG
            MOVE W I 100, R1
            MOVE W I 200, R2
            ADD W R1, R2
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // R2 should now contain 100 + 200 = 300
        assertEquals(300, getRegisterWord(2));
    }

    @Test
    public void testAddThreeAddress() {
        String program = """
            SEG
            MOVE W I 50, R1
            MOVE W I 30, R2
            ADD W R1, R2, R3
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // R3 should now contain 50 + 30 = 80
        assertEquals(80, getRegisterWord(3));
        
        // R1 and R2 should be unchanged
        assertEquals(50, getRegisterWord(1));
        assertEquals(30, getRegisterWord(2));
    }

    @Test
    public void testAddWithCarryFlag() {
        String program = """
            SEG
            MOVE B I 255, R1
            MOVE B I 1, R2
            ADD B R1, R2
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // Byte addition: 255 + 1 = 256, which exceeds 8-bit range (255)
        // Result wraps around to 0 and carry flag is set
        assertEquals(0, getRegisterByte(2));
        assertTrue("Carry flag should be set", isCarryFlag());
    }

    @Test
    public void testAddNegativeNumbers() {
        String program = """
            SEG
            MOVE W I -10, R1
            MOVE W I -5, R2
            ADD W R1, R2
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // R2 should now contain -10 + (-5) = -15
        assertEquals(-15, getRegisterWord(2));
    }

    @Test
    public void testAddWithZeroFlag() {
        String program = """
            SEG
            MOVE W I 5, R1
            MOVE W I -5, R2
            ADD W R1, R2
            HALT
            """;

        assertTrue(executeProgram(program));
        
        // R2 should now contain 5 + (-5) = 0
        assertEquals(0, getRegisterWord(2));
        assertTrue("Zero flag should be set", isZeroFlag());
    }
}
