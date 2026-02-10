package engine.state;

/**
 * Interface for machine memory.
 * Allows different implementations for testing, serialization, or alternative backends.
 */
public interface MemoryInterface {

    /**
     * Reads bytes from storage.
     *
     * @param address starting address
     * @param length number of bytes to read
     * @return byte array of the requested length
     */
    byte[] read(int address, int length);

    /**
     * Writes bytes to storage.
     *
     * @param address starting address
     * @param data bytes to write
     */
    void write(int address, byte[] data);

    /**
     * Reads a single byte from storage.
     *
     * @param address the address
     * @return the byte value
     */
    byte readByte(int address);

    /**
     * Writes a single byte to storage.
     *
     * @param address the address
     * @param value the byte value
     */
    void writeByte(int address, byte value);

    /**
     * Reads a 32-bit integer from storage (big-endian).
     *
     * @param address starting address
     * @return the integer value
     */
    int readInt(int address);

    /**
     * Writes a 32-bit integer to storage (big-endian).
     *
     * @param address starting address
     * @param value the integer value
     */
    void writeInt(int address, int value);

    /**
     * Reads a 64-bit long from storage (big-endian).
     *
     * @param address starting address
     * @return the long value
     */
    long readLong(int address);

    /**
     * Writes a 64-bit long to storage (big-endian).
     *
     * @param address starting address
     * @param value the long value
     */
    void writeLong(int address, long value);

    /**
     * Gets the size of this storage in bytes.
     *
     * @return storage size
     */
    int getSize();

    /**
     * Resets all storage to initial state (zeros).
     */
    void reset();
}
