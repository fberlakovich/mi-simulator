package engine.commands;

import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

/**
 * Operand specification for register addressing mode.
 */
public class RegisterAddressing implements Operand {

    /** The machine this operand operates on */
    private final Machine machine;

    /** Register number */
    int nr;

    /** Access length in bytes */
    int length;

    /**
     * Creates a new register addressing operand.
     *
     * @param machine the machine this operand operates on
     * @param nr     register number (0-15)
     * @param length access length in bytes
     */
    public RegisterAddressing(Machine machine, int nr, int length) {
        this.machine = machine;
        this.nr = nr;
        this.length = length;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    public Operand copy() {
        return new RegisterAddressing(machine, nr, length);
    }

    @Override
    public int getAdress() {
        return NumberConversion.myBytetoIntWithoutSign(
                machine.getRegisters().getRegister(nr).getContent(4));
    }

    @Override
    public MyByte[] getContent() {
        return machine.getRegisters().getRegister(nr).getContent(length);
    }

    @Override
    public byte[] encode() {
        MyByte[] ret = new MyByte[]{new MyByte(5 * 16 + nr)};
        return MyByte.toByteArray(ret);
    }

    @Override
    public void setContent(MyByte[] content, int length) {
        machine.getRegisters().getRegister(nr).setContent(content);
    }

    @Override
    public String toString() {
        return "R" + Integer.toString(nr);
    }
}
