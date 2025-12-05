/**
 *
 */
package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;

import java.util.ArrayList;

/**
 * Res-Befehl
 *
 * @author Matthias Oehme
 */
public class Res extends Command {

    /**
     * Länge der Reservierung
     */
    int bytes = 0;

    /**
     * Konstruktor für einen Res-befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param bytes
     *            Länge des zu reservierenden Bereiches
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Res(Machine machine, int line, int adress, int bytes, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.bytes = bytes;
        setAdress(adress);
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getLabel()
     */
    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<LabelInUse>();
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte[] ret = new MyByte[bytes];
        for (int i = 0; i < bytes; i++) {
            ret[i] = new MyByte(0);
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
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        super.run();
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#setAdress(int)
     */
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RES";
    }

}
