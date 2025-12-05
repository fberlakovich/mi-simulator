package cli;

import engine.Machine;
import engine.ProgramRunner;
import engine.state.Register;
import static engine.MachineConstants.REGISTER_COUNT;
import engine.commands.Command;

import java.io.PrintStream;

/**
 * Quiet execution wrapper - runs program without per-instruction output,
 * provides method to print final register state.
 * Works directly with ProgramRunner.
 */
class QuietMachine {
    private final ProgramRunner runner;
    private final PrintStream out;
    private final boolean printHex;
    private boolean halted;

    QuietMachine(ProgramRunner runner, PrintStream out, boolean printHex) {
        this.runner = runner;
        this.out = out;
        this.printHex = printHex;
    }

    public boolean hasHalted() {
        return halted;
    }

    public Command executeNext() {
        if (halted) {
            return null;
        }
        boolean hasMore = runner.step();
        Command executed = runner.getLastExecuted();
        if (!hasMore) {
            halted = true;
        }
        return executed;
    }

    public void printRegisterState() {
        String format = printHex ? "R%s: 0x%X" : "R%s: %d";
        for (int i = 0; i < REGISTER_COUNT; i++) {
            Register register = Machine.getInstance().getRegisters().getRegister(i);
            int regValue = register.getContentAsNumber(4);
            out.println(String.format(format, i, regValue));
        }
    }
}