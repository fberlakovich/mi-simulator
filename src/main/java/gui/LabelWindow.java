/**
 *
 */
package gui;

import engine.program.Label;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Cyberdyne
 *
 */
public class LabelWindow extends JFrame {
    private JTable table = new JTable();

    public LabelWindow() {
        super();
        this.add(new JScrollPane(table));
    }

    public void setContent(ArrayList<Label> list) {
        table.setModel(new LabelTableModel(list));

    }

}
