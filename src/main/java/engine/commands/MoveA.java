package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * MoveA-Befehl
 *
 * @author Matthias Oehme
 */
public class MoveA extends Command {
    /** erster Operand */
    Operand op1;

    /** zweiter Operand */
    Operand op2;

    /**
     * Konstruktor für einen MoveA-Befehl
     *
     * @param machine
     *            the machine this command operates on
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param op1
     *            erster Operand
     * @param op2
     *            zweiter Operand
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public MoveA(Machine machine, int line, int adress, Operand op1, Operand op2, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        setAdress(adress);
    }

    /**
     * Decodes a MOVEA instruction from memory.
     * MI Manual page 90: MOVEA with 2 operands (address move).
     * Opcode: 0xAB
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        return new MoveA(machine, 0, pc, op1, op2, 0, 0);
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

        return ret;
    }
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("AB");
        int x = 1;
        byte[] opc1 = op1.encode();
        byte[] opc2 = op2.encode();

        MyByte[] ret = new MyByte[opc1.length + opc2.length + 1];
        ret[0] = opcode;
        for (byte element : opc1) {
            ret[x] = new MyByte(element);
            x++;
        }
        for (byte element : opc2) {
            ret[x] = new MyByte(element);
            x++;
        }

        return MyByte.toByteArray(ret);

    }
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel())) || (
                (op2 instanceof AbsAddress) && (((AbsAddress) op2).hasLabel()));
    }
    @Override
    public synchronized void run() {
        super.run();
        op2.setContent(NumberConversion.intToByte(
                (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) ?
                        ((AbsAddress) op1).getAdress() :
                        ((AdressGetter) op1).getAdress(), 4), 4);
        machine.getFlags().setOverflow(false);

    }
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ((AbsAddress) op1).setOrt(adress + 1);
        }
        if (op2 instanceof AbsAddress && ((AbsAddress) op2).hasLabel()) {
            ((AbsAddress) op2).setOrt(
                    ((AbsAddress) op1).getOrt() + op1.encode().length);
        }

    }
    @Override
    public String toString() {

        return "MOVEA " + op1.toString() + ", " + op2.toString();
    }
}
