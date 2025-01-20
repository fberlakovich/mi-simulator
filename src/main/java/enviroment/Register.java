package enviroment;

import gui.CONSTANTS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Diese Klasse repaesentier ein Register der MI
 */
public class Register {

    /**
     * Inhalt des Registers
     */
    private int content;

    /**
     * Stackregister?
     */
    private boolean isStack = false;

    /**
     * Registernr
     */
    private int nr;

    /**
     * geandert? für die Rotfaerbung erforderlich
     */
    private boolean changed = false;

    /**
     * Instanziiert ein Register
     *
     * @param nr Registerbummer
     */
    public Register(int nr) {
        content = 0;
        this.nr = nr;
    }

    /**
     * Instanziiert ein Register
     *
     * @param isStack Stackregister
     */
    public Register(int nr, boolean isStack) {
        this(nr);
        this.isStack = isStack;
    }

    /**
     * Gibt den Inhalt des Registers zurueck
     *
     * @param length Länge des Registerzurgriffs
     * @return Inhalt des Registers
     */
    public MyByte[] getContent(int length) {
        MyByte[] ret;
        switch (length) {
            case 1:
                ret = new MyByte[1];
                ret[0] = new MyByte((byte)(this.content & 0x000000ff));
                return ret;
            case 2:
                ret = new MyByte[2];
                ret[0] = new MyByte((byte)((this.content & 0x0000ff00) >> 8));
                ret[1] = new MyByte((byte)((this.content & 0x000000ff) >> 0));
                return ret;
            case 4:
                ret = new MyByte[4];
                ret[0] = new MyByte((byte)((this.content & 0xff000000) >> 24));
                ret[1] = new MyByte((byte)((this.content & 0x00ff0000) >> 16));
                ret[2] = new MyByte((byte)((this.content & 0x0000ff00) >> 8));
                ret[3] = new MyByte((byte)((this.content & 0x000000ff) >> 0));
                return ret;
            case 8:
                ret = new MyByte[8];
                ret[0] = new MyByte((byte)((this.content & 0xff000000) >> 24));
                ret[1] = new MyByte((byte)((this.content & 0x00ff0000) >> 16));
                ret[2] = new MyByte((byte)((this.content & 0x0000ff00) >> 8));
                ret[3] = new MyByte((byte)((this.content & 0x000000ff) >> 0));
                Register wrap = Enviroment.REGISTERS.getRegister(
                        (nr + 1) % CONSTANTS.NUMBER_OF_REGISTER);
                MyByte[] next = wrap.getContent(4);
                ret[4] = next[0];
                ret[5] = next[1];
                ret[6] = next[2];
                ret[7] = next[3];
                return ret;
            default:
                System.out.println("Nicht zugelassene Zugriffslaenge: " + length);
                return null;
        }
    }

    /**
     * Gibt den Inhalt eines Registers als Integer zurueck
     *
     * @param length Länge des
     * @return Inhalt eines Register
     */
    public int getContentAsNumber(int length) {
        assert length > 0 && length <= 4;
        // TODO: strip to length
        return this.content;
    }

    public void setContentAsNumber(int value) {
        this.content = value;
    }

    /**
     * Setzt den Inhalt eines Registers
     *
     * @param content Inhalt
     */
    public void setContent(MyByte[] data) {
        switch (data.length) {
            case 1:
                content = data[0].getContent();
                break;
            case 2:
                content  = data[0].getContent() << 8;
                content |= data[1].getContent() << 0;
                break;
            case 4:
                content  = data[0].getContent() << 24;
                content |= data[1].getContent() << 16;
                content |= data[2].getContent() << 8;
                content |= data[3].getContent() << 0;
                break;
            case 8:
                content  = data[0].getContent() << 24;
                content |= data[1].getContent() << 16;
                content |= data[2].getContent() << 8;
                content |= data[3].getContent() << 0;
                Register wrap = 
                    Enviroment.REGISTERS.getRegister((nr + 1) % CONSTANTS.NUMBER_OF_REGISTER);
                wrap.setContent(new MyByte[]{data[4], data[5], data[6], data[7]});
                break;
        }

        if (isStack && Enviroment.STACKBEGIN == 0) {
            Enviroment.STACKBEGIN = NumberConversion.myBytetoIntWithSign(data);
        }

        changed = true;
    }

    /**
     * Setzt den Änderungsstatus eines Register zurück
     */
    public void reset() {
        changed = false;
    }

    public boolean isChanged() {
        return changed;
    }
}
