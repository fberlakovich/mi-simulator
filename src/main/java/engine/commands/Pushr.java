/**
 *
 */
package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;

import static engine.MachineConstants.SP_REGISTER;

/**
 * Pushr-Befehl
 *
 * @author Matthias Oehme
 */
public class Pushr extends Command {

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
    public Pushr(Machine machine, int line, int adress, int length, int beg, int end) {
        super(machine, line, adress, beg, end);

    }

    /**
     * Decodes a PUSHR instruction from memory.
     * MI Manual page 92: PUSHR has opcode 0xF4, no operands.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        return new Pushr(machine, 0, pc, 4, 0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("F4");

        return MyByte.toByteArray(new MyByte[]{opcode});

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        super.run();
        Register sp = machine.getRegisters().getRegister(SP_REGISTER);
        for (int i = 14; i >= 0; i--) {
            Register reg = machine.getRegisters().getRegister(i);
            sp.setContent(NumberConversion.intToByte(
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)) - 4, 4));
            AbsAddress ort = new AbsAddress(machine,
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)), 4, 0);
            ort.setContent(reg.getContent(4), 4);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PUSHR";
    }
}
