package engine.parser;

import engine.Machine;
import engine.scanner.Scanner;
import engine.scanner.Token;
import engine.commands.*;
import engine.program.Label;
import engine.program.Program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static engine.ErrorMessages.*;

/**
 * Parses individual MI assembly commands.
 * Extracted from Parser to reduce complexity and improve separation of concerns.
 */
public class CommandParser {

    private final ParserState state;
    private final OperandParser operandParser;
    private final Parser parentParser;

    /**
     * Maps token types to command parsing methods.
     */
    private static final Map<Integer, Function<CommandParser, Command>> COMMAND_PARSERS;
    static {
        Map<Integer, Function<CommandParser, Command>> map = new HashMap<>();
        // Arithmetic commands
        map.put(Scanner.TOKEN_ADD, CommandParser::add);
        map.put(Scanner.TOKEN_SUB, CommandParser::sub);
        map.put(Scanner.TOKEN_MULT, CommandParser::mult);
        map.put(Scanner.TOKEN_DIV, CommandParser::div);
        // Move commands
        map.put(Scanner.TOKEN_MOVE, CommandParser::move);
        map.put(Scanner.TOKEN_MOVEA, CommandParser::movea);
        map.put(Scanner.TOKEN_MOVEC, CommandParser::movec);
        map.put(Scanner.TOKEN_MOVEN, CommandParser::moven);
        map.put(Scanner.TOKEN_CLEAR, CommandParser::clear);
        // Logical commands
        map.put(Scanner.TOKEN_OR, CommandParser::or);
        map.put(Scanner.TOKEN_XOR, CommandParser::xor);
        map.put(Scanner.TOKEN_ANDNOT, CommandParser::andnot);
        map.put(Scanner.TOKEN_CMP, CommandParser::cmp);
        // Control flow commands
        map.put(Scanner.TOKEN_CALL, CommandParser::call);
        map.put(Scanner.TOKEN_RET, CommandParser::ret);
        map.put(Scanner.TOKEN_HALT, CommandParser::halt);
        // Stack commands
        map.put(Scanner.TOKEN_PUSHR, CommandParser::pushr);
        map.put(Scanner.TOKEN_POPR, CommandParser::popr);
        // Shift/rotate commands
        map.put(Scanner.TOKEN_SH, CommandParser::sh);
        map.put(Scanner.TOKEN_ROT, CommandParser::rot);
        // Bitfield commands
        map.put(Scanner.TOKEN_EXT, CommandParser::ext);
        map.put(Scanner.TOKEN_EXTS, CommandParser::exts);
        map.put(Scanner.TOKEN_INS, CommandParser::ins);
        map.put(Scanner.TOKEN_FINDS, CommandParser::finds);
        map.put(Scanner.TOKEN_FINDC, CommandParser::findc);
        map.put(Scanner.TOKEN_JBSSI, CommandParser::jbssi);
        map.put(Scanner.TOKEN_JBCCI, CommandParser::jbcci);
        // Other commands
        map.put(Scanner.TOKEN_DD, CommandParser::dd);
        map.put(Scanner.TOKEN_RES, CommandParser::res);
        map.put(Scanner.TOKEN_CONV, CommandParser::conv);
        map.put(Scanner.TOKEN_SBPSW, CommandParser::sbpsw);
        COMMAND_PARSERS = Collections.unmodifiableMap(map);
    }

