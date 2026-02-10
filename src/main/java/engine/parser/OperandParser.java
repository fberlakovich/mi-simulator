package engine.parser;

import engine.Machine;
import engine.scanner.Scanner;
import engine.scanner.Token;
import engine.commands.*;
import engine.util.NumberConversion;

import java.util.ArrayList;

import static engine.ErrorMessages.*;

/**
 * Parses operands for MI assembly instructions.
 * Handles all addressing modes: register, immediate, indirect, relative, etc.
 */
public class OperandParser {

    private final ParserState state;

    /**
     * Holds parsed data type information (length and floating flag).
     */
    public static class DataTypeInfo {
        public final int length;
        public final boolean floating;

        public DataTypeInfo(int length, boolean floating) {
            this.length = length;
            this.floating = floating;
        }
    }

    /**
     * Result of checking for end of operand list.
     */
    public enum OperandListResult {
        CONTINUE,      // More operands expected (comma found)
        DONE,          // List is complete
        DONE_BRACKET   // List is complete (bracket close for DD)
    }

    /**
     * Creates a new OperandParser.
     */
    public OperandParser(ParserState state) {
        this.state = state;
    }

    // Convenience accessors for shared state
    private Token token() {
        return state.getCurrentToken();
    }

    private void nextToken() {
        state.nextToken();
    }

    private Machine machine() {
        return state.getMachine();
    }

    private ErrorMessage error() {
        return state.getError();
    }

    private int address() {
        return state.getAddress();
    }

    // =====================================================
    // Data Type Parsing
    // =====================================================

    /**
     * Parses a required data type (B/H/W/F/D) from current token.
     * Reports error if not found or if floating types used when not supported.
     */
    public DataTypeInfo parseDataType(boolean supportFloating) {
        int lineNum = token().getLine();
        int length;
        boolean floating = false;

        switch (token().getNr()) {
            case Scanner.TOKEN_B:
                length = 1;
                break;
            case Scanner.TOKEN_H:
                length = 2;
                break;
            case Scanner.TOKEN_W:
                length = 4;
                break;
            case Scanner.TOKEN_F:
                if (!supportFloating) {
                    error().append(lineNum, ERROR_PARSER_BHW);
                    return null;
                }
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                if (!supportFloating) {
                    error().append(lineNum, ERROR_PARSER_BHW);
                    return null;
                }
                length = 8;
                floating = true;
                break;
            default:
                error().append(lineNum, supportFloating ? ERROR_PARSER_BHWFD : ERROR_PARSER_BHW);
                return null;
        }
        nextToken();
        return new DataTypeInfo(length, floating);
    }

    /**
     * Tries to parse an optional data type (B/H/W/F/D).
     * Returns null if current token is not a data type (doesn't report error).
     */
    public DataTypeInfo tryParseDataType() {
        int length;
        boolean floating = false;
        switch (token().getNr()) {
            case Scanner.TOKEN_B: length = 1; break;
            case Scanner.TOKEN_H: length = 2; break;
            case Scanner.TOKEN_W: length = 4; break;
            case Scanner.TOKEN_F: length = 4; floating = true; break;
            case Scanner.TOKEN_D: length = 8; floating = true; break;
            default: return null;
        }
        nextToken();
        return new DataTypeInfo(length, floating);
    }

    // =====================================================
    // Operand List Helpers
    // =====================================================

    /**
     * Handles end-of-operand-list tokens (comma, newline, EOF, etc.).
     */
    public OperandListResult handleOperandListEnd(int lineNum) {
        switch (token().getNr()) {
            case Scanner.TOKEN_COMMA:
                nextToken();
                return OperandListResult.CONTINUE;
            case Scanner.TOKEN_BRACKETCLOSE:
                return OperandListResult.DONE_BRACKET;
            case Scanner.TOKEN_APOSTROPHE:
            case Scanner.TOKEN_NEWLINE:
            case Scanner.TOKEN_EOF:
                return OperandListResult.DONE;
            case Scanner.TOKEN_ERROR:
                error().append(lineNum, token().getText());
                return OperandListResult.DONE;
            default:
                error().append(lineNum, ERROR_PARSER_NO_END_SIGN);
                nextToken();
                return OperandListResult.CONTINUE;
        }
    }

