package cli;

import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.Register;
import gui.CONSTANTS;
import simulator.Command;

import java.io.PrintStream;

class QuietMachine implements IMachine {
    private final IMachine inner;
    private final PrintStream out;

    private final boolean printHex;

    QuietMachine(IMachine inner, PrintStream out, boolean printHex) {
        this.inner = inner;
        this.out = out;
        this.printHex = printHex;
    }

    @Override
    public boolean hasHalted() {
        return inner.hasHalted();
    }

    @Override
    public Command executeNext() {
        Command command = inner.executeNext();
        return command;
    }

    public void printRegisterState() {
        String format = printHex ? "R%s: 0x%X" : "R%s: %d";
        for (int i = 0; i < CONSTANTS.NUMBER_OF_REGISTER; i++) {
            Register register = Enviroment.REGISTERS.getRegister(i);
            int regValue = register.getContentAsNumber(4);
            out.println(String.format(format, i, regValue));
        }
    }
}
