package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;

import java.util.ArrayList;

/**
 * Abstrakte Oberklasse für Befehle
 */
public abstract class Command {

    /**
     * The machine this command operates on.
     */
    protected final Machine machine;

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
     * Konstruktor für einen Befehl
     *
     * @param machine the machine this command operates on
     * @param line   Zeile des Quelltextes in der der Befehl steht
     * @param adress Adresse des Befehls im Speicher
     * @param beg    Zeichenpostition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    public Command(Machine machine, int line, int adress, int beg, int end) {
        this.machine = machine;
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
     * Encodes this command to bytes.
     *
     * @return the encoded bytes
     */
    public byte[] encode() {
        return new byte[0];
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
        machine.getRegisters().getRegister(PC_REGISTER).setContent(
                NumberConversion.intToByte(getAdress() + encode().length, 4));
    }

    /**
     * Gets the machine this command operates on.
     *
     * @return the machine
     */
    public Machine getMachine() {
        return machine;
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
