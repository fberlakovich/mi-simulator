package engine.state;

/**
 * Interface for machine status flags (C, Z, V, N).
 */
public interface StatusFlags {

    /**
     * Checks if the Carry flag is set.
     *
     * @return true if Carry is set
     */
    boolean isCarry();

    /**
     * Sets the Carry flag.
     *
     * @param carry new value
     */
    void setCarry(boolean carry);

    /**
     * Checks if the Zero flag is set.
     *
     * @return true if Zero is set
     */
    boolean isZero();

    /**
     * Sets the Zero flag.
     *
     * @param zero new value
     */
    void setZero(boolean zero);

    /**
     * Checks if the Overflow flag is set.
     *
     * @return true if Overflow is set
     */
    boolean isOverflow();

    /**
     * Sets the Overflow flag.
     *
     * @param overflow new value
     */
    void setOverflow(boolean overflow);

    /**
     * Checks if the Negative flag is set.
     *
     * @return true if Negative is set
     */
    boolean isNegative();

    /**
     * Sets the Negative flag.
     *
     * @param negative new value
     */
    void setNegative(boolean negative);

    /**
     * Resets all flags to false.
     */
    default void reset() {
        setCarry(false);
        setZero(false);
        setOverflow(false);
        setNegative(false);
    }
}
