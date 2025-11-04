/**
 *
 */
package gui;

import core.Constants;
import java.awt.*;

/**
 * Diese Klasse enthält die GUI-spezifischen Konstanten der MI-Implementierung.
 * Für allgemeine Konstanten siehe core.Constants.
 *
 * @author Matthias Oehme
 */
public class CONSTANTS {

    // Import core constants for backward compatibility
    public final static String VERSION = Constants.VERSION;
    public final static String ERROR_PARSER = Constants.ERROR_PARSER;
    public final static String ERROR_PARSER_BHWFD = Constants.ERROR_PARSER_BHWFD;
    public final static String ERROR_PARSER_BHW = Constants.ERROR_PARSER_BHW;
    public final static String ERROR_PARSER_NUMBER_OF_OPERANDS = Constants.ERROR_PARSER_NUMBER_OF_OPERANDS;
    public final static String ERROR_PARSER_NONUMBER = Constants.ERROR_PARSER_NONUMBER;
    public final static String ERROR_PARSER_NO_END_SIGN = Constants.ERROR_PARSER_NO_END_SIGN;
    public final static String ERROR_PARSER_FALSE_SYMBOL = Constants.ERROR_PARSER_FALSE_SYMBOL;
    public final static String ERROR_PARSER_PARSE_FLOAT = Constants.ERROR_PARSER_PARSE_FLOAT;
    public final static String ERROR_PARSER_PARSE_DOUBLE = Constants.ERROR_PARSER_PARSE_DOUBLE;
    public final static String ERROR_PARSER_NO_IMMEDIATE_OPERAND = Constants.ERROR_PARSER_NO_IMMEDIATE_OPERAND;
    public final static String ERROR_PARSER_NO_FLOAT = Constants.ERROR_PARSER_NO_FLOAT;
    public final static String ERROR_PARSER_NO_REGISTER = Constants.ERROR_PARSER_NO_REGISTER;
    public final static String ERROR_PARSER_NO_STAR = Constants.ERROR_PARSER_NO_STAR;
    public final static String ERROR_PARSER_NO_VALIDNUMBER = Constants.ERROR_PARSER_NO_VALIDNUMBER;
    public final static String ERROR_MANIPULATION_MEMORY = Constants.ERROR_MANIPULATION_MEMORY;
    public final static String ERROR_MEMORY_TEXT = Constants.ERROR_MEMORY_TEXT;
    public final static String ERROR_MEMORY_TITLE = Constants.ERROR_MEMORY_TITLE;
    public final static String ASSEMBLE_SUCCESSFUL = Constants.ASSEMBLE_SUCCESSFUL;
    public final static String ASSEMBLE_UNSUCCESSFUL = Constants.ASSEMBLE_UNSUCCESSFUL;
    public final static String PROGRAM_END = Constants.PROGRAM_END;
    public final static String LABEL_INSTR = Constants.LABEL_INSTR;
    public final static int MEMORY_LENGTH = Constants.MEMORY_LENGTH;
    public final static int SP_REGISTER = Constants.SP_REGISTER;
    public final static int PC_REGISTER = Constants.PC_REGISTER;
    public final static int NUMBER_OF_REGISTER = Constants.NUMBER_OF_REGISTER;
    public final static int WORD_SIZE = Constants.WORD_SIZE;

    // GUI-specific error messages
    public final static String ERROR_SAVEFILE = "Fehler beim Speichern der Datei.";
    public final static String ERROR_OPENFILE = "Fehler beim Öffnen der Datei.";
    public final static String ERROR_SETBREAKPOINT_TITEL = "Breakpoint nicht gesetzt";
    public final static String ERROR_SETBREAKPOINT_NOT_ASSEMBLED = "Breakoint konnte nicht gesetzt werden, da das Programm noch nicht assembliert ist.";
    public final static String ERROR_SETBREAKPOINT_NOT_EXECUTABLE_INSTR = "Breakoint konnte nicht gesetzt werden, da sich in der Zeile kein ausführbarer Befehl befindet.";

    // GUI-specific info messages
    public final static String INFO_TEXT =
            "MI-Assembler erstellt von Matthias Oehme\n" + VERSION;
    public final static String INFO_TITLE = "Info";
    public final static String HELP_TEXT = "Bei Fragen wenden Sie sich bitte an Ihren Betreuer.";
    public final static String HELP_TITLE = "Hilfe";
    public final static String TITLE = "MI-Assembler " + VERSION;

    // GUI-specific style constants
    public final static Font FONT = new Font("Courier", Font.PLAIN, 14);
    public final static Color DARK_GREY = new Color(90, 90, 90);

    // GUI window size constants
    public final static int WINDOW_HEIGHT = 800;
    public final static int WINDOW_WIDTH = 900;

    public final static int MEMORY_HEIGHT = 260;
    public final static int MEMORY_WIDTH = 220;

    public final static int STACK_HEIGHT = 260;
    public final static int STACK_WIDTH = 220;

}
