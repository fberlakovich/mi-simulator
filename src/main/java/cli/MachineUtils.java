package cli;

import codegenerator.Program;
import enviroment.Enviroment;
import enviroment.MyByte;
import enviroment.NumberConversion;
import core.Constants;
import parser.Parser;
import scanner.Scanner;
import simulator.Command;

import java.util.ArrayList;

public class MachineUtils {
    public static boolean assembleAndLoad(String programText) {
        Enviroment.init();
        Scanner scanner = new Scanner(false);
        scanner.init(programText);
        Parser parser = new Parser(scanner);
        parser.start();
        if (!parser.eval()) {
            System.err.println(parser.getErrorMeassge().getErrorMessage());
            return false;
        }
        Program program = parser.getProgramm();
        ArrayList<Command> commands = program.getCommands();
        if (commands.isEmpty()) {
            System.err.println(Constants.ASSEMBLE_UNSUCCESSFUL);
            return false;
        }
        System.out.println(Constants.ASSEMBLE_SUCCESSFUL);
        Enviroment.setProgram(program);
        Enviroment.MEMORY.setContent(0, program.getOpCode());
        return true;
    }

    public static void loadState(String state, boolean useHex) {
        String[] components = state.split("[\\n;]");

        int radix = useHex ? 16 : 10;
        for (String component : components) {
            String trimmed = component.trim();
            if (trimmed.isEmpty()) continue;

            String[] split = trimmed.split(":");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid state file entry: " + trimmed);
            }

            String key = split[0].trim();
            String value = split[1].trim();

            if (value.contains("->")) {
                String[] valueParts = value.split("->");
                if (valueParts.length != 2) {
                    throw new IllegalArgumentException("Invalid state file entry: " + trimmed);
                }
                value = valueParts[1].trim();
            }

            if (key.startsWith("R")) {
                int regNum = Integer.parseInt(key.replace("R", ""), radix);
                Enviroment.REGISTERS.getRegister(regNum).setContent(NumberConversion.intToByte(Integer.parseInt(value, radix), 4));
                continue;
            }

            if (key.startsWith("C")) {
                Enviroment.flags.setCarry(Integer.parseInt(value, radix) == 1);
                continue;
            }

            if (key.startsWith("V")) {
                Enviroment.flags.setOverflow(Integer.parseInt(value, radix) == 1);
                continue;
            }

            if (key.startsWith("Z")) {
                Enviroment.flags.setZero(Integer.parseInt(value, radix) == 1);
                continue;
            }

            if (key.startsWith("N")) {
                Enviroment.flags.setNegative(Integer.parseInt(value, radix) == 1);
                continue;
            }

            int address = Integer.parseInt(key, radix);
            int memoryValue = Integer.parseInt(value, radix);
            Enviroment.MEMORY.setContent(address, new MyByte[]{new MyByte(memoryValue)});
        }
    }
}
