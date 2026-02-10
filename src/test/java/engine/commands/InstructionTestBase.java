package engine.commands;

import cli.MachineUtils;
import engine.Machine;
import engine.MachineContext;
import engine.ProgramRunner;
import engine.util.NumberConversion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 * Base class for instruction unit tests.
 * Provides common setup and helper methods.
 */
public abstract class InstructionTestBase {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(120);

    protected MachineContext machine;

    @Before
    public void setUp() {
        Machine.resetInstance();
        machine = Machine.getInstance();
    }

    /**
     * Assembles and loads a program, executes until HALT.
     */
    protected void assembleAndRun(String program) {
        MachineUtils.assembleAndLoad(machine, program);
        ProgramRunner runner = machine.createRunner();
        while (runner.step()) {
            // step() returns false when program ends (HALT or no more commands)
        }
    }

    /**
     * Gets the value of a register as a signed 32-bit integer.
     */
    protected int getRegister(int regNum) {
        return machine.getRegisters().getRegister(regNum).getContentAsNumber(4);
    }

    /**
     * Gets the value of a register as a float.
     */
    protected float getRegisterAsFloat(int regNum) {
        int bits = NumberConversion.myBytetoIntWithoutSign(
                machine.getRegisters().getRegister(regNum).getContent(4));
        return Float.intBitsToFloat(bits);
    }

    /**
     * Gets the value of a register as a double.
     */
    protected double getRegisterAsDouble(int regNum) {
        long bits = NumberConversion.myBytetoLongWithoutSign(
                machine.getRegisters().getRegister(regNum).getContent(8));
        return Double.longBitsToDouble(bits);
    }

    /**
     * Gets the Zero flag.
     */
    protected boolean isZeroFlag() {
        return machine.getFlags().isZero();
    }

    /**
     * Gets the Negative flag.
     */
    protected boolean isNegativeFlag() {
        return machine.getFlags().isNegative();
    }

    /**
     * Gets the Overflow flag.
     */
    protected boolean isOverflowFlag() {
        return machine.getFlags().isOverflow();
    }

    /**
     * Gets the Carry flag.
     */
    protected boolean isCarryFlag() {
        return machine.getFlags().isCarry();
    }

    /**
     * Creates a simple MI program with SEG/END wrapper.
     */
    protected String program(String... lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("          SEG\n");
        for (String line : lines) {
            sb.append("          ").append(line).append("\n");
        }
        sb.append("          HALT\n");
        sb.append("          END\n");
        return sb.toString();
    }
}