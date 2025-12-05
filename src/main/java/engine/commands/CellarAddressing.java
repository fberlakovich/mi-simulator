package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

/**
 * Operand specification for stack (cellar) addressing mode.
 * Supports pre-decrement (-!Rn) and post-increment (!Rn+) operations.
 */
public class CellarAddressing implements Operand {

    /** The machine this operand operates on */
    private final Machine machine;

    /** The register used as stack pointer */
    private RegisterAddressing register;

    /** Access length in bytes */
    private int length;

    /** Offset for stack adjustment */
    private int offset;

    /** true for post-increment (!Rn+), false for pre-decrement (-!Rn) */
    private boolean plus;

    /** Register number */
    private int nr;

    /**
     * Creates a new cellar addressing operand.
     *
     * @param machine  the machine this operand operates on
     * @param register register number to use as stack pointer
     * @param length   access length in bytes
     * @param offset   offset for stack adjustment
     * @param plus     true for post-increment, false for pre-decrement
     */
    public CellarAddressing(Machine machine, int register, int length, int offset, boolean plus) {
        this.machine = machine;
        this.register = new RegisterAddressing(machine, register, 4);
        nr = register;
        this.length = length;
        this.offset = offset;
        this.plus = plus;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    public Operand copy() {
        return new CellarAddressing(machine, nr, length, offset, plus);
    }

    @Override
    public int getAdress() {
        return 0;
    }

    @Override
    public MyByte[] getContent() {
        MyByte[] val = NumberConversion.add(register.getContent(),
                NumberConversion.intToByte(offset, 4));
        if (!plus)
            register.setContent(val, length);
        int adr = NumberConversion.myBytetoIntWithoutSign(register.getContent());
        MyByte[] ret = new AbsAddress(machine, adr, length, 0).getContent();
        if (plus)
            register.setContent(val, length);
        return ret;
    }

    /**
     * Gets the content without adjusting the stack pointer offset.
     *
     * @return the content at the current address
     */
    public MyByte[] getContentWithoutOffset() {
        MyByte[] ret = new AbsAddress(machine,
                NumberConversion.myBytetoIntWithoutSign(register.getContent()), length,
                0).getContent();
        return ret;
    }

    @Override
    public byte[] encode() {
        MyByte[] ret = new MyByte[]{plus ? new MyByte(8 * 16 + nr) : new MyByte(7 * 16 + nr)};
        return MyByte.toByteArray(ret);
    }

    @Override
    public void setContent(MyByte[] content, int length) {
        if (!plus) {
            register.setContent(NumberConversion.add(register.getContent(),
                            NumberConversion.intToByte(offset, 4)),
                    length);
        }
        new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(register.getContent()), 4,
                0).setContent(content, length);
        if (plus) {
            register.setContent(NumberConversion.add(register.getContent(),
                            NumberConversion.intToByte(offset, 4)),
                    length);
        }
    }

    /**
     * Sets content without adjusting the stack pointer offset.
     *
     * @param content the content to write
     * @param length  the length in bytes
     */
    public void setContentWithoutOffset(MyByte[] content, int length) {
        new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(register.getContent()), 4,
                0).setContent(content, length);
    }

    @Override
    public String toString() {
        return plus ? ("!R" + Integer.toString(nr) + "+") : "-!R" + Integer.toString(nr);
    }
}
