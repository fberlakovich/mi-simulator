package simulator;

import codegenerator.LabelInUse;
import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import core.Constants;

import java.util.ArrayList;

/**
 * Abstrakte Oberklasse für Befehle
 */
public abstract class Command {

    /**
     * Zeile des Quelltextes in der der Befehl steht
     */
    protected int line;

    /**
     * Adresse des Befehls im Speicher
     */
    protected int adress;

    /**
     * Zeichenpostition - Beginn des Befehlswortes im Quelltext
     */
    protected int beg;

    /**
     * Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    protected int end;

    /**
     * vorheriger Befehl
     */
    private Command pre;

    /**
     * nachfolgender Befehl
     */
    protected Command next;

    /**
     * Breakpoint
     */
    private boolean breakPoint = false;

    /**
     * Konstruktor für einen Befehl
     *
     * @param line   Zeile des Quelltextes in der der Befehl steht
     * @param adress Adresse des Befehls im Speicher
     * @param beg    Zeichenpostition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    public Command(int line, int adress, int beg, int end) {
        this.line = line;
        this.adress = adress;
        this.beg = beg;
        this.end = end;
    }

    /**
     * Gibt die Adresse des Befehls zurück
     *
     * @return Adresse des Befehls
     */
    public int getAdress() {
        return adress;

    }

    /**
     * Gibt die Zeichenpostition - Beginn des Befehlswortes im Quelltext zurück
     *
     * @return Zeichenpostition - Beginn des Befehlswortes im Quelltext
     */
    public int getBeg() {
        return beg;
    }

    /**
     * Gibt die Zeichenpostition - Ende des Befehlswortes im Quelltext zurück
     *
     * @return Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gibt die Label des Befehls zurück
     *
     * @return Label des Befehls
     */
    public ArrayList<LabelInUse> getLabel() {
        return new ArrayList<LabelInUse>();
    }

    /**
     * Gibt die Zeile des Befehls im Quelltext zurück
     *
     * @return Zeile des Befehls im Quelltext
     */
    public int getLine() {
        return line;
    }

    /**
     * Gibt den nachfolgenden Befehl zurück
     *
     * @return nachfolgenden Befehl
     */
    public Command getNext() {
        return next;
    }

    /**
     * Gibt den Opcode des Befehls zurück
     *
     * @return Opcode des Befehls
     */
    public MyByte[] getOpCode() {
        return null;
    }

    /**
     * Gibt den vorherigen Befehl zurück
     *
     * @return vorherigen Befehl
     */
    public Command getPre() {
        return pre;
    }

    /**
     * Gibt zurück ob ein Breakpoint gesetz wurde
     *
     * @return true, wenn Breakpoint gesetz
     */
    public boolean hasBreakPoint() {
        return breakPoint;
    }

    /**
     * Gibt zurück ob der Befehl label enthält
     *
     * @return true, wenn Befehl label enthält
     */
    public boolean hasLabel() {
        return getLabel().size() != 0;
    }

    /**
     * Methode zur Ausführung eines Befehls
     */
    public synchronized void run() {
        Enviroment.REGISTERS.getRegister(Constants.PC_REGISTER).setContent(
                NumberConversion.intToByte(getAdress() + getOpCode().length, 4));

    }

    /**
     * Setzt die Adresse des Befehls
     *
     * @param adress Adresse des Befehls
     */
    public void setAdress(int adress) {
        this.adress = adress;
    }

    /**
     * Setzt einen Breskpoint
     *
     * @param wert Breakpoint, true um Breakpoint zu setzen
     */
    public void setBreakPoint(boolean wert) {
        breakPoint = wert;
    }

    /**
     * Setzt den nachfolgenden Befehl
     *
     * @param next nachfolgenden Befehl
     */
    public void setNext(Command next) {
        this.next = next;
    }

    /**
     * Setzt den vorherigen Befehl
     *
     * @param pre vorherigen Befehl
     */
    public void setPre(Command pre) {
        this.pre = pre;
    }

}
