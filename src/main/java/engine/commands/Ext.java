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
 * EXT-Befehl
 *
 * @author Matthias Oehme
 *
 */
public class Ext extends Command {

    /** erster Operand */
    Operand op1;

    /** zweiter Operand */
    Operand op2;

    /** dritter Operand */
    Operand op3;

    /** dritter Operand */
    Operand op4;

    /**
     * Konstruktor für einen 4EXT-Befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param op1
     *            erster Operand
     * @param op2
     *            zweiter Operand
     * @param op3
     *            dritter Operand
     * @param op4
     *            vierter Operand
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Ext(Machine machine, int line, int adress, Operand op1, Operand op2, Operand op3, Operand op4,
               int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.op4 = op4;
        setAdress(adress);
    }

    /**
     * Decodes an EXT instruction from memory.
     * MI Manual page 93: EXT with 4 operands (extract bitfield, zero extend).
     * Opcode: 0xF7
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        Operand op3 = Operand.decode(machine, 4);
        Operand op4 = Operand.decode(machine, 4);
        return new Ext(machine, 0, pc, op1, op2, op3, op4, 0, 0);
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

        if (op4 instanceof AbsAddress && ((AbsAddress) op4).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op4).getLabel(),
                    (AbsAddress) op4));
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
        MyByte opcode = new MyByte("F7");

        int x = 1;
        byte[] opc1 = op1.encode();
        byte[] opc2 = op2.encode();
        byte[] opc3 = op3.encode();
        byte[] opc4 = op4.encode();
        MyByte[] ret = new MyByte[opc1.length + opc2.length + opc3.length + opc4.length
                + 1];
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

        for (byte element : opc4) {
            ret[x] = new MyByte(element);
            x++;
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
                (op3 instanceof AbsAddress) && (((AbsAddress) op3).hasLabel())) || (
                (op4 instanceof AbsAddress) && (((AbsAddress) op4).hasLabel()));
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        super.run();
        // MI Spec (page 30): Extract bitfield
        // EXT a1, a2, a3, a4: P := S[a1], S := S[a2], A := a3, S[a4] := extracted bitfield
        // Operand order: op1=P, op2=S, op3=A (source address), op4=dest

        int p = NumberConversion.myBytetoIntWithSign(op1.getContent());
        int s = NumberConversion.myBytetoIntWithSign(op2.getContent());
        int value = NumberConversion.myBytetoIntWithSign(op3.getContent());

        // MSB-0 bit numbering: bit 0 is the most significant bit
        // Extract S bits starting at bit position P (from MSB)
        // The bitfield spans from bit P to bit P+S-1 (MSB-0 numbering)
        // Convert to LSB-0 for extraction: LSB position = 31 - (P + S - 1) = 32 - P - S
        int lsbStart = 32 - p - s;
        int mask = (s >= 32) ? -1 : ((1 << s) - 1);
        int result = (lsbStart >= 0) ? (value >>> lsbStart) & mask : 0;

        MyByte[] ret = NumberConversion.intToByte(result, 4);
        op4.setContent(ret, 4);

        machine.getFlags().setCarry(false);
        machine.getFlags().setOverflow(false);
        machine.getFlags().setZero(result == 0);
        machine.getFlags().setNegative(result < 0);
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
        if (op4 instanceof AbsAddress && ((AbsAddress) op4).hasLabel()) {
            ((AbsAddress) op4).setOrt(
                    adress + 1 + op1.encode().length + op2.encode().length
                            + op3.encode().length);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "EXT " + op1.toString() + ", " + op2.toString() + ", " + op3.toString()
                + ", " + op4.toString();
    }
}
