/**
 *
 */
package engine.scanner;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Diese Klasse wird für dier Verwaltung von der Compileroption Equal verwendet
 *
 * @author Matthias Oehme
 */
public class Equal {

    /**
     * Ersetzungsname
     */
    private String name;

    /**
     * Tokenliste für die Ersetzung
     */
    private ArrayList<Token> makro = new ArrayList<Token>();

    /**
     * Konstruktor für ein neues Equal
     *
     * @param name  Ersetzungsname
     * @param makro Tokenliste für die Ersetzung
     */
    public Equal(String name, ArrayList<Token> makro) {
        this.name = name;
        this.makro = makro;
    }

    /**
     * gibt einen Iterator zurück
     *
     * @return the iterator
     */
    public Iterator<Token> getIterator() {
        return makro.iterator();
    }

    /**
     * Gibt den Namen zurück
     *
     * @return Name
     */
    public String getName() {
        return name;
    }
}