    /**
     * Checks if the current token indicates end of operand.
     */
    private boolean isEndOfOperand() {
        return token().equal(new int[]{Scanner.TOKEN_COMMA, Scanner.TOKEN_NEWLINE,
                Scanner.TOKEN_APOSTROPHE, Scanner.TOKEN_EOF});
    }

    // =====================================================
    // Addressing Mode Helpers
    // =====================================================

    /**
     * Parses index register in the form /Rx/ and returns the index.
     * Expects current token to be SLASH. Returns -1 on error.
     */
    private int parseIndexRegister(int lineNum) {
        nextToken();
        if (token().getNr() != Scanner.TOKEN_REGISTER) {
            error().append(lineNum, ERROR_PARSER_FALSE_SYMBOL);
            return -1;
        }
        int index = Integer.parseInt(token().getText().substring(1));
        nextToken();
        if (token().getNr() != Scanner.TOKEN_SLASH) {
            error().append(lineNum, ERROR_PARSER_FALSE_SYMBOL);
            return -1;
        }
        nextToken();
        return index;
    }

    /**
     * Parses !Rx addressing mode and adds operand to list.
     * Also handles !Rx+, !Rx/Ri/ variants.
     */
    private void parseRelAddressing(ArrayList<Operand> list, int length, int lineNum) {
        int reg = Integer.parseInt(token().getText().substring(1));
        nextToken();

        if (isEndOfOperand()) {
            list.add(new RelAddressing(machine(), 0, reg, length, 0));
        } else if (token().getNr() == Scanner.TOKEN_PLUS) {
            list.add(new CellarAddressing(machine(), reg, length, length, true));
            nextToken();
        } else if (token().getNr() == Scanner.TOKEN_SLASH) {
            int index = parseIndexRegister(lineNum);
            if (index >= 0) {
                list.add(new RelAddressing(machine(), 0, reg, index, length, 0));
            }
        }
    }

    /**
     * Parses !!Rx addressing mode and adds operand to list.
     * Also handles !!Rx/Ri/ variant.
     */
    private void parseDoubleIndirectAddressing(ArrayList<Operand> list, int length, int lineNum) {
        nextToken();
        if (token().getNr() != Scanner.TOKEN_REGISTER) return;

        int reg = Integer.parseInt(token().getText().substring(1));
        nextToken();

        if (isEndOfOperand()) {
            list.add(new IndAddressing(machine(), reg, length, 0));
        } else if (token().getNr() == Scanner.TOKEN_SLASH) {
            int index = parseIndexRegister(lineNum);
            if (index >= 0) {
                list.add(new IndAddressing(machine(), 0, reg, index, length));
            }
        }
    }

    /**
     * Parses !(offset+!Rx) addressing mode and adds operand to list.
     * Also handles !(offset+!Rx)/Ri/ variant.
     */
    private void parseBracketedIndirectAddressing(ArrayList<Operand> list, int length, int lineNum) {
        nextToken();
        if (token().getNr() != Scanner.TOKEN_NUMBER) return;

        int offset = Integer.parseInt(token().getText());
        nextToken();
        if (token().getNr() != Scanner.TOKEN_PLUS) return;

        nextToken();
        if (token().getNr() != Scanner.TOKEN_EXCLAMATIONPOINT) return;

        nextToken();
        if (token().getNr() != Scanner.TOKEN_REGISTER) return;

        int reg = Integer.parseInt(token().getText().substring(1));
        nextToken();
        if (token().getNr() != Scanner.TOKEN_BRACKETCLOSE) return;

        nextToken();
        if (isEndOfOperand()) {
            list.add(new IndAddressing(machine(), offset, reg, length, 0));
        } else if (token().getNr() == Scanner.TOKEN_SLASH) {
            int index = parseIndexRegister(lineNum);
            if (index >= 0) {
                list.add(new IndAddressing(machine(), offset, reg, index, length, 0));
            }
        }
    }

