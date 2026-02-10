package gui;

import engine.Machine;
import engine.MachineContext;

import javax.swing.*;

/**
 * Diese Klasse startet den MISimulator
 */
public class Main {

    /**
     * MainMethode
     *
     * @param args keine
     */
    public static void main(String[] args) {
        // Create machine instance and inject into Window
        MachineContext machine = Machine.getInstance();

        SwingUtilities.invokeLater(() -> {
            Window inst = new Window(machine);
            inst.setVisible(true);
            inst.resetSize();
            inst.setLocationRelativeTo(null);
        });
    }

}
