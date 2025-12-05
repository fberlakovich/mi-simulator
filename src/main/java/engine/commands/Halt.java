/**
 *
 */
package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;

/**
 * Halt-Befehl
 *
 * @author Matthias Oehme
 */
public class Halt extends Command {

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
    public Halt(Machine machine, int line, int adress, int beg, int end) {
        super(machine, line, adress, beg, end);

    }

    /**
     * Decodes a HALT instruction from memory.
     * MI Manual page 89: HALT has opcode 0x00, no operands.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        return new Halt(machine, 0, pc, 0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte(0);

        return MyByte.toByteArray(new MyByte[]{opcode});

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        machine.getRegisters().getRegister(PC_REGISTER)
                .setContent(NumberConversion.intToByte(adress, 4));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HALT";
    }
}
