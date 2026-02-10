package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * ROT-Befehl
 *
 * @author Matthias Oehme
 */
public class Rot extends Command {

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
     * Konstruktor für einen Rot-Befehl
     *
     * @param line   Zeile im Quelltext
     * @param adress Adresse des Befehls
     * @param op1    erster Operand
     * @param op2    zweiter Operand
     * @param op3    dritter Operand
     * @param beg    Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Rot(Machine machine, int line, int adress, Operand op1, Operand op2, Operand op3, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        setAdress(adress);
    }

    /**
     * Decodes a ROT instruction from memory.
     * MI Manual page 92: ROT with 3 operands, Word type.
     * Opcode: 0xE8
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        Operand op3 = Operand.decode(machine, 4);
        return new Rot(machine, 0, pc, op1, op2, op3, 0, 0);
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
        MyByte opcode = new MyByte("E8");

        int x = 1;
        byte[] opc1 = op1.encode();
        byte[] opc2 = op2.encode();
        byte[] opc3 = op3.encode();
        MyByte[] ret = new MyByte[opc1.length + opc2.length + opc3.length + 1];
        ret[0] = opcode;
        for (byte element : opc1) {
            ret[x] = new MyByte(element);
            x++;
        }
        for (byte element : opc2) {
            ret[x] = new MyByte(element);
            x++;
        }

        for (byte element : opc3) {
            ret[x] = new MyByte(element);
            x++;
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
        MyByte[] ope2 = op2.getContent();
        int ope1 = NumberConversion.myBytetoIntWithSign(op1.getContent());
        int res;
        if (ope1 < 0) {
             res = Integer.rotateRight(
                    NumberConversion.myBytetoIntWithSign(ope2), -ope1);
        } else {
             res = Integer.rotateLeft(
                    NumberConversion.myBytetoIntWithSign(ope2), ope1);
        }
        
        MyByte[] erg = NumberConversion.intToByte(res, 4);

        op3.setContent(erg, 4);

        machine.getFlags().setOverflow(false);
        machine.getFlags().setZero(res == 0);
        machine.getFlags().setNegative(res < 0);

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

        return "ROT " + op1.toString() + ", " + op2.toString() + ", " + op3.toString();
    }

}
