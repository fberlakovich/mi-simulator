package engine.program;

import engine.commands.Command;

/**
 * Diese Klasse repräsentiert ein definiertes Label für die Symboltabelle.
 *
 * @author Matthias Oehme
 */
public class Label {

    /**
     * Name des Labels
     */
    private String name;

    /**
     * Befehl auf welches das Label zeigt
     */
    private Command ziel;

    /**
     * Instanziiert ein neues label.
     *
     * @param name Name des Labels
     * @param ziel Befehl auf welches das Label zeigt
     */
    public Label(String name, Command ziel) {
        this.name = name;
        this.ziel = ziel;
    }

    /**
     * Gibt die Adresse des zugehörigen Befehls zurück
     *
     * @return Adresse des zugehörigen Befehls zurück
     */
    public int getAdress() {
        return ziel.getAdress();
    }

    /**
     * Gibt den Namen des Labels zurück.
     *
     * @return Name des Labels
     */
    public String getName() {
        return name;
    }

    /**
     * Vergleicht ein LabelInUse mit einem Label auf Grundelage der Namen.
     *
     * @param label zu vergleichendes Label
     * @return ob die Namen gleich sind
     */
    @Override
    public boolean equals(Object label) {
        if (label instanceof LabelInUse)
            return name.equals(((LabelInUse) label).getName());
        return super.equals(label);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString() {
        return name + "   " + Integer.toHexString(ziel.getAdress());
    }
}
