package cli;

import engine.Machine;
import engine.ProgramRunner;
import engine.util.NumberConversion;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static engine.MachineConstants.REGISTER_COUNT;
import static org.junit.Assert.*;

/**
 * Unit tests for QuietMachine.
 */
public class QuietMachineTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    @Before
    public void setUp() {
        Machine.resetInstance();
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
    }

    @Test
    public void printRegisterState_allZeros_decimal() {
        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);
        machine.printRegisterState();

        String output = outputStream.toString();
        String[] lines = output.split("\n");
        assertEquals(REGISTER_COUNT, lines.length);

        for (int i = 0; i < REGISTER_COUNT; i++) {
            assertEquals("R" + i + ": 0", lines[i].trim());
        }
    }

    @Test
    public void printRegisterState_allZeros_hex() {
        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, true);
        machine.printRegisterState();

        String output = outputStream.toString();
        String[] lines = output.split("\n");
        assertEquals(REGISTER_COUNT, lines.length);

        for (int i = 0; i < REGISTER_COUNT; i++) {
            assertEquals("R" + i + ": 0x0", lines[i].trim());
        }
    }

    @Test
    public void printRegisterState_withValues_decimal() {
        // Set some register values
        Machine.getInstance().getRegisters().getRegister(0).setContent(
                NumberConversion.intToByte(100, 4));
        Machine.getInstance().getRegisters().getRegister(1).setContent(
                NumberConversion.intToByte(-50, 4));
        Machine.getInstance().getRegisters().getRegister(15).setContent(
                NumberConversion.intToByte(256, 4));

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);
        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 100"));
        assertTrue(output.contains("R1: -50"));
        assertTrue(output.contains("R15: 256"));
    }

    @Test
    public void printRegisterState_withValues_hex() {
        // Set some register values
        Machine.getInstance().getRegisters().getRegister(0).setContent(
                NumberConversion.intToByte(255, 4));
        Machine.getInstance().getRegisters().getRegister(1).setContent(
                NumberConversion.intToByte(4096, 4));

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, true);
        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 0xFF"));
        assertTrue(output.contains("R1: 0x1000"));
    }

    @Test
    public void executeNext_runsProgram() {
        // Load a simple program
        MachineUtils.assembleAndLoad("SEG\nMOVE W I 42, R0\nHALT\nEND");

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);

        // Execute MOVE instruction
        assertFalse(machine.hasHalted());
        machine.executeNext();

        // Execute HALT instruction
        machine.executeNext();
        assertTrue(machine.hasHalted());

        // Check register was set
        assertEquals(42, Machine.getInstance().getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void hasHalted_tracksExecutionState() {
        MachineUtils.assembleAndLoad("SEG\nHALT\nEND");

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);
        assertFalse(machine.hasHalted());

        machine.executeNext(); // HALT
        assertTrue(machine.hasHalted());
    }

    @Test
    public void quietMachine_noOutputDuringExecution() {
        // QuietMachine should not output anything during execution,
        // only when printRegisterState is called
        MachineUtils.assembleAndLoad("SEG\nMOVE W I 42, R0\nMOVE W I 100, R1\nHALT\nEND");

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);

        while (!machine.hasHalted()) {
            machine.executeNext();
        }

        // No output should have been produced yet
        assertEquals("", outputStream.toString());

        // Now print register state
        machine.printRegisterState();
        assertFalse(outputStream.toString().isEmpty());
    }

    @Test
    public void printRegisterState_afterProgram_showsFinalState() {
        MachineUtils.assembleAndLoad("SEG\nMOVE W I 42, R0\nADD W R0, R0, R1\nHALT\nEND");

        ProgramRunner runner = Machine.getInstance().createRunner();
        QuietMachine machine = new QuietMachine(runner, printStream, false);

        while (!machine.hasHalted()) {
            machine.executeNext();
        }

        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 42"));
        assertTrue(output.contains("R1: 84"));  // 42 + 42
    }
}