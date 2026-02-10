package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;
import static engine.MachineConstants.SP_REGISTER;

import java.util.ArrayList;

/**
 * Call-Befehl
 *
 * @author Matthias Oehme
 */
public class Call extends Command {

    /**
     * erster Operand
     */
    Operand op1;

    /**
     * Konstruktor für einen Call-Befehl
     *
     * @param machine the machine this command operates on
     * @param line   Zeile im Quelltext
     * @param adress Adresse des Befehls
     * @param length Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     * @param op1    erster Operand
     * @param beg    Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Call(Machine machine, int line, int adress, int length, Operand op1, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        setAdress(adress);

    }

    /**
     * Decodes a CALL instruction from memory.
     * MI Manual page 92: CALL has opcode 0xF2, 1 operand.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand target = Operand.decode(machine, opcode.length);
        return new Call(machine, 0, pc, opcode.length, target, 0, 0);
    }
    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<>();
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op1).getLabel(),
                    (AbsAddress) op1));
        }

        return ret;
    }
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("F2");

        int x = 1;
        byte[] opc1 = op1.encode();

        MyByte[] ret = new MyByte[opc1.length + 1];
        ret[0] = opcode;
        for (int i = 0; i < opc1.length; i++) {
            ret[x + i] = new MyByte(opc1[i]);
        }
        return MyByte.toByteArray(ret);

    }
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel()));
    }
    @Override
    public synchronized void run() {
        int ziel = 0;
        if (op1 instanceof AbsAddress) {
            ziel = ((AbsAddress) op1).getAdress();
        } else {
            ziel = op1.getAdress();
        }
        // Push return address onto stack (decrement SP, then store)
        CellarAddressing stack = new CellarAddressing(machine, SP_REGISTER, 4, -4, false);
        // PC already advanced past CALL instruction during decoding, so it points to return address
        stack.setContent(NumberConversion.intToByte(machine.getPC(), 4), 4);

        // Jump to target function
        machine.getRegisters().getRegister(PC_REGISTER)
                .setContent(NumberConversion.intToByte(ziel, 4));
    }
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ((AbsAddress) op1).setOrt(adress + 1);
        }
    }
    @Override
    public String toString() {
        return "CALL " + op1.toString();
    }

}
