/**
 *
 */
package simulator;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import enviroment.Register;
import core.Constants;

/**
 * Operandenspezifikation für Indirekte Adressierung
 *
 * @author Cyberdyne
 */
public class IndAddressing implements Operand, AdressGetter {

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
     * @param register1 Regsiternummer
     * @param length    Länge des Registerzugriffs
     * @param address   Adresse des Befehls
     */
    public IndAddressing(int register1, int length, int address) {
        this.address = address;
        this.length = length;
        offset = 0;
        nr1 = register1;
        nr2 = -1;

    }

    /**
     * Konstruktor für die Indirekteadressierung
     *
     * @param offset    offset
     * @param register1 Regsiternummer
     * @param length    Länge des Registerzugriffs
     * @param address   Adresse des Befehls
     */
    public IndAddressing(int offset, int register1, int length, int address) {
        this.address = address;
        this.length = length;
        this.offset = offset;
        nr1 = register1;
        nr2 = -1;

    }

    /**
     * Konstruktor für die Indirekteadressierung
     *
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
    public IndAddressing(int offset, int register1, int register2, int length, int address) {
        this.address = address;
        this.length = length;
        this.offset = offset;
        nr1 = register1;
        nr2 = register2;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#copy()
     */
    @Override
    public Operand copy() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see interpreter.AdressGetter#getAdress()
     */
    @Override
    public int getAdress() {
        if (nr1 == Constants.PC_REGISTER) {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            return NumberConversion.myBytetoIntWithSign(new AbsAddress(index != null ?
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
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            Register register = Enviroment.REGISTERS.getRegister(nr1);
            return NumberConversion.myBytetoIntWithSign(new AbsAddress(index != null ?
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

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#getContent()
     */
    @Override
    public MyByte[] getContent() {
        if (nr1 == Constants.PC_REGISTER) {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;

            return new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                    index != null ?
                            new AbsAddress(address + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0).getContent() :
                            (new AbsAddress(

                                    address + offset, 4, 0)).getContent()), length,
                    0).getContent();
        }

        Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
        Register register = Enviroment.REGISTERS.getRegister(nr1);

        return new AbsAddress(NumberConversion.myBytetoIntWithoutSign(index != null ?
                new AbsAddress(
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
                (new AbsAddress(

                        NumberConversion.myBytetoIntWithoutSign(
                                register.getContent(
                                        4))
                                + offset,
                        4,
                        0)).getContent()),
                length, 0).getContent();

    }

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
        if (index == null && offset == 0) {
            return new MyByte[]{new MyByte(11 * 16 + nr1), new MyByte(0)};
        }
        if (offset == 0) {
            return new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(11 * 16 + nr1),
                    new MyByte(0)};
        }

        if (offset >= Byte.MIN_VALUE && offset <= Byte.MAX_VALUE) {
            return index == null ?
                    new MyByte[]{new MyByte(11 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(11 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]};
        }

        if (offset >= -16384 && offset <= 16383) {
            return index == null ?
                    new MyByte[]{new MyByte(13 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(13 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]};
        }
        return index == null ?
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
    }

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#setContent(interpreter.MyByte[], int)
     */
    @Override
    public void setContent(MyByte[] content, int length) {
        if (nr1 == Constants.PC_REGISTER) {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;

            AbsAddress ort = new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                    (index != null ?
                            new AbsAddress(address + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0) :
                            new AbsAddress(

                                    address + offset, 4, 0)).getContent()), length, 0);
            ort.setContent(content, length);
        } else {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            Register register = Enviroment.REGISTERS.getRegister(nr1);

            AbsAddress ort = new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                    (index != null ?
                            new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                                    register.getContent(4)) + length
                                    * NumberConversion.myBytetoIntWithoutSign(
                                    index.getContent(4)), 4, 0) :
                            new AbsAddress(

                                    NumberConversion.myBytetoIntWithoutSign(
                                            register.getContent(4)) + offset, 4,
                                    0)).getContent()), length, 0);
            ort.setContent(content, length);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
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
