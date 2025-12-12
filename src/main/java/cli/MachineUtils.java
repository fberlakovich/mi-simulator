package cli;

import engine.program.Program;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.parser.Parser;
import engine.scanner.Scanner;

import static engine.ErrorMessages.ASSEMBLE_SUCCESSFUL;
import static engine.ErrorMessages.ASSEMBLE_UNSUCCESSFUL;
import static engine.MachineConstants.MEMORY_SIZE;
import static engine.MachineConstants.REGISTER_COUNT;

/**
 * Utility methods for loading and assembling MI programs.
 */
public class MachineUtils {

    /**
     * Assembles and loads an MI program into the simulator environment.
     *
     * @param programText the source code of the MI program
     * @return true if assembly and loading succeeded, false otherwise
     */
    public static boolean assembleAndLoad(String programText) {
        Machine.resetInstance();
        Scanner scanner = new Scanner(false);
        scanner.init(programText);
        Parser parser = new Parser(Machine.getInstance(), scanner);
        parser.start();
        if (!parser.eval()) {
            System.err.println(parser.getErrorMeassge().getErrorMessage());
            return false;
        }
        Program program = parser.getProgramm();
        if (!program.compile()) {
            System.err.println(ASSEMBLE_UNSUCCESSFUL);
            return false;
        }
        return true;
    }

    /**
     * Loads machine state from a state file.
     *
     * State file format:
     * - Lines starting with # are comments
     * - Register values: R0: 42 or R15: 0xFF
     * - Flag values: C: 1, V: 0, Z: 0, N: 1
     * - Memory values: address: value (e.g., 100: 255 or 0x64: 0xFF)
     * - Transition format supported: R0: 0 -> 42 (uses the "after" value)
     * - Lines can be separated by newlines or semicolons
     *
     * @param state   the state file content
     * @param useHex  if true, parse numeric values as hexadecimal
     * @throws IllegalArgumentException if the state file format is invalid
     */
    public static void loadState(String state, boolean useHex) {
        String[] lines = state.split("[\\n;]");
        int radix = useHex ? 16 : 10;
        int lineNum = 0;

        for (String line : lines) {
            lineNum++;
            String trimmed = line.trim();

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            String[] parts = trimmed.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Line " + lineNum + ": Invalid format (expected 'key: value'): " + trimmed);
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            // Handle transition format: "old -> new"
            if (value.contains("->")) {
                String[] valueParts = value.split("->");
                if (valueParts.length != 2) {
                    throw new IllegalArgumentException(
                            "Line " + lineNum + ": Invalid transition format: " + trimmed);
                }
                value = valueParts[1].trim();
            }

            try {
                if (key.matches("R\\d+")) {
                    loadRegister(key, value, radix, lineNum);
                } else if (key.equals("C")) {
                    Machine.getInstance().getFlags().setCarry(parseFlag(value, radix));
                } else if (key.equals("V")) {
                    Machine.getInstance().getFlags().setOverflow(parseFlag(value, radix));
                } else if (key.equals("Z")) {
                    Machine.getInstance().getFlags().setZero(parseFlag(value, radix));
                } else if (key.equals("N")) {
                    Machine.getInstance().getFlags().setNegative(parseFlag(value, radix));
                } else {
                    loadMemory(key, value, radix, lineNum);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Line " + lineNum + ": Invalid number format: " + e.getMessage());
            }
        }
    }

    private static void loadRegister(String key, String value, int radix, int lineNum) {
        int regNum = Integer.parseInt(key.substring(1), 10); // Register numbers always decimal
        if (regNum < 0 || regNum >= REGISTER_COUNT) {
            throw new IllegalArgumentException(
                    "Line " + lineNum + ": Invalid register number: R" + regNum +
                            " (must be 0-" + (REGISTER_COUNT - 1) + ")");
        }
        int regValue = parseNumber(value, radix);
        Machine.getInstance().getRegisters().getRegister(regNum).setContent(
                NumberConversion.intToByte(regValue, 4));
    }

    private static void loadMemory(String key, String value, int radix, int lineNum) {
        int address = parseNumber(key, radix);
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException(
                    "Line " + lineNum + ": Invalid memory address: " + key +
                            " (must be 0-" + (MEMORY_SIZE - 1) + ")");
        }
        int memValue = parseNumber(value, radix);
        if (memValue < -128 || memValue > 255) {
            throw new IllegalArgumentException(
                    "Line " + lineNum + ": Memory value out of byte range: " + value);
        }
        Machine.getInstance().getMemory().setContent(address, new MyByte[]{new MyByte(memValue)});
    }

    private static boolean parseFlag(String value, int radix) {
        int intValue = parseNumber(value, radix);
        if (intValue != 0 && intValue != 1) {
            throw new IllegalArgumentException("Flag value must be 0 or 1, got: " + value);
        }
        return intValue == 1;
    }

    /**
     * Parses a number that may be in decimal or hex format.
     * Supports 0x prefix for hex regardless of radix setting.
     */
    private static int parseNumber(String value, int defaultRadix) {
        String v = value.trim();
        if (v.startsWith("0x") || v.startsWith("0X")) {
            return Integer.parseInt(v.substring(2), 16);
        }
        return Integer.parseInt(v, defaultRadix);
    }
}
