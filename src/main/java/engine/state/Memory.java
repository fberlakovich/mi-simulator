package engine.state;

import engine.Machine;
import engine.events.MemoryAccessErrorEvent;
import engine.events.MemoryUpdateEvent;
import engine.util.NumberConversion;

import static engine.MachineConstants.MEMORY_SIZE;
import static engine.ErrorMessages.ERROR_MEMORY_TEXT;

/**
 * MI machine memory implementation.
 * Implements the Storage interface for abstraction while maintaining
 * backwards compatibility with the MyByte-based API.
 *
 * Frontends that need to track memory changes should subscribe to
 * {@link MemoryUpdateEvent} via {@link Enviroment#getEventBus()}.
 */
public class Memory implements MemoryInterface {

    /** Raw memory storage */
    private final byte[] data;

    /**
     * Creates a new memory instance with default size.
     */
    public Memory() {
        this(MEMORY_SIZE);
    }

    /**
     * Creates a new memory instance with specified size.
     *
     * @param size memory size in bytes
     */
    public Memory(int size) {
        this.data = new byte[size];
    }

    // ========== Storage Interface Implementation ==========

    @Override
    public byte[] read(int address, int length) {
        if (!checkBounds(address, length)) {
            return new byte[length];
        }
        byte[] result = new byte[length];
        System.arraycopy(data, address, result, 0, length);
        return result;
    }

    @Override
    public void write(int address, byte[] bytes) {
        if (!checkBounds(address, bytes.length)) {
            return;
        }
        for (int i = 0; i < bytes.length; i++) {
            int addr = address + i;
            data[addr] = bytes[i];
            Machine.getInstance().getEventBus().publish(new MemoryUpdateEvent(addr));
        }
    }

    @Override
    public byte readByte(int address) {
        if (!checkBounds(address, 1)) {
            return 0;
        }
        return data[address];
    }

    @Override
    public void writeByte(int address, byte value) {
        if (!checkBounds(address, 1)) {
            return;
        }
        data[address] = value;
        Machine.getInstance().getEventBus().publish(new MemoryUpdateEvent(address));
    }

    @Override
    public int readInt(int address) {
        if (!checkBounds(address, 4)) {
            return 0;
        }
        return ((data[address] & 0xFF) << 24) |
               ((data[address + 1] & 0xFF) << 16) |
               ((data[address + 2] & 0xFF) << 8) |
               (data[address + 3] & 0xFF);
    }

    @Override
    public void writeInt(int address, int value) {
        if (!checkBounds(address, 4)) {
            return;
        }
        data[address] = (byte) ((value >> 24) & 0xFF);
        data[address + 1] = (byte) ((value >> 16) & 0xFF);
        data[address + 2] = (byte) ((value >> 8) & 0xFF);
        data[address + 3] = (byte) (value & 0xFF);
        for (int i = 0; i < 4; i++) {
            Machine.getInstance().getEventBus().publish(new MemoryUpdateEvent(address + i));
        }
    }

    @Override
    public long readLong(int address) {
        if (!checkBounds(address, 8)) {
            return 0;
        }
        return ((long)(data[address] & 0xFF) << 56) |
               ((long)(data[address + 1] & 0xFF) << 48) |
               ((long)(data[address + 2] & 0xFF) << 40) |
               ((long)(data[address + 3] & 0xFF) << 32) |
               ((long)(data[address + 4] & 0xFF) << 24) |
               ((long)(data[address + 5] & 0xFF) << 16) |
               ((long)(data[address + 6] & 0xFF) << 8) |
               ((long)(data[address + 7] & 0xFF));
    }

    @Override
    public void writeLong(int address, long value) {
        if (!checkBounds(address, 8)) {
            return;
        }
        data[address] = (byte) ((value >> 56) & 0xFF);
        data[address + 1] = (byte) ((value >> 48) & 0xFF);
        data[address + 2] = (byte) ((value >> 40) & 0xFF);
        data[address + 3] = (byte) ((value >> 32) & 0xFF);
        data[address + 4] = (byte) ((value >> 24) & 0xFF);
        data[address + 5] = (byte) ((value >> 16) & 0xFF);
        data[address + 6] = (byte) ((value >> 8) & 0xFF);
        data[address + 7] = (byte) (value & 0xFF);
        for (int i = 0; i < 8; i++) {
            Machine.getInstance().getEventBus().publish(new MemoryUpdateEvent(address + i));
        }
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public void reset() {
        java.util.Arrays.fill(data, (byte) 0);
    }

    // ========== Legacy MyByte API (for backwards compatibility) ==========

    /**
     * Gets memory content as MyByte array.
     *
     * @param begin  start address
     * @param length number of bytes
     * @return content as MyByte array
     */
    public MyByte[] getContent(int begin, int length) {
        if (!checkBounds(begin, length)) {
            return NumberConversion.intToByte(0, length);
        }
        MyByte[] ret = new MyByte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new MyByte(data[begin + i]);
        }
        return ret;
    }

    /**
     * Gets memory content as signed integer.
     *
     * @param begin  start address
     * @param length number of bytes (max 4)
     * @return content as signed integer
     */
    public int getContentAsInt(int begin, int length) {
        if (!checkBounds(begin, length)) {
            return 0;
        }
        MyByte[] ret = new MyByte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new MyByte(data[begin + i]);
        }
        return NumberConversion.myBytetoIntWithSign(ret);
    }

    /**
     * Gets memory content as unsigned long.
     *
     * @param begin  start address
     * @param length number of bytes (max 8)
     * @return content as unsigned long
     */
    public long getContentAsLong(int begin, int length) {
        if (!checkBounds(begin, length)) {
            return 0;
        }
        MyByte[] ret = new MyByte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new MyByte(data[begin + i]);
        }
        return NumberConversion.myBytetoLongWithoutSign(ret);
    }

    /**
     * Gets raw byte at address for visualization.
     *
     * @param address the address
     * @return byte at address
     */
    public MyByte getRawByte(int address) {
        if (address < 0 || address >= data.length) {
            return new MyByte(0);
        }
        return new MyByte(data[address]);
    }

    /**
     * Sets memory content from MyByte array.
     *
     * @param begin   start address
     * @param content content to write
     */
    public void setContent(int begin, MyByte[] content) {
        if (!checkBounds(begin, content.length)) {
            return;
        }
        for (int i = 0; i < content.length; i++) {
            int address = begin + i;
            data[address] = (byte) content[i].getContent();
            Machine.getInstance().getEventBus().publish(new MemoryUpdateEvent(address));
        }
    }

    // ========== Private Helpers ==========

    /**
     * Checks bounds and handles error if out of bounds.
     *
     * @return true if within bounds, false otherwise
     */
    private boolean checkBounds(int address, int length) {
        if (address < 0 || address + length > data.length) {
            String message = ERROR_MEMORY_TEXT + address;

            // Emit event for any listeners
            Machine.getInstance().getEventBus().publish(
                    new MemoryAccessErrorEvent(MemoryAccessErrorEvent.Type.OUT_OF_BOUNDS, address, message, true));

            return false;
        }
        return true;
    }

}
