package engine.parser;

import engine.program.Label;
import engine.program.LabelInUse;
import engine.program.Program;
import engine.Machine;

import engine.scanner.Equal;
import engine.scanner.Scanner;
import engine.scanner.Token;
import engine.commands.*;

import java.util.ArrayList;

import static engine.ErrorMessages.*;

/**
 * Parser for MI assembly language.
 * Orchestrates scanning, operand parsing, and command parsing.
 */
public class Parser {

    private final ParserState state;
    private final Program program;
    private final OperandParser operandParser;
    private final CommandParser commandParser;

    /**
     * Instantiates a new parser.
     *
     * @param machine the machine
     * @param scanner the scanner
     */
    public Parser(Machine machine, Scanner scanner) {
        this.state = new ParserState(machine, scanner);
        this.program = new Program(machine);
        this.operandParser = new OperandParser(state);
        this.commandParser = new CommandParser(state, operandParser, this);
    }

    /**
     * Parses the next command.
     *
     * @return the command
     */
    Command parseNextCommand() {
        state.markTokenPosition();
        int tokenType = state.getCurrentToken().getNr();

        // Try standard commands via command parser
        Command cmd = commandParser.tryParseCommand(tokenType);
        if (cmd != null) {
            return cmd;
        }

        // Try jump commands
        if (commandParser.isJumpCommand(tokenType)) {
            return commandParser.parseJump(tokenType);
        }

        // Handle label
        if (tokenType == Scanner.TOKEN_LABEL) {
            return commandParser.parseLabel(program);
        }

        // Handle special cases
        return handleSpecialTokens(tokenType);
    }

    private Command handleSpecialTokens(int tokenType) {
        switch (tokenType) {
            case Scanner.TOKEN_EQU:
                parseEqual();
                return null;
            case Scanner.TOKEN_NEWLINE:
                state.nextToken();
                return parseNextCommand();
            case Scanner.TOKEN_SEG:
                state.nextToken();
                if (state.getCurrentToken().getNr() == Scanner.TOKEN_NAME) {
                    state.nextToken();
                }
                return parseNextCommand();
            case Scanner.TOKEN_END:
            case Scanner.TOKEN_EOF:
                return null;
            default:
                state.getError().append(state.getCurrentToken().getLine(), ERROR_PARSER_FALSE_SYMBOL);
                return null;
        }
    }

    /**
     * Parses EQU directive.
     */
    private void parseEqual() {
        int lineNum = state.getCurrentToken().getLine();
        ArrayList<Token> toks = new ArrayList<Token>();
        state.nextToken();
        String name = "";
        if (state.getCurrentToken().getNr() == Scanner.TOKEN_NAME) {
            name = state.getCurrentToken().getText();
            state.nextToken();
            if (state.getCurrentToken().getNr() == Scanner.TOKEN_EQUALSIGN) {
                state.nextToken();
                while (state.getCurrentToken().getNr() != Scanner.TOKEN_APOSTROPHE
                        && state.getCurrentToken().getNr() != Scanner.TOKEN_NEWLINE
                        && state.getCurrentToken().getNr() != Scanner.TOKEN_EOF) {
                    toks.add(state.getCurrentToken());
                    state.nextToken();
                }
            }
            state.getScanner().addEqual(new Equal(name, toks));
        } else {
            state.getError().append(lineNum, ERROR_PARSER_FALSE_SYMBOL);
            skipToEndOfLine();
        }
    }

    /**
     * Skips tokens until end of line.
     */
    private void skipToEndOfLine() {
        while (!state.getCurrentToken().equal(new int[]{Scanner.TOKEN_APOSTROPHE, Scanner.TOKEN_NEWLINE,
                Scanner.TOKEN_EOF})) {
            state.nextToken();
        }
    }

    /**
     * Starts parsing the program.
     */
    public void start() {
        state.nextToken();
        while (state.getCurrentToken().getNr() != Scanner.TOKEN_EOF
                && state.getCurrentToken().getNr() != Scanner.TOKEN_ERROR) {
            Command cmd = parseNextCommand();
            if (cmd != null) {
                program.add(cmd);
                state.addToAddress(cmd.encode().length);
            }
            if (state.getCurrentToken().getNr() == Scanner.TOKEN_EOF) {
                return;
            }
            state.nextToken();
        }
    }

    /**
     * Evaluates the parsed program - resolves labels.
     *
     * @return true if successful
     */
    public boolean eval() {
        if (!state.getError().getErrorMessage().equals("")) {
            return false;
        }

        for (Command com : program.getCommands()) {
            ArrayList<LabelInUse> test = com.getLabel();
            for (LabelInUse lab : test) {
                program.addLabelInUse(lab);
            }
        }
        if (!program.validateLabels(state.getError())) {
            return false;
        }
        program.resolveLabels();
        return true;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public ErrorMessage getErrorMeassge() {
        return state.getError();
    }

    /**
     * Gets the parsed program.
     *
     * @return the program
     */
    public Program getProgramm() {
        return program;
    }

    /**
     * Gets the labels from the program.
     *
     * @return list of defined labels with their addresses
     */
    public ArrayList<Label> getLabels() {
        return program.getLabels();
    }
}
