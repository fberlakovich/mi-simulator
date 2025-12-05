package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * Repräsentation eines Div-Befehls
 *
 * @author Matthias Oehme
 */
public class Div extends Command {

    /**
     * erster Operand
     */
    Operand op1;

    /**
     * zweiter Operand
     */
    Operand op2;

    /**
     * dritter Operand
     */
    Operand op3;

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
     * Konstruktor für einen 2-Adress DIV-Befehl
     *
     * @param line     Zeile im Quelltext
     * @param adress   Adresse des Befehls
     * @param length   Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     * @param op1      erster Operand
     * @param op2      zweiter Operand
     * @param beg      Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end      Zeichenposition - Ende des Befehlswortes im Quelltext
     * @param floating true, falls es ein Gleitpunktzahlbefehl ist
     */
    public Div(Machine machine, int line, int adress, int length, Operand op1, Operand op2, int beg, int end,
               boolean floating) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.length = length;
        setAdress(adress);
        this.floating = floating;
    }

    /**
     * Konstruktor für einen 3-Adress DIV-Befehl
     *
     * @param line     Zeile im Quelltext
     * @param adress   Adresse des Befehls
     * @param length   Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     * @param op1      erster Operand
     * @param op2      zweiter Operand
     * @param op3      dritter Operand
     * @param beg      Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end      Zeichenposition - Ende des Befehlswortes im Quelltext
     * @param floating true, falls es ein Gleitpunktzahlbefehl ist
     */

    public Div(Machine machine, int line, int adress, int length, Operand op1, Operand op2, Operand op3, int beg,
               int end, boolean floating) {
        this(machine, line, adress, length, op1, op2, beg, end, floating);
        this.op3 = op3;
        three = true;
    }

    /**
     * Decodes a DIV instruction from memory.
     * MI Manual page 92: DIV B/H/W/F/D with 2 or 3 operands.
     * Opcodes: 0xDD-0xE6
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, opcode.length);
        Operand op2 = Operand.decode(machine, opcode.length);
        if (opcode.operandCount == 3) {
            Operand op3 = Operand.decode(machine, opcode.length);
            return new Div(machine, 0, pc, opcode.length, op1, op2, op3, 0, 0, opcode.floating);
        }
        return new Div(machine, 0, pc, opcode.length, op1, op2, 0, 0, opcode.floating);
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

        if (op3 instanceof AbsAddress && ((AbsAddress) op3).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op3).getLabel(),
                    (AbsAddress) op3));
        }
        return ret;
    }

    @Override
    public byte[] encode() {
        MyByte opcode = null;
        switch (length) {
            case 1:
                opcode = three ? new MyByte("E2") : new MyByte("DD");
                break;
            case 2:
                opcode = three ? new MyByte("E3") : new MyByte("DE");
                break;
            case 4:
                opcode = three ?
                        (floating ? new MyByte("E5") : new MyByte("E4")) :
                        (floating ? new MyByte("E0") : new MyByte("DF"));
                break;
            case 8:
                opcode = three ? new MyByte("E6") : new MyByte("E1");
                break;
            default:
                System.out.println("Fehler beim berechnen des OPCodes");
        }
        int x = 1;
        byte[] opc1 = op1.encode();
        byte[] opc2 = op2.encode();
        byte[] opc3 = three ? op3.encode() : null;
        MyByte[] ret = new MyByte[three ?
                opc1.length + opc2.length + opc3.length + 1 :
                opc1.length + opc2.length + 1];
        ret[0] = opcode;
        for (byte element : opc1) {
            ret[x] = new MyByte(element);
            x++;
        }
        for (byte element : opc2) {
            ret[x] = new MyByte(element);
            x++;
        }
        if (three) {
            for (byte element : opc3) {
                ret[x] = new MyByte(element);
                x++;
            }
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
                (op2 instanceof AbsAddress) && (((AbsAddress) op2).hasLabel())) || (
                (op3 instanceof AbsAddress) && (((AbsAddress) op3).hasLabel()));
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        super.run();
        MyByte[] erg = new MyByte[length];
        if (floating) {
            switch (length) {
                case 4:

                    Float ope1 = Float.intBitsToFloat(
                            NumberConversion.myBytetoIntWithoutSign(op1.getContent()));
                    Float ope2 = Float.intBitsToFloat(
                            NumberConversion.myBytetoIntWithoutSign(((three == false)
                                    && (op2 instanceof CellarAddressing)) ?
                                    ((CellarAddressing) op2).getContentWithoutOffset() :
                                    op2.getContent()));
                    Float erg_1 = ope2 / ope1;
                    erg = NumberConversion.intToByte(Float.floatToIntBits(erg_1),
                            length);

                    machine.getFlags().setZero(erg_1 == 0);

                    machine.getFlags().setNegative(erg_1 < 0);

                    machine.getFlags().setOverflow(Float.isInfinite(erg_1)
                            || erg_1 == 0 && ope1 != 0
                            && ope2 != 0);

                    break;
                case 8:

                    Double ope11 = Double.longBitsToDouble(
                            NumberConversion.myBytetoLongWithoutSign(op1.getContent()));
                    Double ope22 = Double.longBitsToDouble(
                            NumberConversion.myBytetoLongWithoutSign(((three == false)
                                    && (op2 instanceof CellarAddressing)) ?
                                    ((CellarAddressing) op2).getContentWithoutOffset() :
                                    op2.getContent()));
                    Double erg_11 = ope22 / ope11;
                    erg = NumberConversion.longToByte(Double.doubleToLongBits(erg_11),
                            length);

                    machine.getFlags().setZero(erg_11 == 0);

                    machine.getFlags().setNegative(erg_11 < 0);

                    machine.getFlags().setOverflow(Double.isInfinite(erg_11)
                            || erg_11 == 0 && ope11 != 0
                            && ope22 != 0);

                    break;
            }

        } else {
            MyByte[] ope1 = op1.getContent();
            MyByte[] ope2 = ((three == false) && (op2 instanceof CellarAddressing)) ?
                    ((CellarAddressing) op2).getContentWithoutOffset() :
                    op2.getContent();

            long erg1 = NumberConversion.myBytetoLongWithSign(ope2)
                    / NumberConversion.myBytetoLongWithSign(ope1);

            erg = NumberConversion.intToByte((int) erg1, length);

            int resultSigned = NumberConversion.myBytetoIntWithSign(erg);
            machine.getFlags().setZero(resultSigned == 0);
            machine.getFlags().setNegative(resultSigned < 0);

            machine.getFlags().setOverflow(!NumberConversion.isValidSigned(erg1, length));

        }

        machine.getFlags().setCarry(false);

        if (!three) {
            op2.setContent(erg, length);
        } else {
            op3.setContent(erg, length);
        }

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

        if (op3 instanceof AbsAddress && ((AbsAddress) op3).hasLabel()) {
            ((AbsAddress) op3).setOrt(
                    adress + 1 + op1.encode().length + op2.encode().length);
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
        }
        return "DIV " + bhwfd + " " + op1.toString() + ", " + op2.toString() + (three ?
                ", " + op3.toString() :
                "");
    }

}
