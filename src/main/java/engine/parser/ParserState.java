package engine.parser;

import engine.Machine;
import engine.scanner.Scanner;
import engine.scanner.Token;

/**
 * Shared state between Parser, CommandParser, and OperandParser.
 * Eliminates manual state synchronization between parser components.
 */
public class ParserState {

    private final Machine machine;
    private final Scanner scanner;
    private final ErrorMessage error;

    private Token currentToken;
    private int address;
    private int beg;
    private int end;

    public ParserState(Machine machine, Scanner scanner) {
        this.machine = machine;
        this.scanner = scanner;
        this.error = new ErrorMessage();
        this.address = 0;
    }

    public Machine getMachine() {
        return machine;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public ErrorMessage getError() {
        return error;
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(Token token) {
        this.currentToken = token;
    }

    public Token nextToken() {
        currentToken = scanner.getNextSymbol();
        return currentToken;
    }

    public int getAddress() {
        return address;
    }

    public void addToAddress(int delta) {
        this.address += delta;
    }

    public int getBeg() {
        return beg;
    }

    public int getEnd() {
        return end;
    }

    /**
     * Updates beg/end from current token position.
     * Called at start of parsing each command.
     */
    public void markTokenPosition() {
        this.beg = currentToken.getBeg();
        this.end = currentToken.getEnd();
    }
}
