/**
 *
 */
package simulator;

import codegenerator.LabelInUse;
import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import core.Constants;

import java.util.ArrayList;

/**
 * Jump-Befehl
 *
 * @author Matthias Oehme
 */
public class Jump extends Command {

    /** erster Operand */
    Operand op1;

    /** Typ des Jump-Befehls */
    int typ;

    /**
     * Konstruktor für einen Jump-Befehl
     *
     * @param line
     *            Zeile im Quelltext
     * @param adress
     *            Adresse des Befehls
     * @param op1
     *            erster Operand
     * @param typ
     *            typ des Jump-Befehls
     * @param beg
     *            Zeichenposition - Beginn des Befehlswortes im Quelltext
     * @param end
     *            Zeichenposition - Ende des Befehlswortes im Quelltext
     */
    public Jump(int line, int adress, Operand op1, int typ, int beg, int end) {
        super(line, adress, beg, end);
        this.op1 = op1;
        this.typ = typ;
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
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op1).getLabel(),
                    (AbsAddress) op1)); // +1?
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#getOpCode()
     */
    @Override
    public MyByte[] getOpCode() {
        MyByte opcode = new MyByte((typ > 8 ? 142 : 233) + typ);
        ;

        int x = 1;
        MyByte[] opc1 = op1.getOpCode();

        MyByte[] ret = new MyByte[opc1.length + 1];
        ret[0] = opcode;
        for (int i = 0; i < opc1.length; i++) {
            ret[x + i] = opc1[i];
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#hasLabel()
     */
    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel()));
    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#run()
     */
    @Override
    public synchronized void run() {
        boolean jump = false;
        switch (typ) {
            case 0: // JEQ
                if (Enviroment.flags.isZero()) {
                    jump = true;
                }
                break;
            case 1: // JNE
                if (!Enviroment.flags.isZero()) {
                    jump = true;
                }
                break;
            case 2: // JGT
                if (!Enviroment.flags.isZero() && !Enviroment.flags.isNegative()) {
                    jump = true;
                }
                break;
            case 3: // JGE
                if (Enviroment.flags.isZero() || !Enviroment.flags.isNegative()) {
                    jump = true;
                }
                break;
            case 4: // JLT
                if (Enviroment.flags.isNegative()) {
                    jump = true;
                }
                break;
            case 5: // JLE
                if (Enviroment.flags.isZero() || Enviroment.flags.isNegative()) {
                    jump = true;
                }
                break;
            case 6: // JC
                if (Enviroment.flags.isCarry()) {
                    jump = true;
                }
                break;
            case 7: // JNC
                if (!Enviroment.flags.isCarry()) {
                    jump = true;
                }
                break;
            case 8: // JUMP
                jump = true;
                break;
            case 9: // JV
                jump = Enviroment.flags.isOverflow();
                break;
            case 10: // JNV
                jump = !Enviroment.flags.isOverflow();
                break;

        }
        int ziel = 0;
        if (op1 instanceof AbsAddress) {
            ziel = ((AbsAddress) op1).getAdress();
        } else {
            if (op1 instanceof RelAddressing) {
                ziel = op1.getAdress();
            } else {
                ziel = NumberConversion.myBytetoIntWithoutSign(op1.getContent());
            }
        }
        // System.out.println("Ziel: "+ziel );

        if (jump) {
            // System.out.println("Jump: " + ziel);
            // Enviroment.setNextCommand(nex);
            Enviroment.REGISTERS.getRegister(Constants.PC_REGISTER)
                    .setContent(NumberConversion.intToByte(ziel, 4));
        } else {
            super.run();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see compiler.Command#setAdress(int)
     */
    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ((AbsAddress) op1).setOrt(adress + 1);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String[] suffix = new String[]{"EQ", "NE", "GT", "GE", "LT", "LE", "C", "NC",
                "UMP", "V", "NV"};

        return "J" + suffix[typ] + " " + op1.toString();
    }

}
