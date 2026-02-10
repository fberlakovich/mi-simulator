package gui;

import engine.program.Label;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * Table model for displaying labels and their addresses.
 */
public class LabelTableModel implements TableModel {

    /** Label data */
    private ArrayList<Label> data;

    /**
     * Creates a new label table model.
     *
     * @param labels the list of labels to display
     */
    public LabelTableModel(ArrayList<Label> labels) {
        data = labels;
    }

    @Override
    public void addTableModelListener(TableModelListener listener) {
        // Not supported - table is read-only
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Label";
            case 1:
                return "Address (dec)";
            case 2:
                return "Address (hex)";
            default:
                return null;
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        Label label = data.get(row);
        switch (column) {
            case 0:
                return label.getName();
            case 1:
                return Integer.toString(label.getAdress());
            case 2:
                return Integer.toHexString(label.getAdress());
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener listener) {
        // Not supported - table is read-only
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        // Not supported - table is read-only
    }
}
