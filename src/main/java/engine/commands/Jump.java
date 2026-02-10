package engine.commands;

import engine.program.LabelInUse;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;

import static engine.MachineConstants.PC_REGISTER;

import java.util.ArrayList;
import java.util.Map;

/**
 * Jump instruction implementation.
 * Supports conditional and unconditional jumps.
 */
public class Jump extends Command {

    /** Target operand */
    Operand op1;

    /** Jump type (0=JEQ, 1=JNE, 2=JGT, etc.) */
    int typ;

    /** Opcode to jump type mapping (MI Manual page 92) */
    private static final Map<Integer, Integer> OPCODE_TO_TYPE = Map.ofEntries(
        Map.entry(0xE9, 0),  // JEQ
        Map.entry(0xEA, 1),  // JNE
        Map.entry(0xEB, 2),  // JGT
        Map.entry(0xEC, 3),  // JGE
        Map.entry(0xED, 4),  // JLT
        Map.entry(0xEE, 5),  // JLE
        Map.entry(0xEF, 6),  // JC
        Map.entry(0xF0, 7),  // JNC
        Map.entry(0xF1, 8),  // JUMP
        Map.entry(0x97, 9),  // JV
        Map.entry(0x98, 10)  // JNV
    );

    /**
     * Creates a new jump instruction.
     *
     * @param machine the machine this command operates on
     * @param line   source line number
     * @param adress instruction address
     * @param op1    target operand
     * @param typ    jump type (0=JEQ, 1=JNE, 2=JGT, 3=JGE, 4=JLT, 5=JLE, 6=JC, 7=JNC, 8=JUMP, 9=JV, 10=JNV)
     * @param beg    start position in source
     * @param end    end position in source
     */
    public Jump(Machine machine, int line, int adress, Operand op1, int typ, int beg, int end) {
        super(machine, line, adress, beg, end);
        this.op1 = op1;
        this.typ = typ;
        setAdress(adress);

    }

    /**
     * Decodes a Jump instruction from memory.
     * MI Manual page 90, 92: Jump opcodes 0x97-0x98 (JV/JNV) and 0xE9-0xF1.
     */
    public static Command decode(Machine machine, int pc, Opcode opcode) {
        Operand target = Operand.decode(machine, opcode.length);
        int jumpType = OPCODE_TO_TYPE.get(opcode.code);
        return new Jump(machine, 0, pc, target, jumpType, 0, 0);
    }

    @Override
    public ArrayList<LabelInUse> getLabel() {
        ArrayList<LabelInUse> ret = new ArrayList<>();
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ret.add(new LabelInUse(this, ((AbsAddress) op1).getLabel(),
                    (AbsAddress) op1));
        }
        return ret;
    }

    @Override
    public byte[] encode() {
        MyByte opcode = new MyByte((typ > 8 ? 142 : 233) + typ);

        int x = 1;
        byte[] opc1 = op1.encode();

        MyByte[] ret = new MyByte[opc1.length + 1];
        ret[0] = opcode;
        for (int i = 0; i < opc1.length; i++) {
            ret[x + i] = new MyByte(opc1[i]);
        }
        return MyByte.toByteArray(ret);

    }

    @Override
    public boolean hasLabel() {
        return ((op1 instanceof AbsAddress) && (((AbsAddress) op1).hasLabel()));
    }

    @Override
    public synchronized void run() {
        boolean jump = false;
        switch (typ) {
            case 0: // JEQ
                if (machine.getFlags().isZero()) {
                    jump = true;
                }
                break;
            case 1: // JNE
                if (!machine.getFlags().isZero()) {
                    jump = true;
                }
                break;
            case 2: // JGT
                if (!machine.getFlags().isZero() && !machine.getFlags().isNegative()) {
                    jump = true;
                }
                break;
            case 3: // JGE
                if (machine.getFlags().isZero() || !machine.getFlags().isNegative()) {
                    jump = true;
                }
                break;
            case 4: // JLT
                if (machine.getFlags().isNegative()) {
                    jump = true;
                }
                break;
            case 5: // JLE
                if (machine.getFlags().isZero() || machine.getFlags().isNegative()) {
                    jump = true;
                }
                break;
            case 6: // JC
                if (machine.getFlags().isCarry()) {
                    jump = true;
                }
                break;
            case 7: // JNC
                if (!machine.getFlags().isCarry()) {
                    jump = true;
                }
                break;
            case 8: // JUMP
                jump = true;
                break;
            case 9: // JV
                jump = machine.getFlags().isOverflow();
                break;
            case 10: // JNV
                jump = !machine.getFlags().isOverflow();
                break;
            default:
                throw new IllegalStateException("Unknown jump type: " + typ);
        }

        int target = 0;
        if (op1 instanceof AbsAddress) {
            target = ((AbsAddress) op1).getAdress();
        } else if (op1 instanceof RelAddressing) {
            target = op1.getAdress();
        } else {
            target = NumberConversion.myBytetoIntWithoutSign(op1.getContent());
        }

        if (jump) {
            machine.getRegisters().getRegister(PC_REGISTER)
                    .setContent(NumberConversion.intToByte(target, 4));
        } else {
            super.run();
        }
    }

    @Override
    public void setAdress(int adress) {
        this.adress = adress;
        if (op1 instanceof AbsAddress && ((AbsAddress) op1).hasLabel()) {
            ((AbsAddress) op1).setOrt(adress + 1);
        }
    }

    @Override
    public String toString() {
        String[] suffix = new String[]{"EQ", "NE", "GT", "GE", "LT", "LE", "C", "NC",
                "UMP", "V", "NV"};

        return "J" + suffix[typ] + " " + op1.toString();
    }

}
