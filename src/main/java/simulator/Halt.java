/**
 *
 */
package simulator;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import core.Constants;

/**
 * Halt-Befehl
 *
 * @author Matthias Oehme
 */
public class Halt extends Command {

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
    public Halt(int line, int adress, int beg, int end) {
        super(line, adress, beg, end);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        MyByte opcode = new MyByte(0);

        return new MyByte[]{opcode};

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        Enviroment.REGISTERS.getRegister(Constants.PC_REGISTER)
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
