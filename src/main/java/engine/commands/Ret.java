/**
 *
 */
package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;

import static engine.MachineConstants.PC_REGISTER;
import static engine.MachineConstants.SP_REGISTER;

/**
 * Ret-Befehl
 *
 * @author Matthias Oehme
 */
public class Ret extends Command {

    /**
     * Konstruktor
     *
     * @param machine
     *            the machine this command operates on
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Ret(Machine machine, int line, int adress, int beg, int end) {
        super(machine, line, adress, beg, end);

    }

    /**
     * Decodes a RET instruction from memory.
     * MI Manual page 92: RET has opcode 0xF3, no operands.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        return new Ret(machine, 0, pc, 0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("F3");

        return MyByte.toByteArray(new MyByte[]{opcode});

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        CellarAddressing sp = new CellarAddressing(machine, SP_REGISTER, 4, 4, true);
        Register pc = machine.getRegisters().getRegister(PC_REGISTER);
        int target = NumberConversion.myBytetoIntWithSign(sp.getContent());
        pc.setContent(NumberConversion.intToByte(target, 4));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RET";
    }

}
