package engine.state;

/**
 * MI machine status flags implementation.
 * Contains the four condition flags: Carry, Zero, Overflow, Negative.
 */
public class Flags implements StatusFlags {

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

    @Override
    public boolean isCarry() {
        return carry;
    }

    @Override
    public void setCarry(boolean carry) {
        this.carry = carry;
    }

    @Override
    public boolean isZero() {
        return zero;
    }

    @Override
    public void setZero(boolean zero) {
        this.zero = zero;
    }

    @Override
    public boolean isOverflow() {
        return overflow;
    }

    @Override
    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    @Override
    public boolean isNegative() {
        return negative;
    }

    @Override
    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    /**
     * Resets all flags to false.
     */
    public void reset() {
        carry = false;
        zero = false;
        overflow = false;
        negative = false;
    }
}
