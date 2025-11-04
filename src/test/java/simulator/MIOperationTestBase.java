package simulator;

import cli.MachineUtils;
import core.ProgramExecutor;
import enviroment.Enviroment;
import org.junit.Before;

/**
 * Base class for MI operation tests providing common test utilities
 */
public abstract class MIOperationTestBase {

    @Before
    public void setup() {
        Enviroment.init();
    }

    /**
     * Assembles, loads and executes a program until HALT
     *
     * @param program The MI assembly program to execute
     * @return true if the program was successfully assembled and loaded
     */
    protected boolean executeProgram(String program) {
        boolean loaded = MachineUtils.assembleAndLoad(program);
        if (!loaded) {
            return false;
        }

        // Use the core executor to run the program
        ProgramExecutor executor = new ProgramExecutor();
        Enviroment.readNextCommand(); // Initialize first command
        executor.executeProgram();
        
        return true;
    }

    /**
     * Gets the value of a register as an integer
     *
     * @param registerNumber The register number (0-15)
     * @param length The length to read (1, 2, or 4 bytes)
     * @return The register value
     */
    protected int getRegisterValue(int registerNumber, int length) {
        return Enviroment.REGISTERS.getRegister(registerNumber).getContentAsNumber(length);
    }

    /**
     * Gets the value of a register as a word (4 bytes)
     *
     * @param registerNumber The register number (0-15)
     * @return The register value
     */
    protected int getRegisterWord(int registerNumber) {
        return getRegisterValue(registerNumber, 4);
    }

    /**
     * Gets the value of a register as a byte (1 byte)
     *
     * @param registerNumber The register number (0-15)
     * @return The register value
     */
    protected int getRegisterByte(int registerNumber) {
        return getRegisterValue(registerNumber, 1);
    }

    /**
     * Checks if the Zero flag is set
     *
     * @return true if Zero flag is set
     */
    protected boolean isZeroFlag() {
        return Enviroment.flags.isZero();
    }

    /**
     * Checks if the Negative flag is set
     *
     * @return true if Negative flag is set
     */
    protected boolean isNegativeFlag() {
        return Enviroment.flags.isNegative();
    }

    /**
     * Checks if the Carry flag is set
     *
     * @return true if Carry flag is set
     */
    protected boolean isCarryFlag() {
        return Enviroment.flags.isCarry();
    }

    /**
     * Checks if the Overflow flag is set
     *
     * @return true if Overflow flag is set
     */
    protected boolean isOverflowFlag() {
        return Enviroment.flags.isOverflow();
    }
}
