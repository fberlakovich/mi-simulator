/**
 *
 */
package engine.program;

import engine.commands.AbsAddress;
import engine.commands.Command;

/**
 * Repräsentiert einen Eintrag in der Symboltabelle für ein genutztes Label
 *
 * @author Cyberdyne
 */
public class LabelInUse {

    /**
     * Name des Labels
     */
    private String name;

    /**
     * Befehl in welchem das Label genutzt wird.
     */
    private Command command;

    /**
     * Operand in welchem das Label genutzt wird
     */
    private AbsAddress op;

    /**
     * Zugehöriges definiertes Label
     */
    private Label label;

    /**
     * Instanziert ein genutzes Label
     *
     * @param com
     *            Befehl in welchem das Label genutzt wird
     * @param name
     *            name des Labels
     * @param op
     *            Operand in welchem das Label genutzt wird
     */
    public LabelInUse(Command com, String name, AbsAddress op) {
        command = com;
        this.name = name;
        this.op = op;
        this.label = null;
    }

    /**
     * Gibt die Adresse zurück an welcher das Label genutzt wird
     *
     * @return the adress
     */
    public int getAdress() {
        return op.getOrt();
    }

    /**
     * Gib den zugehörigen Befehl zurück
     *
     * @return zugehöriger Befehl
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Gibt den Namen zurück
     *
     * @return Namen des Labels
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt den zugehörigen Operanden zurück
     *
     * @return zugehöriger Operanden
     */
    public AbsAddress getOp() {
        return op;
    }

    /**
     * Vergleicht ein Labe mit einem LabelInUse auf Grundelage der Namen.
     *
     * @param label
     *            zu vergleichendes LabelInUse
     * @return ob die Namen gleich sind
     */
    @Override
    public boolean equals(Object label) {
        if (label instanceof Label)
            return name.equals(((Label) label).getName());
        return super.equals(label);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

}
