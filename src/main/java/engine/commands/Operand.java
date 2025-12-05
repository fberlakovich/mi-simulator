package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

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
        int opcode = machine.fetchByte();

        // Immediate addressing <= 63
        if (opcode < 64) {
            return new ImmediateOperand(machine, NumberConversion.intToByte(opcode, length), length);
        }

        // Immediate addressing (full value)
        if (opcode == 143) {
            machine.addToPC(length);
            return new ImmediateOperand(machine,
                    machine.getMemory().getContent(machine.getPC() - length, length),
                    length);
        }

        // Absolute addressing
        if (opcode == 159) {
            machine.addToPC(4);
            return new AbsAddress(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 4, 4)), length,
                    machine.getPC() - 4);
        }

        // Indexed addressing
        if (opcode >= 64 && opcode <= 79) {
            int index = opcode - 64;
            opcode = machine.fetchByte();

            // Relative addressing without offset
            if (opcode >= 96 && opcode <= 111) {
                return new RelAddressing(machine, 0, opcode - 96, index, length, machine.getPC());
            }

            // Relative addressing with byte offset
            if (opcode >= 160 && opcode <= 175) {
                machine.addToPC(1);
                return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 1, 1)),
                        opcode - 160, index, length, machine.getPC() - 1);
            }

            // Relative addressing with halfword offset
            if (opcode >= 192 && opcode <= 207) {
                machine.addToPC(2);
                return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 2, 2)),
                        opcode - 192, index, length, machine.getPC() - 2);
            }

            // Relative addressing with word offset
            if (opcode >= 224 && opcode <= 239) {
                machine.addToPC(4);
                return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 4, 4)),
                        opcode - 224, index, length, machine.getPC() - 4);
            }

            // Indirect addressing with byte offset
            if (opcode >= 176 && opcode <= 191) {
                machine.addToPC(1);
                return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 1, 1)),
                        opcode - 176, index, length, machine.getPC() - 1);
            }

            // Indirect addressing with halfword offset
            if (opcode >= 208 && opcode <= 223) {
                machine.addToPC(2);
                return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 2, 2)),
                        opcode - 208, index, length, machine.getPC() - 2);
            }

            // Indirect addressing with word offset
            if (opcode >= 240 && opcode <= 255) {
                machine.addToPC(4);
                return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                        machine.getMemory().getContent(machine.getPC() - 4, 4)),
                        opcode - 240, index, length, machine.getPC() - 4);
            }

            throw new OpcodeDecodeException("Invalid indexed operand opcode: " + opcode,
                    machine.getPC());
        }

        // Register addressing
        if (opcode >= 80 && opcode <= 95) {
            return new RegisterAddressing(machine, opcode - 80, length);
        }

        // Relative addressing without offset
        if (opcode >= 96 && opcode <= 111) {
            return new RelAddressing(machine, 0, opcode - 96, length, machine.getPC());
        }

        // Relative addressing with byte offset
        if (opcode >= 160 && opcode <= 175) {
            machine.addToPC(1);
            return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 1, 1)),
                    opcode - 160, length, machine.getPC() - 1);
        }

        // Relative addressing with halfword offset
        if (opcode >= 192 && opcode <= 207) {
            machine.addToPC(2);
            return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 2, 2)),
                    opcode - 192, length, machine.getPC() - 2);
        }

        // Relative addressing with word offset
        if (opcode >= 224 && opcode <= 239) {
            machine.addToPC(4);
            return new RelAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 4, 4)),
                    opcode - 224, length, machine.getPC() - 4);
        }

        // Indirect addressing with byte offset
        if (opcode >= 176 && opcode <= 191) {
            machine.addToPC(1);
            return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 1, 1)),
                    opcode - 176, length, machine.getPC() - 1);
        }

        // Indirect addressing with halfword offset
        if (opcode >= 208 && opcode <= 223) {
            machine.addToPC(2);
            return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 2, 2)),
                    opcode - 208, length, machine.getPC() - 2);
        }

        // Indirect addressing with word offset
        if (opcode >= 240 && opcode <= 255) {
            machine.addToPC(4);
            return new IndAddressing(machine, NumberConversion.myBytetoIntWithSign(
                    machine.getMemory().getContent(machine.getPC() - 4, 4)),
                    opcode - 240, length, machine.getPC() - 4);
        }

        // Stack addressing (pre-decrement)
        if (opcode >= 112 && opcode <= 126) {
            return new CellarAddressing(machine, opcode - 112, length, -length, false);
        }

        // Stack addressing (post-increment)
        if (opcode >= 128 && opcode <= 142) {
            return new CellarAddressing(machine, opcode - 128, length, length, true);
        }

        throw new OpcodeDecodeException("Invalid operand opcode: " + opcode, machine.getPC());
    }
}
