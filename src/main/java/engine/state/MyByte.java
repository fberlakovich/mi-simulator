package engine.state;

import java.util.Objects;

/**
 * Diese Klasse repraesentier ein Byte
 */
public class MyByte {

    /**
     * Wert den Bytes als Integer
     */
    private byte content;

    /**
     * Carryflag
     */
    private boolean carry = false;

    public MyByte(byte content) {
        this.content = content;
    }

    /**
     * Konstruktor
     *
     * @param content Wert den Bytes
     */
    public MyByte(int content) {
        setContent(content);
    }

    /**
     * Konstruktor
     *
     * @param hex Wert den Bytes als Hexzahl
     */
    public MyByte(String hex) {
        int content = Integer.parseInt(hex, 16);
        this.content = (byte) (content % 256);
        if (content > 255) {
            carry = true;
        }
    }

    /**
     * Addiert zwei MyBytes
     *
     * @param opp MyBytes
     * @return Ergbenis als MyByte
     */
    public MyByte add(MyByte opp) {
        return new MyByte((content + opp.getContent()));
    }

    /**
     * Erstellt eine Kopie des Objekts
     *
     * @return Kopie des Objekts
     */
    public MyByte copy() {
        return new MyByte(getCarry() ? content + 256 : content);
    }

    /**
     * Vergleicht zwei MyByte
     *
     * @param one MyByte
     * @return true, if successful
     */
    public boolean hasSameContent(MyByte one) {
        return getContent() == one.getContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyByte myByte = (MyByte) o;
        return content == myByte.content && carry == myByte.carry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, carry);
    }

    /**
     * Gibt das Carry zurueck
     *
     * @return Carry
     */
    public boolean getCarry() {
        return carry;
    }

    /**
     * Gibt den Inhalt als Integer zurueck
     *
     * @return Inhalt als Integer
     */
    public int getContent() {
        return ((int)content) & 0xFF;
    }

    /**
     * Negiert ein MyBYte
     *
     * @return zu negierendes MyByte
     */
    public MyByte negate() {
        return new MyByte(getContent() ^ 0xff);
    }

    /**
     * Setzt den Inhalt des MyByte
     *
     * @param content neuer Inhalt
     */
    public void setContent(int content) {
        this.content = (byte) (content % 256);
        if (content > 255) {
            carry = true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String ret = Integer.toString(getContent(), 16).toUpperCase();

        return (ret.length() == 1) ? "0" + ret : ret;
    }

    /**
     * Returns the raw byte value.
     *
     * @return the byte value
     */
    public byte toByte() {
        return content;
    }

    /**
     * Converts a MyByte array to a byte array.
     *
     * @param myBytes the MyByte array
     * @return the byte array
     */
    public static byte[] toByteArray(MyByte[] myBytes) {
        byte[] result = new byte[myBytes.length];
        for (int i = 0; i < myBytes.length; i++) {
            result[i] = myBytes[i].content;
        }
        return result;
    }

    /**
     * Converts a byte array to a MyByte array.
     *
     * @param bytes the byte array
     * @return the MyByte array
     */
    public static MyByte[] fromByteArray(byte[] bytes) {
        MyByte[] result = new MyByte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = new MyByte(bytes[i]);
        }
        return result;
    }
}
