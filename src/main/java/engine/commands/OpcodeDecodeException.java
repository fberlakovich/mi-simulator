package engine.commands;

/**
 * Exception thrown when opcode decoding fails.
 */
public class OpcodeDecodeException extends RuntimeException {

    private final int address;
    private final int opcodeValue;

    public OpcodeDecodeException(String message, int address, int opcodeValue) {
        super(message + " at address 0x" + Integer.toHexString(address) +
              " (opcode: 0x" + Integer.toHexString(opcodeValue) + ")");
        this.address = address;
        this.opcodeValue = opcodeValue;
    }

    public OpcodeDecodeException(String message, int address) {
        super(message + " at address 0x" + Integer.toHexString(address));
        this.address = address;
        this.opcodeValue = -1;
    }

    public int getAddress() {
        return address;
    }

    public int getOpcodeValue() {
        return opcodeValue;
    }
}