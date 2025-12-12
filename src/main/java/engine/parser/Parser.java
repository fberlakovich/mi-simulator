package engine.parser;

import engine.program.Label;
import engine.program.LabelInUse;
import engine.program.Program;
import engine.Machine;
import engine.util.NumberConversion;
import static engine.ErrorMessages.*;

import engine.scanner.Equal;
import engine.scanner.Scanner;
import engine.scanner.Token;
import engine.commands.*;

import java.util.ArrayList;

/**
 * parser-Klasse für den MI-Simulator
 */
public class Parser {

    /**
     * The machine.
     */
    private Machine machine;

    /**
     * The scanner.
     */
    private Scanner scanner;

    /**
     * The ret.
     */
    private Token ret;

    /**
     * The error.
     */
    private ErrorMessage error = new ErrorMessage();

    /**
     * The adress.
     */
    private int adress = 0;

    /**
     * The program.
     */
    private Program program;


    /**
     * The beg.
     */
    private int beg;

    /**
     * The end.
     */
    private int end;

    /**
     * Instantiates a new parser.
     *
     * @param machine the machine
     * @param scanner the scanner
     */
    public Parser(Machine machine, Scanner scanner) {
        this.machine = machine;
        this.scanner = scanner;
        this.program = new Program(machine);
    }

