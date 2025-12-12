package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;

import java.util.ArrayList;

/**
 * Datendefinition
 *
 * @author Matthias Oehme
 */
public class DD extends Command {

    /**
     * erster Operand
     */
    Operand[] op1;

    /**
     * true, falls es sich um einen 3-Adressbefehl handelt
     */
    boolean three = false;

    /**
     * true, falls es ein Gleitpunktzahlbefehl ist
     */
    boolean floating = false;

    /**
     * Länge Befehls B = 1, H = 2, W = 4, F = 4, D = 8
     */
    int length = 0;

    /**
     * Konstruktor für einen 2-Adress ADD-Befehl
     *
     * @param line   Zeile im Quelltext
     * @param adress Adresse des Befehls
     * @param op1    Array der Operanden
     * @param beg    Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public DD(Machine machine, int line, int adress, Operand[] op1, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        setAdress(adress);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getLabel()
     */
    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<LabelInUse>();
        for (Operand op : op1) {
            if (op instanceof AbsAddress && ((AbsAddress) op).hasLabel()) {
                ret.add(new LabelInUse(this, ((AbsAddress) op).getLabel(),
                        (AbsAddress) op));
            }
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public byte[] encode() {
        MyByte[][] opc1 = new MyByte[op1.length][];
        int x = 0;
        for (int i = 0; i < op1.length; i++) {
            opc1[i] = ((op1[i] instanceof AbsAddress)
                    && (((AbsAddress) op1[i]).hasLabel())) ?
                    ((AbsAddress) op1[i]).getOpCode2() :
                    ((ImmediateOperand) op1[i]).getOpCode2();
            x += opc1[i].length;
        }

        MyByte[] ret = new MyByte[x];
        int i = 0;
        for (MyByte[] bla : opc1) {
            for (MyByte bla2 : bla) {
                ret[i++] = bla2;
            }
        }

        return MyByte.toByteArray(ret);

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#hasLabel()
     */
    @Override
    public boolean hasLabel() {
        for (Operand op : op1) {
            if ((op instanceof AbsAddress) && (((AbsAddress) op).hasLabel())) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public void run() {
        throw new engine.InternalError("Data definitions (DD) are not executable");
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#setAdress(int)
     */
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        int adr = adress;
        for (Operand op : op1) {
            if (op instanceof AbsAddress && ((AbsAddress) op).hasLabel()) {
                ((AbsAddress) op).setOrt(adr);
                adr += op.encode().length;
            }
        }
    }

}
