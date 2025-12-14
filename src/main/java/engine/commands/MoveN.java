/**
 *
 */
package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * MoveN-Befehl
 *
 * @author Matthias Oehme
 */
public class MoveN extends Command {
    /** erster Operand */
    Operand op1;

    /**
     * zweiter Operand
     */
    Operand op2;

    /**
     * true, falls es sich um einen 3-Adressbefehl handelt
     */
    boolean three = false;

    /**
     * true, falls es ein Gleitpunktzahlbefehl ist
     */
    boolean floating = false;

    /**
     * Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     */
    int length = 0;

    /**
     * Konstruktor für einen MoveN-Befehl
     *
     * @param machine
     *            the machine this command operates on
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param length
     *            Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     * @param op1
     *            erster Operand
     * @param op2
     *            zweiter Operand
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     * @param floating
     *            true, falls es ein Gleitpunktzahlbefehl ist
     */
    public MoveN(Machine machine, int line, int adress, int length, Operand op1, Operand op2, int beg, int end,
                 boolean floating) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.length = length;
        setAdress(adress);
        this.floating = floating;
    }

    /**
     * Decodes a MOVEN instruction from memory.
     * MI Manual page 90: MOVEN B/H/W/F/D with 2 operands (negate).
     * Opcodes: 0xA3-0xA7
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, opcode.length);
        Operand op2 = Operand.decode(machine, opcode.length);
        return new MoveN(machine, 0, pc, opcode.length, op1, op2, 0, 0, opcode.floating);
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getLabel()
     */
    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<LabelInUse>();
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op1).getLabel(),
                    (AbsAddress) op1));
        }
        if (op2 instanceof AbsAddress && ((AbsAddress) op2).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op2).getLabel(),
                    (AbsAddress) op2));
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte opcode = null;
        switch (length) {
            case 1:
                opcode = new MyByte("A3");
                break;
            case 2:
                opcode = new MyByte("A4");
                break;
            case 4:
                opcode = new MyByte(floating ? "A6" : "A5");
                break;
            case 8:
                opcode = new MyByte("A7");
                break;
            default:
                throw new engine.InternalError("Invalid MOVEN length: " + length);
        }
        int x = 1;
        byte[] opc1 = op1.encode();
        byte[] opc2 = op2.encode();

        MyByte[] ret = new MyByte[opc1.length + opc2.length + 1];
        ret[0] = opcode;
        for (int i = 0; i < opc1.length; i++) {
            ret[x++] = new MyByte(opc1[i]);
        }
        for (int i = 0; i < opc2.length; i++) {
            ret[x++] = new MyByte(opc2[i]);
        }

        return MyByte.toByteArray(ret);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#hasLabel()
     */
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel())) || (
                (op2 instanceof AbsAddress) && (((AbsAddress) op2).hasLabel()));
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        MyByte[] ret = new MyByte[length];
        super.run();
        boolean overflow = false;
        boolean zero = false;
        boolean negative = false;

        if (floating) {
            switch (length) {
                case 4:
                    float valF = Float.intBitsToFloat(NumberConversion.myBytetoIntWithSign(
                            op1.getContent()));
                    float resF = valF * -1.0f;
                    ret = NumberConversion.intToByte(Float.floatToIntBits(resF), length);
                    zero = (resF == 0);
                    negative = (resF < 0);
                    break;
                case 8:
                    double valD = Double.longBitsToDouble(
                            NumberConversion.myBytetoLongWithoutSign(
                                    op1.getContent()));
                    double resD = valD * -1.0;
                    ret = NumberConversion.longToByte(Double.doubleToLongBits(resD), length);
                    zero = (resD == 0);
                    negative = (resD < 0);
                    break;
                default:
                    throw new IllegalStateException("Unsupported floating point length: " + length);
            }
        } else {
            long val = NumberConversion.myBytetoLongWithSign(op1.getContent());
            long res = -val;
            ret = NumberConversion.intToByte((int) res, length);
            
            // Overflow check for Integer: Only MIN_VALUE negation overflows
            // if val is MIN_VALUE (e.g. -128 for byte), -val is 128, which is not representable
            // NumberConversion.isValidSigned can check if 'res' fits in 'length'
            overflow = !NumberConversion.isValidSigned(res, length);
            
            zero = ((int)res == 0);
            negative = ((int)res < 0);
        }

        op2.setContent(ret, length);
        machine.getFlags().setCarry(false);
        machine.getFlags().setOverflow(overflow);
        machine.getFlags().setZero(zero);
        machine.getFlags().setNegative(negative);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#setAdress(int)
     */
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ((AbsAddress) op1).setOrt(adress + 1);
        }
        if (op2 instanceof AbsAddress && ((AbsAddress) op2).hasLabel()) {
            ((AbsAddress) op2).setOrt(adress + 1 + op1.encode().length);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String bhwfd = "";
        switch (length) {
            case 1:
                bhwfd = "B";
                break;
            case 2:
                bhwfd = "H";
                break;
            case 4:
                bhwfd = floating ? "F" : "W";
                break;
            case 8:
                bhwfd = "D";
                break;
            default:
                bhwfd = "?";
                break;
        }
        return "MOVEN " + bhwfd + " " + op1.toString() + ", " + op2.toString();
    }

}
