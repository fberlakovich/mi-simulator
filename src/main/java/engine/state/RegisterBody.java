package engine.state;

import static engine.MachineConstants.REGISTER_COUNT;
import static engine.MachineConstants.SP_REGISTER;

/**
 * MI machine register file implementation.
 * Contains 16 32-bit registers (R0-R15).
 */
public class RegisterBody implements RegisterFile {

    /** Array of registers */
    private final Register[] registers = new Register[REGISTER_COUNT];

    /**
     * Creates a new register file.
     */
    public RegisterBody() {
        for (int i = 0; i < REGISTER_COUNT; i++) {
            registers[i] = (i == SP_REGISTER) ?
                    new Register(i, true) :
                    new Register(i);
        }
    }

    @Override
    public Register getRegister(int nr) {
        return registers[nr];
    }

    @Override
    public int getRegisterCount() {
        return REGISTER_COUNT;
    }
}
