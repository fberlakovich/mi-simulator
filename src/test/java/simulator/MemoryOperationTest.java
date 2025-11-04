package simulator;

import enviroment.Enviroment;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryOperationTest extends MIOperationTestBase {

    @Test
    public void testStoreAndLoadWord() {
        assertTrue(executeProgram("SEG\nJUMP start\ndata: DD W 0\nstart:\nMOVE W I 1234, R1\nMOVE W R1, data\nMOVE W data, R2\nHALT"));
        assertEquals(1234, getRegisterWord(2));
    }

    @Test
    public void testDataDefinitionWord() {
        assertTrue(executeProgram("SEG\nJUMP start\ndata: DD W 5678\nstart:\nMOVE W data, R1\nHALT"));
        assertEquals(5678, getRegisterWord(1));
    }

    @Test
    public void testMultipleDataDefinitions() {
        assertTrue(executeProgram("SEG\nJUMP start\nval1: DD W 10\nval2: DD W 20\nval3: DD W 30\nstart:\nMOVE W val1, R1\nMOVE W val2, R2\nMOVE W val3, R3\nHALT"));
        assertEquals(10, getRegisterWord(1));
        assertEquals(20, getRegisterWord(2));
        assertEquals(30, getRegisterWord(3));
    }

    @Test
    public void testMemoryWrite() {
        assertTrue(executeProgram("SEG\nMOVE W I 777, R1\nMOVE W R1, 1000\nHALT"));
        int memValue = Enviroment.MEMORY.getContentAsInt(1000, 4);
        assertEquals(777, memValue);
    }
}
