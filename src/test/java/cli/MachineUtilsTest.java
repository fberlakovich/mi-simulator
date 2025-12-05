package cli;

import engine.Machine;
import org.junit.Before;
import org.junit.Test;

import static engine.MachineConstants.MEMORY_SIZE;
import static engine.MachineConstants.REGISTER_COUNT;
import static org.junit.Assert.*;

/**
 * Unit tests for MachineUtils state loading functionality.
 */
public class MachineUtilsTest {

    @Before
    public void setUp() {
        Machine.resetInstance();
    }

    // ==================== Register Loading ====================

    @Test
    public void loadState_singleRegister_decimal() {
        MachineUtils.loadState("R0: 42", false);
        assertEquals(42, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void loadState_singleRegister_hex() {
        MachineUtils.loadState("R0: FF", true);
        assertEquals(255, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void loadState_registerWithHexPrefix() {
        // 0x prefix should work regardless of radix setting
        MachineUtils.loadState("R0: 0xFF", false);
        assertEquals(255, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void loadState_multipleRegisters() {
        MachineUtils.loadState("R0: 10\nR1: 20\nR2: 30", false);
        assertEquals(10, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(20, Machine.getInstance().getRegisters().getRegister(1).getContentAsNumber(4));
        assertEquals(30, Machine.getInstance().getRegisters().getRegister(2).getContentAsNumber(4));
    }

    @Test
    public void loadState_allRegisters() {
        StringBuilder state = new StringBuilder();
        for (int i = 0; i < REGISTER_COUNT; i++) {
            state.append("R").append(i).append(": ").append(i * 10).append("\n");
        }
        MachineUtils.loadState(state.toString(), false);
        for (int i = 0; i < REGISTER_COUNT; i++) {
            assertEquals(i * 10, Machine.getInstance().getRegisters().getRegister(i).getContentAsNumber(4));
        }
    }

    @Test
    public void loadState_negativeRegisterValue() {
        MachineUtils.loadState("R0: -100", false);
        assertEquals(-100, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_invalidRegisterNumber_tooHigh() {
        MachineUtils.loadState("R16: 42", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_invalidRegisterNumber_negative() {
        MachineUtils.loadState("R-1: 42", false);
    }

    // ==================== Flag Loading ====================

    @Test
    public void loadState_flags_allSet() {
        MachineUtils.loadState("C: 1\nV: 1\nZ: 1\nN: 1", false);
        assertTrue(Machine.getInstance().getFlags().isCarry());
        assertTrue(Machine.getInstance().getFlags().isOverflow());
        assertTrue(Machine.getInstance().getFlags().isZero());
        assertTrue(Machine.getInstance().getFlags().isNegative());
    }

    @Test
    public void loadState_flags_allClear() {
        // Set all flags first, then clear them
        Machine.getInstance().getFlags().setCarry(true);
        Machine.getInstance().getFlags().setOverflow(true);
        Machine.getInstance().getFlags().setZero(true);
        Machine.getInstance().getFlags().setNegative(true);

        MachineUtils.loadState("C: 0\nV: 0\nZ: 0\nN: 0", false);
        assertFalse(Machine.getInstance().getFlags().isCarry());
        assertFalse(Machine.getInstance().getFlags().isOverflow());
        assertFalse(Machine.getInstance().getFlags().isZero());
        assertFalse(Machine.getInstance().getFlags().isNegative());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_flags_invalidValue() {
        MachineUtils.loadState("C: 2", false);
    }

    // ==================== Memory Loading ====================

    @Test
    public void loadState_memoryByte_decimal() {
        MachineUtils.loadState("100: 42", false);
        assertEquals(42, Machine.getInstance().getMemory().getContent(100, 1)[0].getContent());
    }

    @Test
    public void loadState_memoryByte_hex() {
        // When useHex=true, both address and value are in hex
        // 64 in hex = 100 in decimal, FF in hex = 255 in decimal
        MachineUtils.loadState("64: FF", true);
        int actual = Machine.getInstance().getMemory().getContent(0x64, 1)[0].getContent();
        assertEquals("Memory at 0x64 should be 0xFF (-1 as signed byte)", (byte) 0xFF, (byte) actual);
    }

    @Test
    public void loadState_memoryWithHexPrefix() {
        // 0x prefix forces hex regardless of useHex setting
        MachineUtils.loadState("0x64: 0xFF", false);
        int actual = Machine.getInstance().getMemory().getContent(0x64, 1)[0].getContent();
        assertEquals("Memory at 0x64 should be 0xFF (-1 as signed byte)", (byte) 0xFF, (byte) actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_memoryAddress_negative() {
        MachineUtils.loadState("-1: 42", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_memoryAddress_tooLarge() {
        MachineUtils.loadState(MEMORY_SIZE + ": 42", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_memoryValue_tooLarge() {
        MachineUtils.loadState("100: 256", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_memoryValue_tooNegative() {
        MachineUtils.loadState("100: -129", false);
    }

    // ==================== Comment and Format Support ====================

    @Test
    public void loadState_commentsIgnored() {
        MachineUtils.loadState("# This is a comment\nR0: 42\n# Another comment", false);
        assertEquals(42, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void loadState_emptyLinesIgnored() {
        MachineUtils.loadState("R0: 10\n\n\nR1: 20\n\n", false);
        assertEquals(10, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(20, Machine.getInstance().getRegisters().getRegister(1).getContentAsNumber(4));
    }

    @Test
    public void loadState_semicolonSeparator() {
        MachineUtils.loadState("R0: 10; R1: 20; R2: 30", false);
        assertEquals(10, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(20, Machine.getInstance().getRegisters().getRegister(1).getContentAsNumber(4));
        assertEquals(30, Machine.getInstance().getRegisters().getRegister(2).getContentAsNumber(4));
    }

    @Test
    public void loadState_transitionFormat() {
        // Format: "R0: 0 -> 42" uses the "after" value
        MachineUtils.loadState("R0: 0 -> 42", false);
        assertEquals(42, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void loadState_whitespaceHandling() {
        MachineUtils.loadState("  R0  :   42  ", false);
        assertEquals(42, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    // ==================== Error Cases ====================

    @Test(expected = IllegalArgumentException.class)
    public void loadState_invalidFormat_noColon() {
        MachineUtils.loadState("R0 42", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_invalidNumber() {
        MachineUtils.loadState("R0: abc", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadState_invalidTransitionFormat() {
        MachineUtils.loadState("R0: 0 -> 42 -> 100", false);
    }

    // ==================== Mixed State Loading ====================

    @Test
    public void loadState_mixedRegistersAndFlags() {
        MachineUtils.loadState("R0: 100\nR1: 200\nC: 1\nZ: 1", false);
        assertEquals(100, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(200, Machine.getInstance().getRegisters().getRegister(1).getContentAsNumber(4));
        assertTrue(Machine.getInstance().getFlags().isCarry());
        assertTrue(Machine.getInstance().getFlags().isZero());
    }

    @Test
    public void loadState_completeStateFile() {
        // Note: inline comments not supported, only line-start comments
        String state = """
                # Initial machine state
                R0: 0x10
                R1: 0x20
                R14: 0xFFFF0
                R15: 0x100

                # Flags
                C: 0
                V: 0
                Z: 1
                N: 0

                # Memory at address 0x200
                0x200: 0xAB
                """;
        MachineUtils.loadState(state, false);
        assertEquals(0x10, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
        assertEquals(0x20, Machine.getInstance().getRegisters().getRegister(1).getContentAsNumber(4));
        assertEquals(0xFFFF0, Machine.getInstance().getRegisters().getRegister(14).getContentAsNumber(4));
        assertEquals(0x100, Machine.getInstance().getRegisters().getRegister(15).getContentAsNumber(4));
        assertFalse(Machine.getInstance().getFlags().isCarry());
        assertTrue(Machine.getInstance().getFlags().isZero());
        assertEquals((byte) 0xAB, (byte) Machine.getInstance().getMemory().getContent(0x200, 1)[0].getContent());
    }
}
