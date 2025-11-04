package simulator;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import core.Constants;

/**
 * Operandenspezifikation für die Absolute Adressierung
 *
 * @author Matthias Oehme
 */
public class AbsAddress implements Operand, AdressGetter {

    /**
     * Die Adresse für den Operanden
     */
    private int adress;

    /**
     * Die Länge des Operanden
     */
    private int length;

    /**
     * Labelname
     */
    private String label = "";

    /**
     * Speicherstelle des Operanden
     */
    private int ort = 0;

    /**
     * Offset der Oprenaden
     */
    private int offset = 0;

    /**
     * Konstruktor für eine Operanden - Absolute Adressierung
     *
     * @param adress Adresse des Operanden
     * @param length Länge des Operanden
     * @param ort    Speicherstelle des Operanden
     */
    public AbsAddress(int adress, int length, int ort) {
        this.adress = adress;
        this.length = length;
        this.ort = ort;
    }

    /**
     * Konstruktor für eine Operanden - Absolute Adressierung mit einem Label
     *
     * @param name   Labelname des Operanden
     * @param length Länge des Operanden
     * @param ort    Speicherstelle des Operanden
     * @param offset Offset des Operanden
     */
    public AbsAddress(String name, int length, int ort, int offset) {
        label = name;
        this.length = length;
        this.ort = ort;
        this.offset = offset;
    }

    @Override
    public Operand copy() {
        return new AbsAddress(label, length, ort, offset);
    }

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#getAdress()
     */
    @Override
    public int getAdress() {
        return adress;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#getContent()
     */
    @Override
    public MyByte[] getContent() {
        return Enviroment.MEMORY.getContent(adress + offset, length);
    }

    /**
     * Gibt den Labelnamen zurück.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#getOpCode()
     */

    /*
     * (non-Javadoc)
     *
     * @see interpreter.Operand#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        if (hasLabel()) {
            return new RelAddressing(adress - ort + offset - 1, Constants.PC_REGISTER,
                    length, ort).getOpCode();
        }
        MyByte[] ret = new MyByte[5];
        ret[0] = new MyByte(159);
        MyByte[] addr = NumberConversion.intToByte(adress + offset, 4);
        for (int i = 0; i < 4; i++) {
            ret[i + 1] = addr[i];
        }
        return ret;
    }

    public MyByte[] getOpCode2() {
        MyByte[] ret = new MyByte[4];
        MyByte[] addr = NumberConversion.intToByte(adress + offset, 4);
        for (int i = 0; i < 4; i++) {
            ret[i] = addr[i];
        }
        return ret;
    }

    /**
     * Gibt die Speichertstelle der Operanden zurück.
     *
     * @return Speicherstelle des Operanden
     */
    public int getOrt() {
        return ort;
    }

    /**
     * Überprüft ob der Operand ein Label enthält
     *
     * @return true, wenn Label enthalten ist
     */
    public boolean hasLabel() {
        return !(label.equalsIgnoreCase(""));
    }

    /**
     * Setzt die Adresse auf den der Operand zeigt
     *
     * @param adr Speicherstelle auf die der Operand zeigen soll
     */
    public void setAdress(int adr) {
        adress = adr;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Operand#setContent(compiler.MyByte[], int)
     */
    @Override
    public void setContent(MyByte[] content, int length) {
        Enviroment.MEMORY.setContent(adress + offset, content);
    }

    /**
     * Setzt den Ort des Operanden im Speicher
     *
     * @param ort Speicherstelle des Operandne
     */
    public void setOrt(int ort) {
        this.ort = ort;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Integer.toString(getAdress()) + (offset != 0 ?
                Integer.toString(offset) :
                "");
    }

}
