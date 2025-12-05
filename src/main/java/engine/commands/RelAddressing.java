package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;
import static engine.MachineConstants.PC_REGISTER;

/**
 * Operand specification for relative addressing mode.
 * Supports base register with optional offset and index register.
 */
public class RelAddressing implements Operand, AdressGetter {

    /** The machine this operand operates on */
    private final Machine machine;

    /** Access length in bytes */
    private int length;

    /** Offset from base address */
    private int offset;

    /** Base register number */
    private int nr1;

    /** Index register number (-1 if not used) */
    private int nr2;

    /** Address of the instruction (for PC-relative addressing) */
    private int address;

    /**
     * Creates a relative addressing operand without index register.
     *
     * @param machine the machine this operand operates on
     * @param offset  offset from base address
     * @param reg     base register number
     * @param length  access length in bytes
     * @param address instruction address (for PC-relative)
     */
    public RelAddressing(Machine machine, int offset, int reg, int length, int address) {
        this.machine = machine;
        this.address = address;
        this.offset = offset;
        this.length = length;
        nr1 = reg;
        nr2 = -1;
    }

    /**
     * Creates a relative addressing operand with index register.
     *
     * @param machine the machine this operand operates on
     * @param offset  offset from base address
     * @param reg     base register number
     * @param index   index register number
     * @param length  access length in bytes
     * @param address instruction address (for PC-relative)
     */
    public RelAddressing(Machine machine, int offset, int reg, int index, int length, int address) {
        this.machine = machine;
        this.address = address;
        this.offset = offset;
        this.length = length;
        nr1 = reg;
        nr2 = index;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    public Operand copy() {
        if (nr2 == -1) {
            return new RelAddressing(machine, offset, nr1, length, address);
        }
        return new RelAddressing(machine, offset, nr1, nr2, length, address);
    }

    @Override
    public int getAdress() {
        if (nr1 == PC_REGISTER) {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;

            return index != null ?
                    address + offset + length * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)) :
                    address + offset;
        } else {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            Register register = machine.getRegisters().getRegister(nr1);
            return index != null ?
                    NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                            + offset + length * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)) :
                    NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                            + offset;
        }
    }

    @Override
    public MyByte[] getContent() {
        if (nr1 == PC_REGISTER) {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            return index != null ?
                    new AbsAddress(machine, address + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0).getContent() :
                    new AbsAddress(machine, address + offset, length, 0).getContent();
        }
        Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
        Register register = machine.getRegisters().getRegister(nr1);
        return index != null ?
                new AbsAddress(machine,
                        NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                                + offset + length * NumberConversion.myBytetoIntWithoutSign(
                                index.getContent(4)), length, 0).getContent() :
                new AbsAddress(machine,
                        NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                                + offset, length, 0).getContent();
    }

    @Override
    public byte[] encode() {
        Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
        MyByte[] ret;
        if (index == null && offset == 0) {
            ret = new MyByte[]{new MyByte(6 * 16 + nr1)};
            return MyByte.toByteArray(ret);
        }
        if (offset == 0) {
            ret = new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(6 * 16 + nr1)};
            return MyByte.toByteArray(ret);
        }

        if (offset >= Byte.MIN_VALUE && offset <= Byte.MAX_VALUE) {
            ret = index == null ?
                    new MyByte[]{new MyByte(10 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(10 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]};
            return MyByte.toByteArray(ret);
        }

        if (offset >= -16384 && offset <= 16383) {
            ret = index == null ?
                    new MyByte[]{new MyByte(12 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(12 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]};
            return MyByte.toByteArray(ret);
        }
        ret = index == null ?
                new MyByte[]{new MyByte(14 * 16 + nr1),
                        NumberConversion.intToByte(offset, 4)[0],
                        NumberConversion.intToByte(offset, 4)[1],
                        NumberConversion.intToByte(offset, 4)[2],
                        NumberConversion.intToByte(offset, 4)[3]} :
                new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(14 * 16 + nr1),
                        NumberConversion.intToByte(offset, 4)[0],
                        NumberConversion.intToByte(offset, 4)[1],
                        NumberConversion.intToByte(offset, 4)[2],
                        NumberConversion.intToByte(offset, 4)[3]};
        return MyByte.toByteArray(ret);
    }

    @Override
    public void setContent(MyByte[] content, int length) {
        if (nr1 == PC_REGISTER) {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            AbsAddress ort = index != null ?
                    new AbsAddress(machine, address + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0) :
                    new AbsAddress(machine, address + offset, 4, 0);
            ort.setContent(content, length);
        } else {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            Register register = machine.getRegisters().getRegister(nr1);
            AbsAddress ort = index != null ?
                    new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(4)) + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0) :
                    new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(4)) + offset, length, 0);
            ort.setContent(content, length);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(offset != 0 ? offset + " + " : " ");
        builder.append("!R");
        builder.append(nr1);
        if (nr2 != -1) {
            builder.append("/R").append(nr2).append("/");
        }
        return builder.toString();
    }
}
