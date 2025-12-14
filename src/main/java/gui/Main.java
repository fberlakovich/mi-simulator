package gui;

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window inst = new Window();
                inst.setVisible(true);
                inst.resetSize();
                inst.setLocationRelativeTo(null);
            }
        });
    }

}
