package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;

import java.util.ArrayList;

/**
 * Jbcci-Befehl
 *
 * @author Matthias Oehme
 *
 */
public class Jbcci extends Command {

    /** erster Operand */
    Operand op1;

    /** zweiter Operand */
    Operand op2;

    /** dritter Operand */
    Operand op3;

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
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Jbcci(Machine machine, int line, int adress, Operand op1, Operand op2, Operand op3, int beg,
                 int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        setAdress(adress);
    }

    /**
     * Decodes a JBCCI instruction from memory.
     * MI Manual page 93: JBCCI with 3 operands (jump if bit clear, clear and invert).
     * Opcode: 0xFC
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand op1 = Operand.decode(machine, 4);
        Operand op2 = Operand.decode(machine, 4);
        Operand op3 = Operand.decode(machine, 4);
        return new Jbcci(machine, 0, pc, op1, op2, op3, 0, 0);
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
        MyByte opcode = new MyByte("FC");

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
        // JBCCI bitPos, register, label
        // LSB-0 bit numbering throughout
        // Semantics (inverse of JBSSI): JBCCI N tests bit N, if clear then jumps
        // Always clears the 2-bit pair containing N: bits (N & ~1) and (N | 1)
        // This means: N=0,1 both clear bits 0,1; N=2,3 both clear bits 2,3; etc.

        // Get bit position - for AbsAddress, the address itself IS the immediate value
        int bitPos;
        if (op1 instanceof AbsAddress) {
            bitPos = ((AbsAddress) op1).getAdress();
        } else {
            bitPos = NumberConversion.myBytetoIntWithSign(op1.getContent());
        }

        // Get register value
        MyByte[] content = op2.getContent();
        int value = NumberConversion.myBytetoIntWithSign(content);

        // Test if bit at bitPos (LSB-0) is clear
        boolean bitIsClear = ((value >>> bitPos) & 1) == 0;

        // Determine bits to clear
        int evenBit = bitPos & ~1;  // Even bit in pair
        int oddBit = bitPos | 1;     // Odd bit in pair

        // Always clear the even bit
        boolean evenBitWasAlreadyClear = ((value >>> evenBit) & 1) == 0;
        value &= ~(1 << evenBit);

        // Clear the odd bit if: (1) bitPos is even, OR (2) even bit was already clear
        boolean bitPosIsEven = (bitPos & 1) == 0;
        if (bitPosIsEven || evenBitWasAlreadyClear) {
            value &= ~(1 << oddBit);
        }

        op2.setContent(NumberConversion.intToByte(value, 4), 4);

        // Jump if bit bitPos was clear before we modified
        if (bitIsClear) {
            int target = 0;
            if (op3 instanceof AbsAddress) {
                target = ((AbsAddress) op3).getAdress();
            } else if (op3 instanceof RelAddressing) {
                target = op3.getAdress();
            } else {
                target = NumberConversion.myBytetoIntWithoutSign(op3.getContent());
            }
            machine.getRegisters().getRegister(PC_REGISTER)
                    .setContent(NumberConversion.intToByte(target, 4));
        } else {
            // No jump - increment PC normally
            super.run();
        }
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

        return "JBCCI " + op1.toString() + ", " + op2.toString() + ", " + op3.toString();
    }
}
