package engine.state;

import engine.events.FlagsChangeEvent;
import engine.events.MachineEventBus;

/**
 * MI machine status flags implementation.
 * Contains the four condition flags: Carry, Zero, Overflow, Negative.
 */
public class Flags implements StatusFlags {

    /**
     * Event bus for publishing flag change events.
     * Uses NO_OP by default until wired to the machine.
     */
    private MachineEventBus eventBus = MachineEventBus.NO_OP;

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
     * Sets the event bus for publishing flag change events.
     *
     * @param eventBus the event bus
     */
    public void setEventBus(MachineEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public boolean isCarry() {
        return carry;
    }

    @Override
    public void setCarry(boolean carry) {
        boolean old = this.carry;
        this.carry = carry;
        if (old != carry) {
            eventBus.publish(new FlagsChangeEvent(FlagsChangeEvent.Flag.CARRY, old, carry));
        }
    }

    @Override
    public boolean isZero() {
        return zero;
    }

    @Override
    public void setZero(boolean zero) {
        boolean old = this.zero;
        this.zero = zero;
        if (old != zero) {
            eventBus.publish(new FlagsChangeEvent(FlagsChangeEvent.Flag.ZERO, old, zero));
        }
    }

    @Override
    public boolean isOverflow() {
        return overflow;
    }

    @Override
    public void setOverflow(boolean overflow) {
        boolean old = this.overflow;
        this.overflow = overflow;
        if (old != overflow) {
            eventBus.publish(new FlagsChangeEvent(FlagsChangeEvent.Flag.OVERFLOW, old, overflow));
        }
    }

    @Override
    public boolean isNegative() {
        return negative;
    }

    @Override
    public void setNegative(boolean negative) {
        boolean old = this.negative;
        this.negative = negative;
        if (old != negative) {
            eventBus.publish(new FlagsChangeEvent(FlagsChangeEvent.Flag.NEGATIVE, old, negative));
        }
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
