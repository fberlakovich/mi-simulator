package engine.scanner;

/**
 * Diese Klasse reprĂ¤sentier ein erkanntes Token / Symbol
 *
 * @author Matthias Oehme
 */
public class Token {

    /**
     * Tokennummer, Zeilennummer
     */
    private int nr, line;

    /**
     * Lexem
     */
    private String text;

    /**
     * Beginn im Quelltext
     */
    private int beg;

    /**
     * Ende im Quelltext
     */
    private int end;

    /**
     * Instantiates a new token.
     *
     * @param nr   Tokennummer
     * @param text Lexem
     * @param line Zeilennummer
     * @param beg  Beginn im Quelltext
     * @param end  Ende im Quelltext
     */
    public Token(int nr, String text, int line, int beg, int end) {
        this.nr = nr;
        this.text = text;
        this.line = line;
        this.beg = beg;
        this.end = end;
    }

    /**
     * Equal.
     *
     * @param zahlen the zahlen
     * @return true, if successful
     */
    public boolean equal(int[] zahlen) {
        for (int x : zahlen) {
            if (x == nr) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the beg.
     *
     * @return the beg
     */
    public int getBeg() {
        return beg;
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets the line.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the nr.
     *
     * @return the nr
     */
    public int getNr() {
        return nr;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return nr + " " + text + " in Line " + line;
    }

    public Token getCopy(int line) {
        return new Token(this.nr, this.text, line, this.beg, this.end);
    }
}
