package cli;

import engine.Machine;
import engine.MachineContext;
import engine.ProgramRunner;
import engine.util.NumberConversion;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static engine.MachineConstants.REGISTER_COUNT;
import static engine.MachineConstants.MEMORY_SIZE;
import static engine.MachineConstants.SP_REGISTER;
import static org.junit.Assert.*;

/**
 * Unit tests for QuietMachine.
 */
public class QuietMachineTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private MachineContext context;

    @Before
    public void setUp() {
        Machine.resetInstance();
        context = Machine.getInstance();
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
    }

    @Test
    public void printRegisterState_allZeros_decimal() {
        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);
        machine.printRegisterState();

        String output = outputStream.toString();
        String[] lines = output.split("\n");
        assertEquals(REGISTER_COUNT, lines.length);

        for (int i = 0; i < REGISTER_COUNT; i++) {
            if (i == SP_REGISTER) {
                // Stack pointer is initialized to top of memory
                assertEquals("R" + i + ": " + MEMORY_SIZE, lines[i].trim());
            } else {
                assertEquals("R" + i + ": 0", lines[i].trim());
            }
        }
    }

    @Test
    public void printRegisterState_allZeros_hex() {
        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, true);
        machine.printRegisterState();

        String output = outputStream.toString();
        String[] lines = output.split("\n");
        assertEquals(REGISTER_COUNT, lines.length);

        for (int i = 0; i < REGISTER_COUNT; i++) {
            if (i == SP_REGISTER) {
                // Stack pointer is initialized to top of memory (0x100000)
                assertEquals("R" + i + ": 0x" + Integer.toHexString(MEMORY_SIZE).toUpperCase(), lines[i].trim());
            } else {
                assertEquals("R" + i + ": 0x0", lines[i].trim());
            }
        }
    }

    @Test
    public void printRegisterState_withValues_decimal() {
        // Set some register values
        context.getRegisters().getRegister(0).setContent(
                NumberConversion.intToByte(100, 4));
        context.getRegisters().getRegister(1).setContent(
                NumberConversion.intToByte(-50, 4));
        context.getRegisters().getRegister(15).setContent(
                NumberConversion.intToByte(256, 4));

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);
        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 100"));
        assertTrue(output.contains("R1: -50"));
        assertTrue(output.contains("R15: 256"));
    }

    @Test
    public void printRegisterState_withValues_hex() {
        // Set some register values
        context.getRegisters().getRegister(0).setContent(
                NumberConversion.intToByte(255, 4));
        context.getRegisters().getRegister(1).setContent(
                NumberConversion.intToByte(4096, 4));

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, true);
        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 0xFF"));
        assertTrue(output.contains("R1: 0x1000"));
    }

    @Test
    public void executeNext_runsProgram() {
        // Load a simple program
        MachineUtils.assembleAndLoad(context, "SEG\nMOVE W I 42, R0\nHALT\nEND");

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);

        // Execute MOVE instruction
        assertFalse(machine.hasHalted());
        machine.executeNext();

        // Execute HALT instruction
        machine.executeNext();
        assertTrue(machine.hasHalted());

        // Check register was set
        assertEquals(42, context.getRegisters().getRegister(0).getContentAsNumber(4));
    }

    @Test
    public void hasHalted_tracksExecutionState() {
        MachineUtils.assembleAndLoad(context, "SEG\nHALT\nEND");

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);
        assertFalse(machine.hasHalted());

        machine.executeNext(); // HALT
        assertTrue(machine.hasHalted());
    }

    @Test
    public void quietMachine_noOutputDuringExecution() {
        // QuietMachine should not output anything during execution,
        // only when printRegisterState is called
        MachineUtils.assembleAndLoad(context, "SEG\nMOVE W I 42, R0\nMOVE W I 100, R1\nHALT\nEND");

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);

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
        MachineUtils.assembleAndLoad(context, "SEG\nMOVE W I 42, R0\nADD W R0, R0, R1\nHALT\nEND");

        ProgramRunner runner = context.createRunner();
        QuietMachine machine = new QuietMachine(context, runner, printStream, false);

        while (!machine.hasHalted()) {
            machine.executeNext();
        }

        machine.printRegisterState();

        String output = outputStream.toString();
        assertTrue(output.contains("R0: 42"));
        assertTrue(output.contains("R1: 84"));  // 42 + 42
    }
}