    /**
     * Adds the.
     *
     * @return the command
     */
    private Command add() {
        int zeile = ret.getLine();
        Add befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Add(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            case 3:
                befehl = new Add(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Andnot.
     *
     * @return the command
     */
    private Command andnot() {
        int zeile = ret.getLine();
        AndNot befehl = null;
        int length = 0;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
            case Scanner.TOKEN_B:
                length = 1;
                break;
            case Scanner.TOKEN_H:
                length = 2;
                break;
            case Scanner.TOKEN_W:
                length = 4;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHW);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 2:
                befehl = new AndNot(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end);
                break;
            case 3:
                befehl = new AndNot(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Call.
     *
     * @return the command
     */
    private Command call() {
        int zeile = ret.getLine();
        Call befehl = null;
        int length = 4;
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 1:
                befehl = new Call(machine, zeile, adress, length, operands.get(0), beg, end);
                break;

            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Clear.
     *
     * @return the command
     */
    private Command clear() {
        int zeile = ret.getLine();
        Clear befehl = null;
        int length = 0;
        boolean floating = true;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 1:
                befehl = new Clear(machine, zeile, adress, length, operands.get(0), beg, end,
                        floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Cmp.
     *
     * @return the command
     */
    private Command cmp() {
        int zeile = ret.getLine();
        Cmp befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Cmp(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Dd.
     *
     * @return the command
     */
    private Command dd() {
        int zeile = ret.getLine();
        DD befehl = null;
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operandsForDD();
        Operand[] op2 = new Operand[operands.size()];
        for (int i = 0; i < operands.size(); i++) {
            op2[i] = operands.get(i);
        }
        befehl = new DD(machine, zeile, adress, op2, beg, end);

        return befehl;
    }

    /**
     * Div.
     *
     * @return the command
     */
    private Command div() {
        int zeile = ret.getLine();
        Div befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Div(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            case 3:
                befehl = new Div(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * Equal.
     */
    private void equal() {
        int zeile = ret.getLine();
        ArrayList<Token> toks = new ArrayList<Token>();
        ret = scanner.getNextSymbol();
        String name = "";
        if (ret.getNr() == Scanner.TOKEN_NAME) {
            name = ret.getText();
            ret = scanner.getNextSymbol();
            if (ret.getNr() == Scanner.TOKEN_EQUALSIGN) {
                ret = scanner.getNextSymbol();
                while (ret.getNr() != Scanner.TOKEN_APOSTROPHE
                        && ret.getNr() != Scanner.TOKEN_NEWLINE
                        && ret.getNr() != Scanner.TOKEN_EOF) {
                    toks.add(ret);
                    ret = scanner.getNextSymbol();
                }
            }
            scanner.addEqual(new Equal(name, toks));
        } else {
            error.append(zeile, ERROR_PARSER_FALSE_SYMBOL);
            nextCommand();
        }
    }

    /**
     * Eval.
     *
     * @return true, if successful
     */
    public boolean eval() {

        if (!error.getErrorMessage().equals("")) {
            return false;
        }

        for (Command com : program.getCommands()) {
            ArrayList<LabelInUse> test = com.getLabel();
            for (LabelInUse lab : test) {
                program.addLabelInUse(lab);
            }
        }
        if (!program.validateLabels(error)) {
            return false;
        }
        program.resolveLabels();
        return true;
    }

    /**
     * Gets the error meassge.
     *
     * @return the error meassge
     */
    public ErrorMessage getErrorMeassge() {
        return error;
    }

    private int getLengthForOperand(int zahl) {
        if (zahl >= Byte.MIN_VALUE && zahl <= Byte.MAX_VALUE) {
            return 1;
        }
        if (zahl >= Short.MIN_VALUE && zahl <= Short.MAX_VALUE) {
            return 2;
        }
        if (zahl >= Integer.MIN_VALUE && zahl <= Integer.MAX_VALUE) {
            return 4;
        }
        return 4;
    }

    /**
     * Gets the programm.
     *
     * @return the programm
     */
    public Program getProgramm() {
        return program;
    }

    /**
     * Halt.
     *
     * @return the command
     */
    private Command halt() {
        Halt befehl = new Halt(machine, ret.getLine(), adress, beg, end);
        ret = scanner.getNextSymbol();
        return befehl;
    }

    /**
     * Jump.
     *
     * @param typ the typ
     * @return the command
     */
    private Command jump(int typ) {
        int zeile = ret.getLine();
        Jump befehl = null;
        int length = 4;
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 1:
                befehl = new Jump(machine, zeile, adress, operands.get(0), typ, beg, end);
                break;

            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Label.
     *
     * @return the command
     */
    private Command label() {
        String label = ret.getText().substring(0, ret.getText().length() - 1);
        ret = scanner.getNextSymbol();
        Command befehl = parseNextCommand();
        if (befehl != null) {
            program.addLabel(new Label(label, befehl), error);
        }
        return befehl;

    }

    /**
     * Move.
     *
     * @return the command
     */
    private Command move() {
        int zeile = ret.getLine();
        Move befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Move(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Movea.
     *
     * @return the command
     */
    private Command movea() {
        int zeile = ret.getLine();
        MoveA befehl = null;
        int length = 4;
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 2:
                if (operands.get(0) instanceof RelAddressing || operands.get(
                        0) instanceof IndAddressing || (
                        operands.get(0) instanceof AbsAddress && ((AbsAddress) operands.get(
                                0)).hasLabel())) {
                    befehl = new MoveA(machine, zeile, adress, operands.get(0), operands.get(1),
                            beg, end);
                } else {
                    error.append(zeile,
                            ": MOVEA erwartet relative Adresse als ersten Operanden Zeile: ");
                }
                break;

            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Movec.
     *
     * @return the command
     */
    private Command movec() {
        int zeile = ret.getLine();
        MoveC befehl = null;
        int length = 0;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
            case Scanner.TOKEN_B:
                length = 1;
                break;
            case Scanner.TOKEN_H:
                length = 2;
                break;
            case Scanner.TOKEN_W:
                length = 4;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHW);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 2:
                befehl = new MoveC(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end);
                break;

            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Moven.
     *
     * @return the command
     */
    private Command moven() {
        int zeile = ret.getLine();
        MoveN befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new MoveN(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Mult.
     *
     * @return the command
     */
    private Command mult() {
        int zeile = ret.getLine();
        Mult befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Mult(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            case 3:
                befehl = new Mult(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * Diese Methode parsed die Operanden solange, bis ein EndOfCommand-Zeichen
     * erkannt wird
     *
     * @param length   Typangabe des Befehls
     * @param floating Gleitpunktzahlenbefehl
     * @return Array der Operanden
     */
    private ArrayList<Operand> operands(int length, boolean floating) {
        int zeile = ret.getLine();
        ArrayList<Operand> list = new ArrayList<Operand>();
        int reg = 0;
        int index = 0;
        int offset = 0;
        String label = "";
        while (true) {
            switch (ret.getNr()) {
                case Scanner.TOKEN_NAME:
                    label = ret.getText();

                    ret = scanner.getNextSymbol();
                    int off = 0;
                    while (ret.getNr() == Scanner.TOKEN_NUMBER) {
                        off += Integer.parseInt(
                                ret.getText().substring(0, 1).equals("+") ?
                                        ret.getText()
                                                .substring(1, ret.getText().length()) :
                                        ret.getText());
                        ret = scanner.getNextSymbol();
                    }
                    list.add(new AbsAddress(machine, label, length, adress, off));
                    break;
                case Scanner.TOKEN_REGISTER:
                    reg = Integer.parseInt(
                            ret.getText().substring(1, ret.getText().length()));
                    list.add(new RegisterAddressing(machine, reg, length));
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_BIN:
                    list.add(new AbsAddress(machine, Integer.parseInt(
                            ret.getText().substring(2, ret.getText().length() - 1), 2),
                            4, adress));

                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_HEX:
                    list.add(new AbsAddress(machine, Integer.parseInt(
                            ret.getText().substring(2, ret.getText().length() - 1), 16),
                            4, adress));
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_EXCLAMATIONPOINT:
                    ret = scanner.getNextSymbol();

                    switch (ret.getNr()) {
                        case Scanner.TOKEN_REGISTER:
                            reg = Integer.parseInt(ret.getText().substring(1));
                            ret = scanner.getNextSymbol();
                            if (ret.equal(new int[]{Scanner.TOKEN_COMMA,
                                    Scanner.TOKEN_NEWLINE,
                                    Scanner.TOKEN_APOSTROPHE,
                                    Scanner.TOKEN_EOF})) {
                                list.add(new RelAddressing(machine, 0, reg, length, 0));
                            } else {
                                if (ret.getNr() == Scanner.TOKEN_PLUS) {
                                    list.add(new CellarAddressing(machine, reg, length,
                                            length,
                                            true));
                                    ret = scanner.getNextSymbol();
                                } else {
                                    if (ret.getNr() == Scanner.TOKEN_SLASH) {
                                        ret = scanner.getNextSymbol();
                                        if (ret.getNr()
                                                == Scanner.TOKEN_REGISTER) {
                                            index = Integer.parseInt(
                                                    ret.getText()
                                                            .substring(1,
                                                                    ret.getText()
                                                                            .length()));
                                            ret = scanner.getNextSymbol();
                                            if (ret.getNr()
                                                    == Scanner.TOKEN_SLASH) {
                                                list.add(
                                                        new RelAddressing(
                                                                machine,
                                                                0,
                                                                reg,
                                                                index,
                                                                length,
                                                                0));
                                                ret = scanner.getNextSymbol();
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case Scanner.TOKEN_EXCLAMATIONPOINT:
                            ret = scanner.getNextSymbol();
                            if (ret.getNr() == Scanner.TOKEN_REGISTER) {
                                reg = Integer.parseInt(ret.getText().substring(1,
                                        ret.getText()
                                                .length()));
                                ret = scanner.getNextSymbol();
                                if (ret.equal(new int[]{Scanner.TOKEN_COMMA,
                                        Scanner.TOKEN_NEWLINE,
                                        Scanner.TOKEN_APOSTROPHE,
                                        Scanner.TOKEN_EOF})) {
                                    list.add(new IndAddressing(machine, reg, length, 0));
                                } else {
                                    if (ret.getNr() == Scanner.TOKEN_SLASH) {
                                        ret = scanner.getNextSymbol();
                                        if (ret.getNr()
                                                == Scanner.TOKEN_REGISTER) {
                                            index = Integer.parseInt(
                                                    ret.getText()
                                                            .substring(1,
                                                                    ret.getText()
                                                                            .length()));
                                            ret = scanner.getNextSymbol();
                                            if (ret.getNr()
                                                    == Scanner.TOKEN_SLASH) {
                                                list.add(
                                                        new IndAddressing(
                                                                machine,
                                                                0,
                                                                reg,
                                                                index,
                                                                length));
                                                ret = scanner.getNextSymbol();

                                            } else {
                                                error.append(zeile,
                                                        ERROR_PARSER_FALSE_SYMBOL);
                                            }
                                        } else {
                                            error.append(zeile,
                                                    ERROR_PARSER_FALSE_SYMBOL);
                                        }
                                    }
                                }
                            }

                            break;

                        case Scanner.TOKEN_BRACKETOPEN:
                            ret = scanner.getNextSymbol();
                            if (ret.getNr() == Scanner.TOKEN_NUMBER) {
                                offset = Integer.parseInt(ret.getText());
                                ret = scanner.getNextSymbol();
                                if (ret.getNr() == Scanner.TOKEN_PLUS) {
                                    ret = scanner.getNextSymbol();
                                    if (ret.getNr()
                                            == Scanner.TOKEN_EXCLAMATIONPOINT) {
                                        ret = scanner.getNextSymbol();
                                        if (ret.getNr()
                                                == Scanner.TOKEN_REGISTER) {
                                            reg = Integer.parseInt(
                                                    ret.getText()
                                                            .substring(1,
                                                                    ret.getText()
                                                                            .length()));
                                            ret = scanner.getNextSymbol();
                                            if (ret.getNr()
                                                    == Scanner.TOKEN_BRACKETCLOSE) {
                                                ret = scanner.getNextSymbol();
                                                if (ret.equal(
                                                        new int[]{
                                                                Scanner.TOKEN_COMMA,
                                                                Scanner.TOKEN_NEWLINE,
                                                                Scanner.TOKEN_APOSTROPHE,
                                                                Scanner.TOKEN_EOF})) {
                                                    list.add(
                                                            new IndAddressing(
                                                                    machine,
                                                                    offset,
                                                                    reg,
                                                                    length,
                                                                    0));
                                                } else {
                                                    if (ret.getNr()
                                                            == Scanner.TOKEN_SLASH) {
                                                        ret = scanner.getNextSymbol();
                                                        if (ret.getNr()
                                                                == Scanner.TOKEN_REGISTER) {
                                                            index = Integer.parseInt(
                                                                    ret.getText()
                                                                            .substring(
                                                                                    1,
                                                                                    ret.getText()
                                                                                            .length()));
                                                            ret = scanner.getNextSymbol();
                                                            if (ret.getNr()
                                                                    == Scanner.TOKEN_SLASH) {
                                                                list.add(
                                                                        new IndAddressing(
                                                                                machine,
                                                                                offset,
                                                                                reg,
                                                                                index,
                                                                                length,
                                                                                0));
                                                                ret = scanner.getNextSymbol();

                                                            } else {
                                                                error.append(
                                                                        zeile,
                                                                        ERROR_PARSER_FALSE_SYMBOL);
                                                            }
                                                        } else {
                                                            error.append(
                                                                    zeile,
                                                                    ERROR_PARSER_FALSE_SYMBOL);
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case Scanner.TOKEN_NUMBER:
                    offset = Integer.parseInt(ret.getText());
                    ret = scanner.getNextSymbol();
                    if (ret.equal(
                            new int[]{Scanner.TOKEN_COMMA, Scanner.TOKEN_NEWLINE,
                                    Scanner.TOKEN_APOSTROPHE,
                                    Scanner.TOKEN_EOF})) {
                        list.add(new AbsAddress(machine, offset, length, adress));
                    } else {
                        if (ret.getNr() == Scanner.TOKEN_PLUS) {
                            ret = scanner.getNextSymbol();
                            if (ret.getNr() == Scanner.TOKEN_EXCLAMATIONPOINT) {
                                ret = scanner.getNextSymbol();
                                if (ret.getNr() == Scanner.TOKEN_REGISTER) {
                                    reg = Integer.parseInt(ret.getText()
                                            .substring(
                                                    1,
                                                    ret.getText()
                                                            .length()));
                                    ret = scanner.getNextSymbol();
                                    if (ret.equal(new int[]{
                                            Scanner.TOKEN_COMMA,
                                            Scanner.TOKEN_NEWLINE,
                                            Scanner.TOKEN_APOSTROPHE,
                                            Scanner.TOKEN_EOF})) {
                                        list.add(new RelAddressing(
                                                machine, offset, reg, length,
                                                0));
                                    } else {
                                        if (ret.getNr()
                                                == Scanner.TOKEN_SLASH) {
                                            ret = scanner.getNextSymbol();
                                            if (ret.getNr()
                                                    == Scanner.TOKEN_REGISTER) {
                                                index = Integer.parseInt(
                                                        ret.getText()
                                                                .substring(
                                                                        1,
                                                                        ret.getText()
                                                                                .length()));
                                                ret = scanner.getNextSymbol();
                                                if (ret.getNr()
                                                        == Scanner.TOKEN_SLASH) {
                                                    list.add(
                                                            new RelAddressing(
                                                                    machine,
                                                                    offset,
                                                                    reg,
                                                                    index,
                                                                    length,
                                                                    0));
                                                    ret = scanner.getNextSymbol();

                                                } else {
                                                    error.append(
                                                            zeile,
                                                            ERROR_PARSER_FALSE_SYMBOL);
                                                }
                                            } else {
                                                error.append(
                                                        zeile,
                                                        ERROR_PARSER_FALSE_SYMBOL);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                    break;
                case Scanner.TOKEN_I:
                    Long zahl;
                    ret = scanner.getNextSymbol();
                    switch (ret.getNr()) {
                        case Scanner.TOKEN_BIN:

                            zahl = Long.valueOf(ret.getText().substring(2, ret.getText()
                                    .length()
                                    - 1), 2);
                            if (!NumberConversion.valid_number(zahl, length))
                                error.append(zeile,
                                        ERROR_PARSER_NO_VALIDNUMBER);

                            list.add(new ImmediateOperand(machine, NumberConversion.binToByte(
                                    ret.getText()
                                            .substring(2, ret.getText().length() - 1),
                                    length), length));
                            ret = scanner.getNextSymbol();
                            break;
                        case Scanner.TOKEN_HEX:
                            zahl = Long.valueOf(ret.getText().substring(2, ret.getText()
                                    .length()
                                    - 1), 16);
                            if (!NumberConversion.valid_number(zahl, length))
                                error.append(zeile,
                                        ERROR_PARSER_NO_VALIDNUMBER);
                            list.add(new ImmediateOperand(machine, NumberConversion.hexToByte(
                                    ret.getText()
                                            .substring(2, ret.getText().length() - 1),
                                    length), length));
                            ret = scanner.getNextSymbol();
                            break;
                        case Scanner.TOKEN_NUMBER:
                            if (!floating) {
                                zahl = Long.valueOf(ret.getText());
                                if (!NumberConversion.valid_number(zahl, length))
                                    error.append(zeile,
                                            ERROR_PARSER_NO_VALIDNUMBER);
                                list.add(new ImmediateOperand(
                                        machine, NumberConversion.intToByte(
                                                Integer.valueOf(ret.getText()),
                                                length), length));
                                ret = scanner.getNextSymbol();
                            } else {
                                switch (length) {
                                    case 4:
                                        try {
                                            Float a = Float.parseFloat(
                                                    ret.getText());
                                            list.add(new ImmediateOperand(
                                                    machine, NumberConversion.intToByte(
                                                            Float.floatToIntBits(
                                                                    a), length),
                                                    length));
                                            ret = scanner.getNextSymbol();

                                        } catch (Exception e) {
                                            error.append(zeile,
                                                    ERROR_PARSER_PARSE_FLOAT);

                                        }
                                        break;
                                    case 8:
                                        try {
                                            Double a = Double.parseDouble(
                                                    ret.getText());
                                            list.add(new ImmediateOperand(
                                                    machine, NumberConversion.longToByte(
                                                            Double.doubleToLongBits(
                                                                    a), length),
                                                    length));
                                            ret = scanner.getNextSymbol();
                                        } catch (Exception e) {
                                            error.append(zeile,
                                                    ERROR_PARSER_PARSE_DOUBLE);
                                        }
                                        break;
                                    default:
                                        error.append(zeile,
                                                ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                                }
                            }
                            break;

                        case Scanner.TOKEN_FLOAT:
                            if (!floating) {
                                error.append(zeile,
                                        ERROR_PARSER_NO_FLOAT);
                                ret = scanner.getNextSymbol();
                            } else {
                                switch (length) {
                                    case 4:
                                        try {
                                            Float a = Float.parseFloat(
                                                    ret.getText());
                                            list.add(new ImmediateOperand(
                                                    machine, NumberConversion.intToByte(
                                                            Float.floatToIntBits(
                                                                    a), length),
                                                    length));
                                            ret = scanner.getNextSymbol();

                                        } catch (Exception e) {
                                            error.append(zeile,
                                                    ERROR_PARSER_PARSE_FLOAT);

                                        }
                                        break;
                                    case 8:
                                        try {
                                            Double a = Double.parseDouble(
                                                    ret.getText());
                                            list.add(new ImmediateOperand(
                                                    machine, NumberConversion.longToByte(
                                                            Double.doubleToLongBits(
                                                                    a), length),
                                                    length));
                                            ret = scanner.getNextSymbol();
                                        } catch (Exception e) {
                                            error.append(zeile,
                                                    ERROR_PARSER_PARSE_DOUBLE);
                                        }
                                        break;
                                    default:
                                        error.append(zeile,
                                                ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                                }

                            }
                            break;
                        default:
                            error.append(zeile,
                                    ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                            break;
                    }
                    break;
                case Scanner.TOKEN_MINUS:
                    ret = scanner.getNextSymbol();
                    if (ret.getNr() == Scanner.TOKEN_EXCLAMATIONPOINT) {
                        ret = scanner.getNextSymbol();
                        if (ret.getNr() == Scanner.TOKEN_REGISTER) {
                            list.add(new CellarAddressing(machine, Integer.parseInt(
                                    ret.getText()
                                            .substring(1, ret.getText().length())),
                                    length, -length,
                                    false));
                            ret = scanner.getNextSymbol();
                        } else {
                            error.append(zeile,
                                    ERROR_PARSER_NO_REGISTER);
                        }
                    } else {
                        error.append(zeile, ERROR_PARSER_FALSE_SYMBOL);
                    }

                    break;
                default:

            }

            switch (ret.getNr()) {
                case Scanner.TOKEN_COMMA:
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_APOSTROPHE:
                    // System.out.println(";");
                    return list;
                case Scanner.TOKEN_NEWLINE:
                    // System.out.println("nl");
                    return list;
                case Scanner.TOKEN_EOF:
                    // System.out.println("eof");
                    return list;
                case Scanner.TOKEN_FEHLER:
                    error.append(zeile, ret.getText());
                    return list;
                default:
                    error.append(zeile, ERROR_PARSER_NO_END_SIGN);
                    ret = scanner.getNextSymbol();
                    break;

            }
        }
    }

    ;

    /**
     * Operands for dd.
     *
     * @return the array list
     */
    private ArrayList<Operand> operandsForDD() {
        int zeile = ret.getLine();
        int length = 0;
        int newlength = 0;
        int zahl = 0;
        boolean floating = false;
        ArrayList<Operand> list = new ArrayList<Operand>();
        String label = "";
        while (true) {
            switch (ret.getNr()) {
                case Scanner.TOKEN_B:
                    length = 1;
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_H:
                    length = 2;
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_W:
                    length = 4;
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_F:
                    length = 4;
                    floating = true;
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_D:
                    length = 8;
                    floating = true;
                    ret = scanner.getNextSymbol();
                    break;
            }

            switch (ret.getNr()) {

                case Scanner.TOKEN_NAME:
                    label = ret.getText();
                    ret = scanner.getNextSymbol();

                    int off = 0;
                    while (ret.getNr() == Scanner.TOKEN_NUMBER) {
                        off += Integer.parseInt(
                                ret.getText().substring(0, 1).equals("+") ?
                                        ret.getText()
                                                .substring(1, ret.getText().length()) :
                                        ret.getText());
                        ret = scanner.getNextSymbol();
                    }
                    list.add(new AbsAddress(machine, label, 4, adress, off));
                    break;

                case Scanner.TOKEN_STRING:
                    boolean found = false;
                    label = ret.getText().substring(1, ret.getText().length() - 1);
                    for (byte b : label.getBytes()) {
                        // Debug: character output

                        if (b != 39 || found) {
                            list.add(new ImmediateOperand(
                                    machine, NumberConversion.intToByte(b, 1), 1));
                            found = false;
                        } else {
                            found = true;
                        }
                    }
                    ret = scanner.getNextSymbol();
                    break;

                case Scanner.TOKEN_BIN:
                    zahl = Integer.parseInt(
                            ret.getText().substring(2, ret.getText().length() - 1), 2);
                    newlength = length == 0 ? getLengthForOperand(zahl) : length;
                    list.add(new ImmediateOperand(
                            machine, NumberConversion.intToByte(zahl, newlength), newlength));
                    ret = scanner.getNextSymbol();
                    break;

                case Scanner.TOKEN_HEX:
                    zahl = Integer.parseInt(
                            ret.getText().substring(2, ret.getText().length() - 1), 16);
                    newlength = length == 0 ? getLengthForOperand(zahl) : length;
                    list.add(new ImmediateOperand(
                            machine, NumberConversion.intToByte(zahl, newlength), newlength));
                    ret = scanner.getNextSymbol();
                    break;

                case Scanner.TOKEN_FLOAT:
                    switch (length) {
                        case 4:
                            try {
                                Float a = Float.parseFloat(ret.getText());
                                list.add(new ImmediateOperand(
                                        machine, NumberConversion.intToByte(
                                                Float.floatToIntBits(a), length),
                                        length));
                                ret = scanner.getNextSymbol();

                            } catch (Exception e) {
                                error.append(zeile,
                                        ERROR_PARSER_PARSE_FLOAT);

                            }
                            break;
                        case 8:
                            try {
                                Double a = Double.parseDouble(ret.getText());
                                list.add(new ImmediateOperand(
                                        machine, NumberConversion.longToByte(
                                                Double.doubleToLongBits(a), length),
                                        length));
                                ret = scanner.getNextSymbol();
                            } catch (Exception e) {
                                error.append(zeile,
                                        ERROR_PARSER_PARSE_DOUBLE);
                            }
                            break;
                        default:
                            error.append(zeile,
                                    ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                    }

                    break;

                case Scanner.TOKEN_NUMBER:
                    if (!floating) {
                        zahl = Integer.valueOf(ret.getText());
                        newlength =
                                length == 0 ? getLengthForOperand(zahl) : length;
                        list.add(new ImmediateOperand(
                                machine, NumberConversion.intToByte(zahl, newlength),
                                newlength));
                        ret = scanner.getNextSymbol();
                    } else {
                        switch (length) {
                            case 4:
                                try {
                                    Float a = Float.parseFloat(ret.getText());
                                    list.add(new ImmediateOperand(
                                            machine, NumberConversion.intToByte(
                                                    Float.floatToIntBits(a),
                                                    length), length));
                                    ret = scanner.getNextSymbol();

                                } catch (Exception e) {
                                    error.append(zeile,
                                            ERROR_PARSER_PARSE_FLOAT);

                                }
                                break;
                            case 8:
                                try {
                                    Double a = Double.parseDouble(
                                            ret.getText());
                                    list.add(new ImmediateOperand(
                                            machine, NumberConversion.longToByte(
                                                    Double.doubleToLongBits(a),
                                                    length), length));
                                    ret = scanner.getNextSymbol();
                                } catch (Exception e) {
                                    error.append(zeile,
                                            ERROR_PARSER_PARSE_DOUBLE);
                                }
                                break;
                            default:
                                error.append(zeile,
                                        ERROR_PARSER_NO_IMMEDIATE_OPERAND);
                        }
                    }
                    break;

                case Scanner.TOKEN_BRACKETOPEN:
                    ret = scanner.getNextSymbol();
                    ArrayList<Operand> newList = operandsForDD();
                    ret = scanner.getNextSymbol();
                    if (ret.getNr() == Scanner.TOKEN_STAR) {
                        ret = scanner.getNextSymbol();
                        if (ret.getNr() == Scanner.TOKEN_NUMBER) {
                            zahl = Integer.parseInt(ret.getText());
                            for (int i = 0; i < zahl; i++) {
                                for (Operand x : newList) {
                                    list.add(x.copy());
                                }
                            }
                        } else {
                            error.append(ret.getLine(),
                                    ERROR_PARSER_NONUMBER);
                        }
                    } else {
                        error.append(ret.getLine(), ERROR_PARSER_NO_STAR);
                    }
                    ret = scanner.getNextSymbol();
                    break;
            }

            switch (ret.getNr()) {
                case Scanner.TOKEN_COMMA:
                    ret = scanner.getNextSymbol();
                    break;
                case Scanner.TOKEN_BRACKETCLOSE:
                    return list;
                case Scanner.TOKEN_APOSTROPHE:
                    // System.out.println(";");
                    return list;
                case Scanner.TOKEN_NEWLINE:
                    // System.out.println("nl");
                    return list;
                case Scanner.TOKEN_EOF:
                    // System.out.println("eof");
                    return list;
                case Scanner.TOKEN_FEHLER:
                    error.append(ret.getLine(), ret.getText());
                    return list;
                default:
                    error.append(ret.getLine(), ERROR_PARSER_NO_END_SIGN);
                    ret = scanner.getNextSymbol();
                    break;

            }
        }
    }

    /**
     * Or.
     *
     * @return the command
     */
    private Command or() {
        int zeile = ret.getLine();
        Or befehl = null;
        int length = 0;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
            case Scanner.TOKEN_B:
                length = 1;
                break;
            case Scanner.TOKEN_H:
                length = 2;
                break;
            case Scanner.TOKEN_W:
                length = 4;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHW);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 2:
                befehl = new Or(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end);
                break;
            case 3:
                befehl = new Or(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Parses the next command.
     *
     * @return the command
     */
    private Command parseNextCommand() {
        beg = ret.getBeg();
        end = ret.getEnd();
        switch (ret.getNr()) {
            case Scanner.TOKEN_LABEL:
                return label();
            case Scanner.TOKEN_ADD:
                return add();
            case Scanner.TOKEN_SUB:
                return sub();
            case Scanner.TOKEN_MOVE:
                return move();
            case Scanner.TOKEN_CLEAR:
                return clear();
            case Scanner.TOKEN_MOVEC:
                return movec();
            case Scanner.TOKEN_MOVEA:
                return movea();
            case Scanner.TOKEN_MOVEN:
                return moven();
            case Scanner.TOKEN_PUSHR:
                return pushr();
            case Scanner.TOKEN_POPR:
                return popr();
            case Scanner.TOKEN_SH:
                return sh();
            case Scanner.TOKEN_ROT:
                return rot();
            case Scanner.TOKEN_CALL:
                return call();
            case Scanner.TOKEN_RET:
                return ret();
            case Scanner.TOKEN_DD:
                return dd();
            case Scanner.TOKEN_JUMP:
                return jump(8);
            case Scanner.TOKEN_JEQ:
                return jump(0);
            case Scanner.TOKEN_JNE:
                return jump(1);
            case Scanner.TOKEN_JGT:
                return jump(2);
            case Scanner.TOKEN_JGE:
                return jump(3);
            case Scanner.TOKEN_JLT:
                return jump(4);
            case Scanner.TOKEN_JLE:
                return jump(5);
            case Scanner.TOKEN_JC:
                return jump(6);
            case Scanner.TOKEN_JNC:
                return jump(7);
            case Scanner.TOKEN_JV:
                return jump(9);
            case Scanner.TOKEN_JNV:
                return jump(10);
            case Scanner.TOKEN_CMP:
                return cmp();
            case Scanner.TOKEN_RES:
                return res();
            case Scanner.TOKEN_HALT:
                return halt();
            case Scanner.TOKEN_MULT:
                return mult();
            case Scanner.TOKEN_DIV:
                return div();
            case Scanner.TOKEN_EQU:
                equal();
                return null;
            case Scanner.TOKEN_NEWLINE:
                ret = scanner.getNextSymbol();
                return parseNextCommand();
            case Scanner.TOKEN_OR:
                return or();
            case Scanner.TOKEN_ANDNOT:
                return andnot();
            case Scanner.TOKEN_XOR:
                return xor();
            case Scanner.TOKEN_EXT:
                return ext();
            case Scanner.TOKEN_EXTS:
                return exts();
            case Scanner.TOKEN_INS:
                return ins();
            case Scanner.TOKEN_FINDC:
                return findc();
            case Scanner.TOKEN_FINDS:
                return finds();
            case Scanner.TOKEN_JBSSI:
                return jbssi();
            case Scanner.TOKEN_JBCCI:
                return jbcci();
            case Scanner.TOKEN_CONV:
                return conv();
            case Scanner.TOKEN_SBPSW:
                return sbpsw();
            case Scanner.TOKEN_SEG:
                ret = scanner.getNextSymbol();
                if (ret.getNr() == Scanner.TOKEN_NAME) {
                    ret = scanner.getNextSymbol();
                }
                return parseNextCommand();
            case Scanner.TOKEN_END:
                return null;
            case Scanner.TOKEN_EOF:
                return null;
            default:
                error.append(ret.getLine(), ERROR_PARSER_FALSE_SYMBOL);
                return null;
        }
    }

    /**
     * ext.
     *
     * @return the command
     */
    private Command ext() {
        int zeile = ret.getLine();
        Ext befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 4:
                befehl = new Ext(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), operands.get(3), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * exts.
     *
     * @return the command
     */
    private Command exts() {
        int zeile = ret.getLine();
        Exts befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 4:
                befehl = new Exts(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), operands.get(3), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * ext.
     *
     * @return the command
     */
    private Command ins() {
        int zeile = ret.getLine();
        Ins befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 4:
                befehl = new Ins(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), operands.get(3), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * findc.
     *
     * @return the command
     */
    private Command findc() {
        int zeile = ret.getLine();
        Findc befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 2:
                befehl = new Findc(machine, zeile, adress, operands.get(0), operands.get(1),
                        null, null, beg, end);
                break;
            case 4:
                befehl = new Findc(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), operands.get(3), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * finds.
     *
     * @return the command
     */
    private Command finds() {
        int zeile = ret.getLine();
        Finds befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 2:
                befehl = new Finds(machine, zeile, adress, operands.get(0), operands.get(1),
                        null, null, beg, end);
                break;
            case 4:
                befehl = new Finds(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), operands.get(3), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * jbssi.
     *
     * @return the command
     */
    private Command jbssi() {
        int zeile = ret.getLine();
        Jbssi befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 3:
                befehl = new Jbssi(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * jbssi.
     *
     * @return the command
     */
    private Command jbcci() {
        int zeile = ret.getLine();
        Jbcci befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 3:
                befehl = new Jbcci(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * jbssi.
     *
     * @return the command
     */
    private Command conv() {
        int zeile = ret.getLine();
        Conv befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(1, false);
        switch (operands.size()) {
            case 2:
                befehl = new Conv(machine, zeile, adress, operands.get(0), operands.get(1), beg,
                        end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;
    }

    /**
     * sbpsw.
     *
     * @return the command
     */
    private Command sbpsw() {
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        return parseNextCommand();
    }

    /**
     * Popr.
     *
     * @return the command
     */
    private Command popr() {

        Popr befehl = null;

        befehl = new Popr(machine, ret.getLine(), adress, beg, end);
        ret = scanner.getNextSymbol();
        return befehl;
    }

    /**
     * Pushr.
     *
     * @return the command
     */
    private Command pushr() {
        Pushr befehl = null;
        int length = 0;

        befehl = new Pushr(machine, ret.getLine(), adress, length, beg, end);
        ret = scanner.getNextSymbol();
        return befehl;

    }

    /**
     * Res.
     *
     * @return the command
     */
    private Command res() {
        int zeile = ret.getLine();
        Res befehl = null;
        ret = scanner.getNextSymbol();
        if (ret.getNr() != Scanner.TOKEN_NUMBER) {
            error.append(zeile, ERROR_PARSER_NONUMBER);
            return null;
        }
        befehl = new Res(machine, zeile, adress, Integer.parseInt(ret.getText()), beg, end);
        ret = scanner.getNextSymbol();
        return befehl;
    }

    ;

    /**
     * Ret.
     *
     * @return the command
     */
    private Command ret() {
        Ret befehl = null;
        befehl = new Ret(machine, ret.getLine(), adress, beg, end);
        ret = scanner.getNextSymbol();
        return befehl;
    }

    ;

    /**
     * Rot.
     *
     * @return the command
     */
    private Command rot() {
        int zeile = ret.getLine();
        Rot befehl = null;
        int length = 4;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 3:
                befehl = new Rot(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Sh.
     *
     * @return the command
     */
    private Command sh() {
        int zeile = ret.getLine();
        Sh befehl = null;

        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(4, false);
        switch (operands.size()) {
            case 3:
                befehl = new Sh(machine, zeile, adress, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Start.
     */
    public void start() {
        ret = scanner.getNextSymbol();
        while (ret.getNr() != Scanner.TOKEN_EOF && ret.getNr() != Scanner.TOKEN_FEHLER) {

            Command befehl = parseNextCommand();
            if (befehl != null) {
                program.add(befehl);
                adress += befehl.encode().length;
            }
            if (ret.getNr() == Scanner.TOKEN_EOF) {
                return;
            }
            ret = scanner.getNextSymbol();
        }

    }

    /**
     * Sub.
     *
     * @return the command
     */
    private Command sub() {
        int zeile = ret.getLine();
        Sub befehl = null;
        int length = 0;
        boolean floating = false;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
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
                length = 4;
                floating = true;
                break;
            case Scanner.TOKEN_D:
                length = 8;
                floating = true;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHWFD);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, floating);
        switch (operands.size()) {
            case 2:
                befehl = new Sub(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end, floating);
                break;
            case 3:
                befehl = new Sub(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end, floating);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    /**
     * Xor.
     *
     * @return the command
     */
    private Command xor() {
        int zeile = ret.getLine();
        Xor befehl = null;
        int length = 0;
        ret = scanner.getNextSymbol();
        switch (ret.getNr()) {
            case Scanner.TOKEN_B:
                length = 1;
                break;
            case Scanner.TOKEN_H:
                length = 2;
                break;
            case Scanner.TOKEN_W:
                length = 4;
                break;
            default:
                error.append(zeile, ERROR_PARSER_BHW);
                return null;

        }
        ret = scanner.getNextSymbol();
        ArrayList<Operand> operands = operands(length, false);
        switch (operands.size()) {
            case 2:
                befehl = new Xor(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        beg, end);
                break;
            case 3:
                befehl = new Xor(machine, zeile, adress, length, operands.get(0), operands.get(1),
                        operands.get(2), beg, end);
                break;
            default:
                error.append(zeile, ERROR_PARSER_NUMBER_OF_OPERANDS);
        }

        return befehl;

    }

    private void nextCommand() {
        while (!ret.equal(new int[]{Scanner.TOKEN_APOSTROPHE, Scanner.TOKEN_NEWLINE,
                Scanner.TOKEN_EOF})) {
            ret = scanner.getNextSymbol();
        }
    }

    /**
     * Gets the labels from the code generator.
     *
     * @return List of defined labels with their addresses
     */
    public java.util.ArrayList<Label> getLabels() {
        return program.getLabels();
    }

}
