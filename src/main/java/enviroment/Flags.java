package enviroment;

/**
 * Diese Klasse repraesentiert die Flags der MI
 */
public class Flags {

    /**
     * Carry-Flag
     */
    private boolean carry = false;

    /**
     * Zero-Flag.
     */
    private boolean zero = false;

    /**
     * Overflow-Flag
     */
    private boolean overflow = false;

    /**
     * Negative-Flag
     */
    private boolean negative = false;

    /**
     * Prueft ob das Carry-Flag gesetzt ist
     *
     * @return Carry-Flag gesetzt
     */
    public boolean isCarry() {
        return carry;
    }

    /**
     * Prueft ob das Negative-Flag gesetzt ist
     *
     * @return Negative-Flag gesetzt
     */
    public boolean isNegative() {
        return negative;
    }

    /**
     * Prueft ob das Overflow-Flag gesetzt ist
     *
     * @return Overflow-Flag gesetzt
     */
    public boolean isOverflow() {
        return overflow;
    }

    /**
     * Prueft ob das Zero-Flag gesetzt ist
     *
     * @return Zero-Flag gesetzt
     */
    public boolean isZero() {
        return zero;
    }

    /**
     * Setzt das Carry-Flag
     *
     * @param carry Carry-Flag
     */

    public void setCarry(boolean carry) {
        this.carry = carry;
    }

    /**
     * Setzt das Negative-Flag
     *
     * @param negative Negative-Flag
     */
    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    /**
     * Setzt das Overflow-Flag
     *
     * @param overflow Overflow-Flag
     */
    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    /**
     * Setzt das Zero-Flag
     *
     * @param zero Zero-Flag
     */
    public void setZero(boolean zero) {
        this.zero = zero;
    }

}
