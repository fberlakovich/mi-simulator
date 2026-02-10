package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import java.util.ArrayList;

/**
 * INS-Befehl
 *
 * @author Matthias Oehme
 *
 */
public class Ins extends Command {

    /** erster Operand */
    Operand op1;

    /** zweiter Operand */
    Operand op2;

    /** dritter Operand */
    Operand op3;

    /** vierter Operand */
    Operand op4;

    /**
     * Konstruktor für einen INS-Befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param op1
     *            P (bit position)
     * @param op2
     *            S (bit size)
     * @param op3
     *            A (source)
     * @param op4
     *            dest (destination, modified in place)
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Ins(Machine machine, int line, int adress, Operand op1, Operand op2, Operand op3, Operand op4,
               int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.op4 = op4;
        setAdress(adress);
    }

    /**
     * Decodes an INS instruction from memory.
     * MI Manual page 93: INS with 4 operands (insert bitfield).
     * Opcode: 0xF8
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        Operand op3 = Operand.decode(machine, 4);
        Operand op4 = Operand.decode(machine, 4);
        return new Ins(machine, 0, pc, op1, op2, op3, op4, 0, 0);
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

        if (op4 instanceof AbsAddress && ((AbsAddress) op4).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op4).getLabel(),
                    (AbsAddress) op4));
        }
        return ret;
    }
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("F8");

        // For 5-operand form, only encode the first 4 operands (op5 is result register, not encoded)
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
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel())) || (
                (op2 instanceof AbsAddress) && (((AbsAddress) op2).hasLabel())) || (
                (op3 instanceof AbsAddress) && (((AbsAddress) op3).hasLabel())) || (
                (op4 instanceof AbsAddress) && (((AbsAddress) op4).hasLabel()));
    }
    @Override
    public synchronized void run() {
        super.run();

        // MI Spec (page 30): Insert bitfield
        // Machine instruction: INS P, S, A, dest
        // Bit numbering: LSB-0 (bit 0 is least significant)

        int p = NumberConversion.myBytetoIntWithSign(op1.getContent());
        int s = NumberConversion.myBytetoIntWithSign(op2.getContent());
        int sourceValue = NumberConversion.myBytetoIntWithSign(op3.getContent());
        int destValue = NumberConversion.myBytetoIntWithSign(op4.getContent());

        // Create mask for S bits at position P (LSB-0 numbering)
        long mask = (s >= 64) ? -1L : ((1L << s) - 1);

        // Clear the target bitfield in destination
        long clearedDest = (destValue & 0xFFFFFFFFL) & ~(mask << p);

        // Insert source bits (low S bits) at position P
        long sourceBits = (sourceValue & 0xFFFFFFFFL) & mask;
        long result = clearedDest | (sourceBits << p);

        // Write result to destination operand (modifies in place)
        MyByte[] ret = NumberConversion.longToByte(result, 4);
        op4.setContent(ret, 4);

        machine.getFlags().setCarry(false);
        machine.getFlags().setOverflow(false);
        machine.getFlags().setZero((int)result == 0);
        machine.getFlags().setNegative((int)result < 0);
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
        if (op4 instanceof AbsAddress && ((AbsAddress) op4).hasLabel()) {
            ((AbsAddress) op4).setOrt(
                    adress + 1 + op1.encode().length + op2.encode().length
                            + op3.encode().length);
        }
    }
    @Override
    public String toString() {
        return "INS " + op1.toString() + ", " + op2.toString() + ", " + op3.toString()
                + ", " + op4.toString();
    }
}
