/**
 *
 */
package gui;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Die Klasse beinhaltet das ListModel für die Speicherdarstellung
 *
 * @author Matthias oehme
 */
public class MemoryTable extends AbstractListModel<MemoryTableEntry> {

    /** Daten einer Speicherzeile */
    private ArrayList<MemoryTableEntry> data;

    /**
     * Instantiates a new memory table.
     *
     * @param in
     *            the in
     */
    public MemoryTable(ArrayList<MemoryTableEntry> in) {
        data = in;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.ListModel#getElementAt(int)
     */
    @Override
    public MemoryTableEntry getElementAt(int index) {
        return data.get(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.ListModel#getSize()
     */
    @Override
    public int getSize() {
        return data.size();
    }

    public void fireRowUpdated(int index) {
        fireContentsChanged(this, index, index);
    }

    public ArrayList<MemoryTableEntry> getData() {
        return data;
    }
}