    /**
     * Maps jump token types to their jump type codes.
     */
    private static final Map<Integer, Integer> JUMP_TYPE_MAP;
    static {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(Scanner.TOKEN_JEQ, 0);
        map.put(Scanner.TOKEN_JNE, 1);
        map.put(Scanner.TOKEN_JGT, 2);
        map.put(Scanner.TOKEN_JGE, 3);
        map.put(Scanner.TOKEN_JLT, 4);
        map.put(Scanner.TOKEN_JLE, 5);
        map.put(Scanner.TOKEN_JC, 6);
        map.put(Scanner.TOKEN_JNC, 7);
        map.put(Scanner.TOKEN_JUMP, 8);
        map.put(Scanner.TOKEN_JV, 9);
        map.put(Scanner.TOKEN_JNV, 10);
        JUMP_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public CommandParser(ParserState state, OperandParser operandParser, Parser parentParser) {
        this.state = state;
        this.operandParser = operandParser;
        this.parentParser = parentParser;
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

    private int beg() {
        return state.getBeg();
    }

    private int end() {
        return state.getEnd();
    }

    // Command dispatch

    /**
     * Tries to parse a command using the dispatch map.
     * @return the parsed command, or null if token is not a command
     */
    public Command tryParseCommand(int tokenType) {
        Function<CommandParser, Command> parser = COMMAND_PARSERS.get(tokenType);
        if (parser != null) {
            return parser.apply(this);
        }
        return null;
    }

    /**
     * Checks if the token type is a jump command.
     */
    public boolean isJumpCommand(int tokenType) {
        return JUMP_TYPE_MAP.containsKey(tokenType);
    }

    /**
     * Parses a jump command.
     */
    public Command parseJump(int tokenType) {
        Integer jumpType = JUMP_TYPE_MAP.get(tokenType);
        if (jumpType != null) {
            return jump(jumpType);
        }
        return null;
    }

    /**
     * Parses a label command.
     */
    public Command parseLabel(Program program) {
        String labelName = token().getText().substring(0, token().getText().length() - 1);
        nextToken();
        Command cmd = parentParser.parseNextCommand();
        if (cmd != null) {
            program.addLabel(new Label(labelName, cmd), error());
        }
        return cmd;
    }

    // Individual command parsing methods

    private Command add() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        switch (ops.size()) {
            case 2:
                return new Add(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        beg(), end(), info.floating);
            case 3:
                return new Add(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end(), info.floating);
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command sub() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        switch (ops.size()) {
            case 2:
                return new Sub(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        beg(), end(), info.floating);
            case 3:
                return new Sub(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end(), info.floating);
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command mult() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        switch (ops.size()) {
            case 2:
                return new Mult(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        beg(), end(), info.floating);
            case 3:
                return new Mult(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end(), info.floating);
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command div() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        switch (ops.size()) {
            case 2:
                return new Div(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        beg(), end(), info.floating);
            case 3:
                return new Div(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end(), info.floating);
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command move() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        if (ops.size() == 2) {
            return new Move(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                    beg(), end(), info.floating);
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command movea() {
        int lineNum = token().getLine();
        int length = 4;
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(length, false);
        if (ops.size() == 2) {
            if (ops.get(0) instanceof RelAddressing || ops.get(0) instanceof IndAddressing ||
                    (ops.get(0) instanceof AbsAddress && ((AbsAddress) ops.get(0)).hasLabel())) {
                return new MoveA(machine(), lineNum, address(), ops.get(0), ops.get(1), beg(), end());
            } else {
                error().append(lineNum, ": MOVEA expects relative address as first operand at line:");
                return null;
            }
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command movec() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(false);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, false);
        if (ops.size() == 2) {
            return new MoveC(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command moven() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        if (ops.size() == 2) {
            return new MoveN(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                    beg(), end(), info.floating);
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command clear() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        if (ops.size() == 1) {
            return new Clear(machine(), lineNum, address(), info.length, ops.get(0), beg(), end(), info.floating);
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command or() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(false);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, false);
        switch (ops.size()) {
            case 2:
                return new Or(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1), beg(), end());
            case 3:
                return new Or(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end());
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command xor() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(false);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, false);
        switch (ops.size()) {
            case 2:
                return new Xor(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1), beg(), end());
            case 3:
                return new Xor(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end());
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command andnot() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(false);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, false);
        switch (ops.size()) {
            case 2:
                return new AndNot(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1), beg(), end());
            case 3:
                return new AndNot(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                        ops.get(2), beg(), end());
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command cmp() {
        int lineNum = token().getLine();
        nextToken();

        OperandParser.DataTypeInfo info = operandParser.parseDataType(true);
        if (info == null) return null;

        ArrayList<Operand> ops = operandParser.operands(info.length, info.floating);
        if (ops.size() == 2) {
            return new Cmp(machine(), lineNum, address(), info.length, ops.get(0), ops.get(1),
                    beg(), end(), info.floating);
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command call() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 1) {
            return new Call(machine(), lineNum, address(), 4, ops.get(0), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command ret() {
        Ret befehl = new Ret(machine(), token().getLine(), address(), beg(), end());
        nextToken();
        return befehl;
    }

    private Command halt() {
        Halt befehl = new Halt(machine(), token().getLine(), address(), beg(), end());
        nextToken();
        return befehl;
    }

    private Command jump(int typ) {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 1) {
            return new Jump(machine(), lineNum, address(), ops.get(0), typ, beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command pushr() {
        Pushr befehl = new Pushr(machine(), token().getLine(), address(), 0, beg(), end());
        nextToken();
        return befehl;
    }

    private Command popr() {
        Popr befehl = new Popr(machine(), token().getLine(), address(), beg(), end());
        nextToken();
        return befehl;
    }

    private Command sh() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 3) {
            return new Sh(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command rot() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 3) {
            return new Rot(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command ext() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 4) {
            return new Ext(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), ops.get(3), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command exts() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 4) {
            return new Exts(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), ops.get(3), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command ins() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 4) {
            return new Ins(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), ops.get(3), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command finds() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        switch (ops.size()) {
            case 2:
                return new Finds(machine(), lineNum, address(), ops.get(0), ops.get(1), null, null, beg(), end());
            case 4:
                return new Finds(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), ops.get(3), beg(), end());
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command findc() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        switch (ops.size()) {
            case 2:
                return new Findc(machine(), lineNum, address(), ops.get(0), ops.get(1), null, null, beg(), end());
            case 4:
                return new Findc(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), ops.get(3), beg(), end());
            default:
                error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
                return null;
        }
    }

    private Command jbssi() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 3) {
            return new Jbssi(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command jbcci() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(4, false);
        if (ops.size() == 3) {
            return new Jbcci(machine(), lineNum, address(), ops.get(0), ops.get(1), ops.get(2), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command conv() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operands(1, false);
        if (ops.size() == 2) {
            return new Conv(machine(), lineNum, address(), ops.get(0), ops.get(1), beg(), end());
        }
        error().append(lineNum, ERROR_PARSER_NUMBER_OF_OPERANDS);
        return null;
    }

    private Command sbpsw() {
        nextToken();
        operandParser.operands(4, false);
        // SBPSW is parsed then continues to next command
        return parentParser.parseNextCommand();
    }

    private Command dd() {
        int lineNum = token().getLine();
        nextToken();
        ArrayList<Operand> ops = operandParser.operandsForDD();
        Operand[] opArray = ops.toArray(new Operand[0]);
        return new DD(machine(), lineNum, address(), opArray, beg(), end());
    }

    private Command res() {
        int lineNum = token().getLine();
        nextToken();
        if (token().getNr() != Scanner.TOKEN_NUMBER) {
            error().append(lineNum, ERROR_PARSER_NONUMBER);
            return null;
        }
        Res befehl = new Res(machine(), lineNum, address(), Integer.parseInt(token().getText()), beg(), end());
        nextToken();
        return befehl;
    }
}
