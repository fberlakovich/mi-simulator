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
 * FindS-Befehl
 *
 * @author Matthias Oehme
 *
 */
public class Finds extends Command {

    /** erster Operand */
    Operand op1;

    /** zweiter Operand */
    Operand op2;

    /** dritter Operand */
    Operand op3;

    /** dritter Operand */
    Operand op4;

    /**
     * Konstruktor für einen Findc-Befehl
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
    public Finds(Machine machine, int line, int adress, Operand op1, Operand op2, Operand op3, Operand op4,
                 int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.op4 = op4;
        setAdress(adress);
    }

    /**
     * Decodes a FINDS instruction from memory.
     * MI Manual page 93: FINDS with 4 operands (find first set bit).
     * Opcode: 0xF9
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        Operand op3 = Operand.decode(machine, 4);
        Operand op4 = Operand.decode(machine, 4);
        return new Finds(machine, 0, pc, op1, op2, op3, op4, 0, 0);
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
        MyByte opcode = new MyByte("F9");

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
        int p = NumberConversion.myBytetoIntWithSign(op1.getContent());
        int s = NumberConversion.myBytetoIntWithSign(op2.getContent());
        int a = op3.getAdress();

        // Per MI spec (page 31): Find first set bit in bitfield
        // MI uses MSB-0 bit numbering: bit 0 is the most significant bit
        // Returns position of first '1' bit (P <= position < P+S), or P+S if not found

        // Read 8 bytes starting from address A
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int b = machine.getMemory().getContent(a + i, 1)[0].getContent() & 0xFF;
            value = (value << 8) | b;
        }

        // Search for first set bit in the range [P, P+S)
        // In our 64-bit value, bit 0 corresponds to bit 63 of 'value' (MSB)
        int result = p + s;  // Default: not found
        for (int bitPos = p; bitPos < p + s; bitPos++) {
            // Check if bit at position bitPos is set
            // bitPos 0 = bit 63 of value, bitPos 63 = bit 0 of value
            int shiftAmount = 63 - bitPos;
            if (shiftAmount >= 0 && shiftAmount < 64) {
                if ((value & (1L << shiftAmount)) != 0) {
                    result = bitPos;
                    break;
                }
            }
        }

        boolean bitNotFound = (result == p + s);

        MyByte[] ret = NumberConversion.intToByte(result, 4);
        op4.setContent(ret, 4);

        // Per MI spec (page 35): FINDS has C=0, V=0, Z=BNF (bit not found), N=0
        machine.getFlags().setCarry(false);
        machine.getFlags().setOverflow(false);
        machine.getFlags().setZero(bitNotFound);
        machine.getFlags().setNegative(false);
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

        return "FINDS " + op1.toString() + ", " + op2.toString() + ", " + op3.toString()
                + ", " + op4.toString();
    }
}
