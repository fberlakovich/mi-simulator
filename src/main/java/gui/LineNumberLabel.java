/**
 *
 */
package gui;

import Exceptions.NoCommandException;
import engine.Machine;

import javax.swing.*;

/**
 * The Class MyLabel.Die Klasse repräsentiert ein Label, welches Klickbar ist
 *
 * @author Matthias Oehme
 */
public class LineNumberLabel extends JLabel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Status */
    private boolean status = false;

    /** Icons */
    private ImageIcon icon1, icon2;

    /**
     * Instaziiert ein neues Label
     *
     * @param name
     *            Name des Label
     * @param icon1
     *            Icon1
     * @param icon2
     *            Icon2
     */
    public LineNumberLabel(String name, ImageIcon icon1, ImageIcon icon2) {
        super(name);
        setOpaque(true);
        this.icon1 = icon1;
        this.icon2 = icon2;
        setIcon(icon2);
    }

    /**
     * Gibt den Status zurueck
     *
     * @return Status
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Setzt den Breakpoint zurueck
     */
    public void reset() {
        status = false;
        setIcon(icon2);
    }

    /**
     * Setzt den Status und ändert das Icon, falls erforderlich
     *
     * @param status neuer Status
     */
    public void setStatus(boolean status) {
        this.status = status;
        try {
            Machine.getInstance().getProgram().setBreakPoint(Integer.parseInt(getText()), status);
            setText(getText());
            setIcon(status ? icon1 : icon2);
        } catch (NoCommandException e) {
            JOptionPane.showMessageDialog(GuiState.getFrame(),
                    CONSTANTS.ERROR_SETBREAKPOINT_NOT_EXECUTABLE_INSTR,
                    CONSTANTS.ERROR_SETBREAKPOINT_TITEL,
                    JOptionPane.ERROR_MESSAGE);
        }

    }

}
