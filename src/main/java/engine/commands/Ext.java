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
        int p = NumberConversion.myBytetoIntWithSign(op1.getContent());
        int s = NumberConversion.myBytetoIntWithSign(op2.getContent());
        int a = op3.getAdress();

        // Per MI spec (page 30): Extract bitfield of size S bits starting at bit position P
        // from base address A. Bit numbering uses LSB-0 convention (bit 0 is least significant).
        // Memory is big-endian, so for a word at address A:
        //   - addr A:   bits 31-24 (MSB)
        //   - addr A+1: bits 23-16
        //   - addr A+2: bits 15-8
        //   - addr A+3: bits 7-0 (LSB)

        // We need to read enough bytes to cover bits P through P+S-1
        // Calculate which bytes contain these bits
        int highestBit = p + s - 1;
        int lowestBit = p;

        // In big-endian with LSB-0, bit 0 is at the rightmost position
        // To read a range of bits, we need to figure out which bytes contain them
        // Assuming the bitfield can span up to 64 bits (8 bytes)

        // Read 8 bytes starting from address A (enough for any valid bitfield)
        // This gives us bits 63-0 when interpreted as a 64-bit big-endian value
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int b = machine.getMemory().getContent(a + i, 1)[0].getContent() & 0xFF;
            value = (value << 8) | b;
        }

        // Now 'value' contains 64 bits with bit 63 being the MSB at address A
        // and bit 0 being the LSB at address A+7
        // But MI's bit numbering has bit 0 at address A (the MSB of our 64-bit value)
        // So we need to reverse our thinking...

        // Actually, looking at the spec diagram more carefully:
        // MI numbers bits starting from 0 at position P from address A
        // The diagram shows: 0, P-1, P, P+S-1 with b₀ at position P
        // This suggests bit position increases with memory address

        // For MI with big-endian and this bit numbering:
        // Bit P is at byte address A + P/8, within-byte position (7 - P%8) from MSB
        // Let's use a different approach: shift and mask based on P and S

        // Extract bits P through P+S-1
        // In our 64-bit value, bit 0 corresponds to the MSB
        // We need to shift right to position the desired bits at the low end
        int shiftAmount = 64 - p - s;
        if (shiftAmount >= 0) {
            value >>>= shiftAmount;
        } else {
            value <<= -shiftAmount;
        }

        // Mask to s bits (zero extension for EXT)
        long mask = (s >= 64) ? -1L : ((1L << s) - 1);
        long result = value & mask;

        MyByte[] ret = NumberConversion.longToByte(result, 4);
        op4.setContent(ret, 4);

        int val = (int) result;
        machine.getFlags().setCarry(false);
        machine.getFlags().setOverflow(false);
        machine.getFlags().setZero(val == 0);
        machine.getFlags().setNegative(val < 0);
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
