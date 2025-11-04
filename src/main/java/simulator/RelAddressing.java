package simulator;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import enviroment.Register;
import core.Constants;

/**
 * Operandenspeziikation für Relative Adressierung
 */
public class RelAddressing implements Operand, AdressGetter {

    /**
     * The length.
     */
    private int length;

    /**
     * The offset.
     */
    private int offset;

    /**
     * The nr1.
     */
    private int nr1;

    /**
     * The nr2.
     */
    private int nr2;

    /**
     * The adress.
     */
    private int address;

    /**
     * Konstruktor für die Relative Adressierung
     *
     * @param offset  offset
     * @param reg     Registernummer
     * @param length  Länge des Registerzugriffs
     * @param address Adresse des Befehls
     */
    public RelAddressing(int offset, int reg, int length, int address) {
        this.address = address;
        this.offset = offset;
        this.length = length;
        nr1 = reg;
        nr2 = -1;
    }

    /**
     * Konstruktor für die Relative Adressierung
     *
     * @param offset  offset
     * @param reg     Registernummer
     * @param index   Indexregister
     * @param length  Länge des Registerzugriffs
     * @param address Adresse des Befehls the adress
     */
    public RelAddressing(int offset, int reg, int index, int length, int address) {
        this.address = address;
        this.offset = offset;
        this.length = length;
        nr1 = reg;
        nr2 = index;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#copy()
     */
    @Override
    public Operand copy() {
        // TODO Auto-generated method stub
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

            return index != null ?
                    address + offset + length * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)) :
                    address + offset;
        } else {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            Register register = Enviroment.REGISTERS.getRegister(nr1);
            return index != null ?
                    NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                            + offset + length * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)) :
                    NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                            + offset;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#getContent()
     */
    @Override
    public MyByte[] getContent() {
        if (nr1 == Constants.PC_REGISTER) {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            return index != null ?
                    new AbsAddress(address + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0).getContent() :
                    new AbsAddress(address + offset, length, 0).getContent();
        }
        Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
        Register register = Enviroment.REGISTERS.getRegister(nr1);
        return index != null ?
                new AbsAddress(
                        NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                                + offset + length * NumberConversion.myBytetoIntWithoutSign(
                                index.getContent(4)), length, 0).getContent() :
                new AbsAddress(
                        NumberConversion.myBytetoIntWithoutSign(register.getContent(4))
                                + offset, length, 0).getContent();

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
            return new MyByte[]{new MyByte(6 * 16 + nr1)};
        }
        if (offset == 0) {
            return new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(6 * 16 + nr1)};
        }

        if (offset >= Byte.MIN_VALUE && offset <= Byte.MAX_VALUE) {
            return index == null ?
                    new MyByte[]{new MyByte(10 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(10 * 16 + nr1),
                            NumberConversion.intToByte(offset, 1)[0]};
        }

        if (offset >= -16384 && offset <= 16383) {
            return index == null ?
                    new MyByte[]{new MyByte(12 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]} :
                    new MyByte[]{new MyByte(4 * 16 + nr2), new MyByte(12 * 16 + nr1),
                            NumberConversion.intToByte(offset, 2)[0],
                            NumberConversion.intToByte(offset, 2)[1]};
        }
        return index == null ?
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
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#setContent(compiler.MyByte[], int)
     */
    @Override
    public void setContent(MyByte[] content, int length) {
        if (nr1 == Constants.PC_REGISTER) {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            AbsAddress ort = index != null ?
                    new AbsAddress(address + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0) :
                    new AbsAddress(address + offset, 4, 0);
            ort.setContent(content, length);
        } else {
            Register index = nr2 != -1 ? Enviroment.REGISTERS.getRegister(nr2) : null;
            Register register = Enviroment.REGISTERS.getRegister(nr1);
            AbsAddress ort = index != null ?
                    new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(4)) + offset + length
                            * NumberConversion.myBytetoIntWithoutSign(
                            index.getContent(4)), length, 0) :
                    new AbsAddress(NumberConversion.myBytetoIntWithoutSign(
                            register.getContent(4)) + offset, length, 0);
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
