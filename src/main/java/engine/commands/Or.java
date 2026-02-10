package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * Or-Befehl
 *
 * @author Matthias Oehme
 */
public class Or extends Command {

    /** erster Operand */
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
     * Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     */
    int length = 0;

    /**
     * Konstruktor für einen 2-Adress Or-Befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param length
     *            Länge Befehls B = 1, H = 2, W = 4,
     * @param op1
     *            erster Operand
     * @param op2
     *            zweiter Operand
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Or(Machine machine, int line, int adress, int length, Operand op1, Operand op2, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.length = length;
        setAdress(adress);
    }

    /**
     * Konstruktor für einen 2-Adress Or-Befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param length
     *            Länge Befehls B = 1, H = 2, W = 4,
     * @param op1
     *            erster Operand
     * @param op2
     *            zweiter Operand
     * @param op3
     *            dritter Operand
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Or(Machine machine, int line, int adress, int length, Operand op1, Operand op2, Operand op3, int beg,
              int end) {
        this(machine, line, adress, length, op1, op2, beg, end);
        this.op3 = op3;
        three = true;
    }

    /**
     * Decodes an OR instruction from memory.
     * MI Manual page 91: OR B/H/W with 2 or 3 operands.
     * Opcodes: 0xAD-0xB2
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, opcode.length);
        Operand op2 = Operand.decode(machine, opcode.length);
        if (opcode.operandCount == 3) {
            Operand op3 = Operand.decode(machine, opcode.length);
            return new Or(machine, 0, pc, opcode.length, op1, op2, op3, 0, 0);
        }
        return new Or(machine, 0, pc, opcode.length, op1, op2, 0, 0);
    }
    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<>();
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
                opcode = three ? new MyByte("B0") : new MyByte("AD");
                break;
            case 2:
                opcode = three ? new MyByte("B1") : new MyByte("AE");
                break;
            case 4:
                opcode = three ? new MyByte("B2") : new MyByte("AF");
                break;
            default:
                throw new engine.InternalError("Invalid OR length: " + length);
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
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel())) || (
                (op2 instanceof AbsAddress) && (((AbsAddress) op2).hasLabel())) || (
                (op3 instanceof AbsAddress) && (((AbsAddress) op3).hasLabel()));
    }
    @Override
    public synchronized void run() {
        super.run();
        MyByte[] erg = new MyByte[length];
        MyByte[] ope1 = op1.getContent();
        MyByte[] ope2 = ((!three) && (op2 instanceof CellarAddressing)) ?
                ((CellarAddressing) op2).getContentWithoutOffset() :
                op2.getContent();
        for (int i = length - 1; i >= 0; i--) {
            erg[i] = new MyByte(ope1[i].getContent() | ope2[i].getContent());
        }
        if (!three) {
            op2.setContent(erg, length);
        } else {
            op3.setContent(erg, length);
        }
        
        int resultSigned = NumberConversion.myBytetoIntWithSign(erg);
        machine.getFlags().setZero(resultSigned == 0);
        machine.getFlags().setNegative(resultSigned < 0);
        machine.getFlags().setOverflow(false);

    }
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
                bhwfd = "W";
                break;
            default:
                bhwfd = "?";
                break;
        }
        return "OR " + bhwfd + " " + op1.toString() + ", " + op2.toString() + (three ?
                ", " + op3.toString() :
                "");
    }

}