package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;
import static engine.MachineConstants.PC_REGISTER;

/**
 * Operandenspezifikation für Indirekte Adressierung
 *
 * @author Cyberdyne
 */
public class IndAddressing implements Operand, AdressGetter {

    /** The machine this operand operates on */
    private final Machine machine;

    /** The length. */
    private int length;

    /** The offset. */
    private int offset;

    /** The adress. */
    private int address;

    /** The nr1. */
    private int nr1;

    /** The nr2. */
    private int nr2;

    /**
     * Konstruktor für die Indirekteadressierung
     *
     * @param machine   the machine this operand operates on
     * @param register1 Regsiternummer
     * @param length    Länge des Registerzugriffs
     * @param address   Adresse des Befehls
     */
    public IndAddressing(Machine machine, int register1, int length, int address) {
        this.machine = machine;
        this.address = address;
        this.length = length;
        offset = 0;
        nr1 = register1;
        nr2 = -1;

    }

    /**
     * Konstruktor für die Indirekteadressierung
     *
     * @param machine   the machine this operand operates on
     * @param offset    offset
     * @param register1 Regsiternummer
     * @param length    Länge des Registerzugriffs
     * @param address   Adresse des Befehls
     */
    public IndAddressing(Machine machine, int offset, int register1, int length, int address) {
        this.machine = machine;
        this.address = address;
        this.length = length;
        this.offset = offset;
        nr1 = register1;
        nr2 = -1;

    }

    /**
     * Konstruktor für die Indirekteadressierung
     *
     * @param machine
     *            the machine this operand operates on
     * @param offset
     *            offset
     * @param register1
     *            Regsiternummer
     * @param register2
     *            Indexregister
     * @param length
     *            Länge des Registerzugriffs
     * @param address
     *            Adresse des Befehls
     */
    public IndAddressing(Machine machine, int offset, int register1, int register2, int length, int address) {
        this.machine = machine;
        this.address = address;
        this.length = length;
        this.offset = offset;
        nr1 = register1;
        nr2 = register2;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }
    @Override
    public Operand copy() {
        return null;
    }
    @Override
    public int getAdress() {
        if (nr1 == PC_REGISTER) {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            return NumberConversion.myBytetoIntWithSign(new AbsAddress(machine, index != null ?
                    address +
                            length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(
                                            4)) :
                    address
                            + offset,
                    4,
                    0).getContent());
        } else {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            Register register = machine.getRegisters().getRegister(nr1);
            return NumberConversion.myBytetoIntWithSign(new AbsAddress(machine, index != null ?
                    NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(
                                    4))
                            +
                            length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(
                                            4)) :
                    NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(
                                    4))
                            + offset,
                    4,
                    0).getContent());
        }

    }
    @Override
    public MyByte[] getContent() {
        if (nr1 == PC_REGISTER) {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;

            return new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                    index != null ?
                            new AbsAddress(machine, address + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0).getContent() :
                            (new AbsAddress(machine,

                                    address + offset, 4, 0)).getContent()), length,
                    0).getContent();
        }

        Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
        Register register = machine.getRegisters().getRegister(nr1);

        return new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(index != null ?
                new AbsAddress(machine,
                        NumberConversion.myBytetoIntWithoutSign(
                                register.getContent(
                                        4))
                                +
                                length
                                        * NumberConversion.myBytetoIntWithoutSign(
                                        index.getContent(
                                                4)),
                        4,
                        0).getContent() :
                (new AbsAddress(machine,

                        NumberConversion.myBytetoIntWithoutSign(
                                register.getContent(
                                        4))
                                + offset,
                        4,
                        0)).getContent()),
                length, 0).getContent();

    }

    @Override
    public byte[] encode() {
        Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
        MyByte[] ret;
        if (index == null && offset == 0) {
            ret = new MyByte[]{new MyByte(11 * 16 + nr1), new MyByte(0)};
            return MyByte.toByteArray(ret);
        }
        if (offset == 0) {
            ret = new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(11 * 16 + nr1),
                    new MyByte(0)};
            return MyByte.toByteArray(ret);
        }

        if (offset >= Byte.MIN_VALUE && offset <= Byte.MAX_VALUE) {
            ret = index == null ?
                    new MyByte[]{new MyByte(11 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(11 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]};
            return MyByte.toByteArray(ret);
        }

        if (offset >= -16384 && offset <= 16383) {
            ret = index == null ?
                    new MyByte[]{new MyByte(13 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(13 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]};
            return MyByte.toByteArray(ret);
        }
        ret = index == null ?
                new MyByte[]{new MyByte(15 * 16 + nr1),
                        NumberConversion.intToByte(offset, 4)[0],
                        NumberConversion.intToByte(offset, 4)[1],
                        NumberConversion.intToByte(offset, 4)[2],
                        NumberConversion.intToByte(offset, 4)[3]} :
                new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(15 * 16 + nr1),
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

            AbsAddress ort = new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                    (index != null ?
                            new AbsAddress(machine, address + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0) :
                            new AbsAddress(machine,

                                    address + offset, 4, 0)).getContent()), length, 0);
            ort.setContent(content, length);
        } else {
            Register index = nr2 != -1 ? machine.getRegisters().getRegister(nr2) : null;
            Register register = machine.getRegisters().getRegister(nr1);

            AbsAddress ort = new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                    (index != null ?
                            new AbsAddress(machine, NumberConversion.myBytetoIntWithoutSign(
                                    register.getContent(4)) + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0) :
                            new AbsAddress(machine,

                                    NumberConversion.myBytetoIntWithoutSign(
                                            register.getContent(4)) + offset, 4,
                                    0)).getContent()), length, 0);
            ort.setContent(content, length);
        }
    }
    @Override
    public String toString() {
        return (offset != 0 ?
                "!(" + Integer.toString(offset) + " + " + "!R" + Integer.toString(nr1)
                        + ")" :
                "!!R" + Integer.toString(nr1)) + (nr2 != -1 ?
                "/R" + Integer.toString(nr2) + "/" :
                "");
    }

}
