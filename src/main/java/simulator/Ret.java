/**
 *
 */
package simulator;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import enviroment.Register;
import core.Constants;

/**
 * Ret-Befehl
 *
 * @author Matthias Oehme
 */
public class Ret extends Command {

    /**
     * Konstruktor
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Ret(int line, int adress, int beg, int end) {
        super(line, adress, beg, end);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        MyByte opcode = new MyByte("F3");

        return new MyByte[]{opcode};

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        CellarAddressing sp = new CellarAddressing(Constants.SP_REGISTER, 4, 4, true);
        Register pc = Enviroment.REGISTERS.getRegister(Constants.PC_REGISTER);
        int ziel = NumberConversion.myBytetoIntWithSign(sp.getContent());
        pc.setContent(NumberConversion.intToByte(ziel, 4));
        // Enviroment.setNextCommand(Enviroment.getCommand(ziel));

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
