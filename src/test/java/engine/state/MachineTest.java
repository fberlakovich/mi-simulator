package engine.state;

import engine.Machine;
import org.junit.Before;
import org.junit.Test;

import static engine.MachineConstants.MEMORY_SIZE;
import static engine.MachineConstants.SP_REGISTER;
import static org.junit.Assert.*;

/**
 * Tests for the Machine class.
 */
public class MachineTest {

    private Machine machine;

    @Before
    public void setUp() {
        machine = new Machine();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, machine.getPC());
        assertEquals(0, machine.getStackBegin());
        assertNotNull(machine.getMemory());
        assertNotNull(machine.getRegisters());
        assertNotNull(machine.getFlags());
        assertNotNull(machine.getEventBus());
        assertNotNull(machine.getBreakpointManager());
    }

    @Test
    public void testPCOperations() {
        assertEquals(0, machine.getPC());

        machine.setPC(100);
        assertEquals(100, machine.getPC());

        machine.addToPC(50);
        assertEquals(150, machine.getPC());

        machine.addToPC(-30);
        assertEquals(120, machine.getPC());
    }

    @Test
    public void testFetchByte() {
        // Write some bytes to memory at address 0
        machine.getMemory().writeByte(0, (byte) 0x42);
        machine.getMemory().writeByte(1, (byte) 0x43);
        machine.getMemory().writeByte(2, (byte) 0x44);

        machine.setPC(0);

        assertEquals(0x42, machine.fetchByte());
        assertEquals(1, machine.getPC());

        assertEquals(0x43, machine.fetchByte());
        assertEquals(2, machine.getPC());

        assertEquals(0x44, machine.fetchByte());
        assertEquals(3, machine.getPC());
    }

    @Test
    public void testReadByteAtPC() {
        machine.getMemory().writeByte(10, (byte) 0xAB);
        machine.setPC(10);

        // Should read without advancing PC
        assertEquals(0xAB, machine.readByteAtPC());
        assertEquals(10, machine.getPC());

        // Reading again should give same result
        assertEquals(0xAB, machine.readByteAtPC());
        assertEquals(10, machine.getPC());
    }

    @Test
    public void testReset() {
        // Modify machine state
        machine.setPC(1000);
        machine.setStackBegin(500);
        machine.getFlags().setCarry(true);
        machine.getFlags().setZero(true);
        machine.getRegisters().getRegister(0).setContentAsNumber(42);
        machine.getMemory().writeByte(0, (byte) 0xFF);

        // Reset
        machine.reset();

        // Verify state is reset
        assertEquals(0, machine.getPC());
        // Stack begins at top of memory (stack grows downward)
        assertEquals(MEMORY_SIZE, machine.getStackBegin());
        // Stack pointer (R14) is initialized to top of memory
        assertEquals(MEMORY_SIZE, machine.getRegisters().getRegister(SP_REGISTER).getContentAsNumber(4));
        assertFalse(machine.getFlags().isCarry());
        assertFalse(machine.getFlags().isZero());
        assertEquals(0, machine.getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(0, machine.getMemory().readByte(0));
    }

    @Test
    public void testRegisterCount() {
        assertEquals(16, machine.getRegisters().getRegisterCount());
    }

    @Test
    public void testMemorySize() {
        assertEquals(1048576, machine.getMemory().getSize()); // 1 MB
    }
}