    /**
     * Parses offset+!Rx addressing mode and adds operand to list.
     * Also handles offset+!Rx/Ri/ variant.
     */
    private boolean parseOffsetRelAddressing(ArrayList<Operand> list, int offset, int length, int lineNum) {
        if (token().getNr() != Scanner.TOKEN_PLUS) {
            return false;
        }
        nextToken();
        if (token().getNr() != Scanner.TOKEN_EXCLAMATIONPOINT) {
            return false;
        }
        nextToken();
        if (token().getNr() != Scanner.TOKEN_REGISTER) {
            return false;
        }
        int reg = Integer.parseInt(token().getText().substring(1));
        nextToken();

        if (isEndOfOperand()) {
            list.add(new RelAddressing(machine(), offset, reg, length, 0));
        } else if (token().getNr() == Scanner.TOKEN_SLASH) {
            int index = parseIndexRegister(lineNum);
            if (index >= 0) {
                list.add(new RelAddressing(machine(), offset, reg, index, length, 0));
            }
        }
        return true;
    }

    /**
     * Parses floating point immediate operand.
     */
    private void parseFloatImmediate(ArrayList<Operand> list, int length, int lineNum) {
        try {
            if (length == 4) {
                float f = Float.parseFloat(token().getText());
                list.add(new ImmediateOperand(machine(),
                        NumberConversion.intToByte(Float.floatToIntBits(f), length), length));
            } else if (length == 8) {
                double d = Double.parseDouble(token().getText());
                list.add(new ImmediateOperand(machine(),
                        NumberConversion.longToByte(Double.doubleToLongBits(d), length), length));
            } else {
                error().append(lineNum, ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                return;
            }
            nextToken();
        } catch (NumberFormatException e) {
            error().append(lineNum, length == 4 ? ERROR_PARSER_PARSE_FLOAT : ERROR_PARSER_PARSE_DOUBLE);
        }
    }

    // =====================================================
    // Main Operand Parsing Methods
    // =====================================================

    /**
     * Parses operands until an end-of-command token is found.
     *
     * @param length   Data type length
     * @param floating Whether floating point numbers are allowed
     * @return List of parsed operands
     */
    public ArrayList<Operand> operands(int length, boolean floating) {
        int lineNum = token().getLine();
        ArrayList<Operand> list = new ArrayList<>();
        int reg;
        int offset;
        String label;
        while (true) {
            switch (token().getNr()) {
                case Scanner.TOKEN_NAME:
                    label = token().getText();
                    nextToken();
                    int off = 0;
                    while (token().getNr() == Scanner.TOKEN_NUMBER) {
                        off += Integer.parseInt(
                                token().getText().charAt(0) == '+' ?
                                        token().getText().substring(1) :
                                        token().getText());
                        nextToken();
                    }
                    list.add(new AbsAddress(machine(), label, length, address(), off));
                    break;

                case Scanner.TOKEN_REGISTER:
                    reg = Integer.parseInt(token().getText().substring(1));
                    list.add(new RegisterAddressing(machine(), reg, length));
                    nextToken();
                    break;

                case Scanner.TOKEN_BIN:
                    list.add(new AbsAddress(machine(), Integer.parseInt(
                            token().getText().substring(2, token().getText().length() - 1), 2),
                            4, address()));
                    nextToken();
                    break;

                case Scanner.TOKEN_HEX:
                    list.add(new AbsAddress(machine(), Integer.parseInt(
                            token().getText().substring(2, token().getText().length() - 1), 16),
                            4, address()));
                    nextToken();
                    break;

                case Scanner.TOKEN_EXCLAMATIONPOINT:
                    nextToken();
                    switch (token().getNr()) {
                        case Scanner.TOKEN_REGISTER:
                            parseRelAddressing(list, length, lineNum);
                            break;
                        case Scanner.TOKEN_EXCLAMATIONPOINT:
                            parseDoubleIndirectAddressing(list, length, lineNum);
                            break;
                        case Scanner.TOKEN_BRACKETOPEN:
                            parseBracketedIndirectAddressing(list, length, lineNum);
                            break;
                    }
                    break;

                case Scanner.TOKEN_NUMBER:
                    offset = Integer.parseInt(token().getText());
                    nextToken();
                    if (isEndOfOperand()) {
                        list.add(new AbsAddress(machine(), offset, length, address()));
                    } else {
                        parseOffsetRelAddressing(list, offset, length, lineNum);
                    }
                    break;

                case Scanner.TOKEN_I:
                    nextToken();
                    parseImmediateOperand(list, length, floating, lineNum);
                    break;

                case Scanner.TOKEN_MINUS:
                    nextToken();
                    if (token().getNr() == Scanner.TOKEN_EXCLAMATIONPOINT) {
                        nextToken();
                        if (token().getNr() == Scanner.TOKEN_REGISTER) {
                            list.add(new CellarAddressing(machine(),
                                    Integer.parseInt(token().getText().substring(1)),
                                    length, -length, false));
                            nextToken();
                        } else {
                            error().append(lineNum, ERROR_PARSER_NO_REGISTER);
                        }
                    } else {
                        error().append(lineNum, ERROR_PARSER_FALSE_SYMBOL);
                    }
                    break;

                default:
                    break;
            }

            OperandListResult result = handleOperandListEnd(lineNum);
            if (result != OperandListResult.CONTINUE) {
                return list;
            }
        }
    }

    /**
     * Parses immediate operand (I prefix already consumed).
     */
    private void parseImmediateOperand(ArrayList<Operand> list, int length, boolean floating, int lineNum) {
        Long value;
        switch (token().getNr()) {
            case Scanner.TOKEN_BIN:
                value = Long.valueOf(token().getText().substring(2, token().getText().length() - 1), 2);
                if (!NumberConversion.valid_number(value, length))
                    error().append(lineNum, ERROR_PARSER_NO_VALIDNUMBER);
                list.add(new ImmediateOperand(machine(), NumberConversion.binToByte(
                        token().getText().substring(2, token().getText().length() - 1), length), length));
                nextToken();
                break;

            case Scanner.TOKEN_HEX:
                value = Long.valueOf(token().getText().substring(2, token().getText().length() - 1), 16);
                if (!NumberConversion.valid_number(value, length))
                    error().append(lineNum, ERROR_PARSER_NO_VALIDNUMBER);
                list.add(new ImmediateOperand(machine(), NumberConversion.hexToByte(
                        token().getText().substring(2, token().getText().length() - 1), length), length));
                nextToken();
                break;

            case Scanner.TOKEN_NUMBER:
                if (!floating) {
                    value = Long.valueOf(token().getText());
                    if (!NumberConversion.valid_number(value, length))
                        error().append(lineNum, ERROR_PARSER_NO_VALIDNUMBER);
                    list.add(new ImmediateOperand(machine(),
                            NumberConversion.intToByte(Integer.parseInt(token().getText()), length), length));
                    nextToken();
                } else {
                    parseFloatImmediate(list, length, lineNum);
                }
                break;

            case Scanner.TOKEN_FLOAT:
                if (!floating) {
                    error().append(lineNum, ERROR_PARSER_NO_FLOAT);
                    nextToken();
                } else {
                    parseFloatImmediate(list, length, lineNum);
                }
                break;

            default:
                error().append(lineNum, ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                break;
        }
    }

    /**
     * Parses operands for DD directive.
     */
    public ArrayList<Operand> operandsForDD() {
        int lineNum = token().getLine();
        int length = 0;
        int newlength;
        int num;
        boolean floating = false;
        ArrayList<Operand> list = new ArrayList<>();
        String label;

        while (true) {
            // Try to parse optional data type prefix (B/H/W/F/D)
            DataTypeInfo typeInfo = tryParseDataType();
            if (typeInfo != null) {
                length = typeInfo.length;
                floating = typeInfo.floating;
            }

            switch (token().getNr()) {
                case Scanner.TOKEN_NAME:
                    label = token().getText();
                    nextToken();
                    int off = 0;
                    while (token().getNr() == Scanner.TOKEN_NUMBER) {
                        off += Integer.parseInt(
                                token().getText().charAt(0) == '+' ?
                                        token().getText().substring(1) :
                                        token().getText());
                        nextToken();
                    }
                    list.add(new AbsAddress(machine(), label, 4, address(), off));
                    break;

                case Scanner.TOKEN_STRING:
                    boolean found = false;
                    label = token().getText().substring(1, token().getText().length() - 1);
                    for (byte b : label.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                        if (b != 39 || found) {
                            list.add(new ImmediateOperand(machine(),
                                    NumberConversion.intToByte(b, 1), 1));
                            found = false;
                        } else {
                            found = true;
                        }
                    }
                    nextToken();
                    break;

                case Scanner.TOKEN_BIN:
                    num = Integer.parseInt(
                            token().getText().substring(2, token().getText().length() - 1), 2);
                    newlength = length == 0 ? getLengthForOperand(num) : length;
                    list.add(new ImmediateOperand(machine(),
                            NumberConversion.intToByte(num, newlength), newlength));
                    nextToken();
                    break;

                case Scanner.TOKEN_HEX:
                    num = Integer.parseInt(
                            token().getText().substring(2, token().getText().length() - 1), 16);
                    newlength = length == 0 ? getLengthForOperand(num) : length;
                    list.add(new ImmediateOperand(machine(),
                            NumberConversion.intToByte(num, newlength), newlength));
                    nextToken();
                    break;

                case Scanner.TOKEN_FLOAT:
                    parseFloatImmediate(list, length, lineNum);
                    break;

                case Scanner.TOKEN_NUMBER:
                    if (!floating) {
                        num = Integer.parseInt(token().getText());
                        newlength = length == 0 ? getLengthForOperand(num) : length;
                        list.add(new ImmediateOperand(machine(),
                                NumberConversion.intToByte(num, newlength), newlength));
                        nextToken();
                    } else {
                        parseFloatImmediate(list, length, lineNum);
                    }
                    break;

                case Scanner.TOKEN_BRACKETOPEN:
                    nextToken();
                    ArrayList<Operand> newList = operandsForDD();
                    nextToken();
                    if (token().getNr() == Scanner.TOKEN_STAR) {
                        nextToken();
                        if (token().getNr() == Scanner.TOKEN_NUMBER) {
                            num = Integer.parseInt(token().getText());
                            for (int i = 0; i < num; i++) {
                                for (Operand x : newList) {
                                    list.add(x.copy());
                                }
                            }
                        } else {
                            error().append(token().getLine(), ERROR_PARSER_NONUMBER);
                        }
                    } else {
                        error().append(token().getLine(), ERROR_PARSER_NO_STAR);
                    }
                    nextToken();
                    break;
            }

            OperandListResult result = handleOperandListEnd(lineNum);
            if (result == OperandListResult.DONE || result == OperandListResult.DONE_BRACKET) {
                return list;
            }
        }
    }

    /**
     * Returns appropriate byte length for an operand value.
     */
    private int getLengthForOperand(int num) {
        if (num >= -128 && num <= 255) return 1;
        if (num >= -32768 && num <= 65535) return 2;
        return 4;
    }
}
