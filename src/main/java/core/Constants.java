package core;

/**
 * Core constants for the MI-Simulator implementation.
 * These constants are independent of any GUI or CLI specifics.
 */
public class Constants {

    public static final String VERSION = "Version 1.14";
    
    // Error messages
    public static final String ERROR_PARSER = "Fehler in Zeile";
    public static final String ERROR_PARSER_BHWFD = "B, H, W,F oder D erwartet.";
    public static final String ERROR_PARSER_BHW = "B, H oder W erwartet.";
    public static final String ERROR_PARSER_NUMBER_OF_OPERANDS = "ungültige Anzahl an Operanden";
    public static final String ERROR_PARSER_NONUMBER = "ganze Zahl erwartet.";
    public static final String ERROR_PARSER_NO_END_SIGN = "Komma, Semikolon oder Zeilenwechsel erwartet.";
    public static final String ERROR_PARSER_FALSE_SYMBOL = "unerwartetes Symbol.";
    public static final String ERROR_PARSER_PARSE_FLOAT = "Fehler beim Parsen eines Floatwertes.";
    public static final String ERROR_PARSER_PARSE_DOUBLE = "Fehler beim Parsen eines Doublewertes.";
    public static final String ERROR_PARSER_NO_IMMEDIATE_OPERAND = "kein gültiger Direkter Operand.";
    public static final String ERROR_PARSER_NO_FLOAT = "Float hier nicht möglich.";
    public static final String ERROR_PARSER_NO_REGISTER = "Register, SP oder PC erwartet.";
    public static final String ERROR_PARSER_NO_STAR = "*Zahl erwartet nach einer Datengruppe.";
    public static final String ERROR_PARSER_NO_VALIDNUMBER = "Zahl befindet sich nicht im gültigen Zahlenbereich.";

    public static final String ERROR_MANIPULATION_MEMORY =
            "Es wurde eine Manipulation im Maschinencode ihres Programms festgestellt. Möglichweise kann der aktuelle Befehl nicht mehr hervorgehoben werden. "
                    + "\nDie aktuelle Instruktion wird unterhalb der Programmeingabe angezeigt.";

    public static final String ERROR_MEMORY_TEXT = "Zugriff auf ungültige Speicheradresse! Speicheradr: ";
    public static final String ERROR_MEMORY_TITLE = "Ungültiger Speicherzugriff";

    public static final String ASSEMBLE_SUCCESSFUL = "Programm wurde erfolgreich übersetzt.";
    public static final String ASSEMBLE_UNSUCCESSFUL = "Programm konnte nicht übersetzt werden.";
    public static final String PROGRAM_END = "Programmende erreicht";
    public static final String LABEL_INSTR = "dekodierte Instruktion: ";

    // Constants for interpreter environment
    public static final int MEMORY_LENGTH = 1048576; // 1MByte memory
    public static final int SP_REGISTER = 14; // Stack pointer
    public static final int PC_REGISTER = 15; // Program counter
    public static final int NUMBER_OF_REGISTER = 16; // Should not be changed as opcodes are designed for 16 registers
    public static final int WORD_SIZE = 4;
}
