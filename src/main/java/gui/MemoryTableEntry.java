/**
 *
 */
package gui;

import engine.util.MemoryChangeTracker;
import engine.state.MyByte;

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

    /** Memory change tracker for highlighting. */
    private final MemoryChangeTracker tracker;

    /**
     * Konstruktor für einen Speicherzelleneintrag
     *
     * @param data Daten der Speicherzelle
     * @param adr  the adr
     * @param tracker the memory change tracker (may be null)
     */
    public MemoryTableEntry(ArrayList<MyByte> data, String adr, MemoryChangeTracker tracker) {
        this.data = data;
        this.adr = adr;
        this.tracker = tracker;
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

        for (MyByte in : data) {
            boolean changed = tracker != null && tracker.isChanged(address);
            if (changed) {
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
