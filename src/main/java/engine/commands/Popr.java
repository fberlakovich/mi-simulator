package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;

import static engine.MachineConstants.SP_REGISTER;

/**
 * POPR-Befehl
 *
 * @author Matthias Oehme
 */
public class Popr extends Command {

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
    public Popr(Machine machine, int line, int adress, int beg, int end) {
        super(machine, line, adress, beg, end);

    }

    /**
     * Decodes a POPR instruction from memory.
     * MI Manual page 92: POPR has opcode 0xF5, no operands.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        return new Popr(machine, 0, pc, 0, 0);
    }
    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte("F5");
        return MyByte.toByteArray(new MyByte[]{opcode});

    }
    @Override
    public synchronized void run() {
        super.run();
        Register sp = machine.getRegisters().getRegister(SP_REGISTER);
        for (int i = 0; i < 15; i++) {
            Register reg = machine.getRegisters().getRegister(i);
            AbsAddress ort = new AbsAddress(machine,
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)), 4, 0);
            reg.setContent(ort.getContent());
            sp.setContent(NumberConversion.intToByte(
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)) + 4, 4));
        }

    }
    @Override
    public String toString() {
        return "POPR";
    }

}
