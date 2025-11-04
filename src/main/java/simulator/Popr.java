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
 * POPR-Befehl
 *
 * @author Matthias Oehme
 */
public class Popr extends Command {

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
    public Popr(int line, int adress, int beg, int end) {
        super(line, adress, beg, end);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        MyByte opcode = new MyByte("F5");
        return new MyByte[]{opcode};

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        super.run();
        Register sp = Enviroment.REGISTERS.getRegister(Constants.SP_REGISTER);
        for (int i = 0; i < 15; i++) {
            Register reg = Enviroment.REGISTERS.getRegister(i);
            AbsAddress ort = new AbsAddress(
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)), 4, 0);
            reg.setContent(ort.getContent());
            sp.setContent(NumberConversion.intToByte(
                    NumberConversion.myBytetoIntWithoutSign(sp.getContent(4)) + 4, 4));
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "POPR";
    }

}
