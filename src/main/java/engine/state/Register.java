package engine.state;

import static engine.MachineConstants.REGISTER_COUNT;

import engine.Machine;
import engine.events.RegisterChangeEvent;
import engine.util.NumberConversion;

/**
 * Represents a single MI machine register.
 */
public class Register {

    /** Register content as 32-bit integer */
    private int content;

    /** Is this the stack pointer register? */
    private boolean isStack = false;

    /** Register number (0-15) */
    private int nr;

    /**
     * Creates a new register.
     *
     * @param nr register number
     */
    public Register(int nr) {
        content = 0;
        this.nr = nr;
    }

    /**
     * Creates a new register with stack flag.
     *
     * @param nr      register number
     * @param isStack true if this is the stack pointer register
     */
    public Register(int nr, boolean isStack) {
        this(nr);
        this.isStack = isStack;
        if (isStack) {
            // Initialize stack pointer to top of memory (MEMORY_SIZE)
            content = engine.MachineConstants.MEMORY_SIZE;
        }
    }

    /**
     * Gets the register content as a byte array.
     *
     * @param length access length in bytes (1, 2, 4, or 8)
     * @return register content as byte array
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
                // 64-bit access spans this register and the next
                ret = new MyByte[8];
                ret[0] = new MyByte((byte)((this.content & 0xff000000) >> 24));
                ret[1] = new MyByte((byte)((this.content & 0x00ff0000) >> 16));
                ret[2] = new MyByte((byte)((this.content & 0x0000ff00) >> 8));
                ret[3] = new MyByte((byte)((this.content & 0x000000ff) >> 0));
                Register wrap = Machine.getInstance().getRegisters().getRegister(
                        (nr + 1) % REGISTER_COUNT);
                MyByte[] next = wrap.getContent(4);
                ret[4] = next[0];
                ret[5] = next[1];
                ret[6] = next[2];
                ret[7] = next[3];
                return ret;
            default:
                throw new engine.InternalError("Invalid register access length: " + length);
        }
    }

    /**
     * Gets the register content as an integer.
     *
     * @param length access length in bytes
     * @return register content as integer
     */
    public int getContentAsNumber(int length) {
        assert length > 0 && length <= 4;
        return this.content;
    }

    /**
     * Sets the register content directly as an integer.
     *
     * @param value the value to set
     */
    public void setContentAsNumber(int value) {
        this.content = value;
    }

    /**
     * Sets the register content from a byte array.
     *
     * @param data the byte data to set
     */
    public void setContent(MyByte[] data) {
        int oldContent = this.content;
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
                // 64-bit write spans this register and the next
                content  = data[0].getContent() << 24;
                content |= data[1].getContent() << 16;
                content |= data[2].getContent() << 8;
                content |= data[3].getContent() << 0;
                Register wrap =
                    Machine.getInstance().getRegisters().getRegister((nr + 1) % REGISTER_COUNT);
                wrap.setContent(new MyByte[]{data[4], data[5], data[6], data[7]});
                break;
        }

        if (isStack && Machine.getInstance().getStackBegin() == 0) {
            Machine.getInstance().setStackBegin(NumberConversion.myBytetoIntWithSign(data));
        }

        Machine.getInstance().getEventBus().publish(new RegisterChangeEvent(nr, oldContent, content));
    }
}
