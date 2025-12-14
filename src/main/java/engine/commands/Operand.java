package engine.commands;

import engine.Machine;
import engine.state.MyByte;

/**
 * Interface für eine Operandenspezifikation
 */
public interface Operand {

    /**
     * Gets the machine this operand operates on.
     *
     * @return the machine
     */
    Machine getMachine();

    Operand copy();

    /**
     * Gibt des Adresse des Operanden zurück
     *
     * @return Adresse des Operanden
     */
    int getAdress();

    /**
     * Gibt den Inhalt des Operanden zurück
     *
     * @return Inhalt als MyByte-Array
     */
    MyByte[] getContent();

    /**
     * Encodes this operand to bytes.
     *
     * @return the encoded bytes
     */
    byte[] encode();

    /**
     * Setzt den Inhalt des Operanden.
     *
     * @param content neues Inhalt als MyByte-Array
     * @param length  Anzahl der Bytes die neugesetzt werden
     */
    void setContent(MyByte[] content, int length);

    /**
     * Decodes an operand from memory at the current PC position.
     * Advances the PC as bytes are consumed.
     *
     * @param machine the machine to decode from
     * @param length the data type length (1=Byte, 2=Halfword, 4=Word, 8=Double)
     * @return the decoded Operand
     * @throws OpcodeDecodeException if the operand cannot be decoded
     */
    static Operand decode(Machine machine, int length) throws OpcodeDecodeException {
        return OperandDecoder.decode(machine, length);
    }
}
