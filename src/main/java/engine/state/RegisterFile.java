package engine.state;

/**
 * Interface for machine register file (set of registers).
 * Allows different implementations for testing or alternative backends.
 */
public interface RegisterFile {

    /**
     * Gets a register by number.
     *
     * @param nr register number (0-15)
     * @return the register
     */
    Register getRegister(int nr);

    /**
     * Gets the number of registers.
     *
     * @return register count
     */
    int getRegisterCount();
}
