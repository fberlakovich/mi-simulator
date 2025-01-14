/**
 *
 */
package gui;

import enviroment.Enviroment;
import enviroment.MyByte;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Diese Klasse repräsentiert einen Speicherzeileneintag
 *
 * @author Cyberdyne
 */
public class MemoryTableEntry {

    /** The data. */
    ArrayList<MyByte> data;

    /** The adr. */
    String adr;

    /**
     * Konstruktor für einen Speicherzelleneintrag
     *
     * @param data Daten der Speicherzelle
     * @param adr  the adr
     */
    public MemoryTableEntry(ArrayList<MyByte> data, String adr) {
        this.data = data;
        this.adr = adr;
    }

    /**
     * Setzt die Daten der Speicherzellen
     *
     * @param data
     *            geänderte Daten
     */
    public void setData(ArrayList<MyByte> data) {
        this.data = data;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        boolean html = false;
        StringBuffer ret2 = new StringBuffer(adr + " ");
        int address = Integer.parseInt(this.adr, 16);
        Map<Integer, MyByte> changes = Enviroment.MEMORY.getChanges();

        for (MyByte in : data) {
            if (changes.containsKey(address)) {
                ret2.append(
                        "<font color=\"#ff0000\">" + in.toString() + " </font>");
                html = true;
            } else {
                ret2.append(in.toString() + " ");
            }
            address++;
        }
        return html ? "<html>" + ret2.toString() + "</html>" : ret2.toString();
    }

}
