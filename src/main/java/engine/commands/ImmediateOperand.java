package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

/**
 * Operand specification for immediate (literal) values.
 */
public class ImmediateOperand implements Operand {

    /** The machine this operand operates on */
    private final Machine machine;

    /** Internal storage for the operand value (8 bytes max) */
    MyByte[] content = new MyByte[8];

    /** Operand length in bytes */
    int length;

    /**
     * Creates a new immediate operand.
     *
     * @param machine the machine this operand operates on
     * @param content the value as a byte array
     * @param length  the operand length in bytes
     */
    public ImmediateOperand(Machine machine, MyByte[] content, int length) {
        this.machine = machine;
        for (int i = (8 - length); i < 8; i++) {
            this.content[i] = content[i - (8 - length)];
        }
        this.length = length;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    public Operand copy() {
        return new ImmediateOperand(machine, getContent(), length);
    }

    @Override
    public int getAdress() {
        return 0;
    }

    @Override
    public MyByte[] getContent() {
        switch (length) {
            case 1:
                return new MyByte[]{content[7]};
            case 2:
                return new MyByte[]{content[6], content[7]};
            case 4:
                return new MyByte[]{content[4], content[5], content[6], content[7]};
            case 8:
                return content;
        }
        return content;
    }

    @Override
    public byte[] encode() {
        int merke = NumberConversion.myBytetoIntWithoutSign(getContent());
        if (merke >= 0 && merke < 64 && length != 8) {
            MyByte[] ret = new MyByte[]{content[7]};
            return MyByte.toByteArray(ret);
        }

        MyByte[] ret = new MyByte[length + 1];
        ret[0] = new MyByte("8F");
        int x = 1;
        for (int i = (8 - length); i < 8; i++) {
            ret[x++] = content[i];
        }
        return MyByte.toByteArray(ret);
    }

    /**
     * Gets the raw opcode bytes without the prefix.
     *
     * @return the opcode bytes
     */
    public MyByte[] getOpCode2() {
        switch (length) {
            case 1:
                return new MyByte[]{content[7]};
            case 2:
                return new MyByte[]{content[6], content[7]};
            case 4:
                return new MyByte[]{content[4], content[5], content[6], content[7]};
            case 8:
                return content;
        }
        return content;
    }

    @Override
    public void setContent(MyByte[] content, int length) {
        // Immediate operands are read-only
    }

    @Override
    public String toString() {
        return "I H'" + NumberConversion.myBytetoHex(getContent()) + "'";
    }
}
