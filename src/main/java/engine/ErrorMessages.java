package engine;

/**
 * Error messages for the MI simulator.
 *
 * These are user-facing messages that can be localized if needed.
 * Separated from MachineConstants to keep pure machine config separate.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Prevent instantiation
    }

    // Parser errors
    public static final String ERROR_PARSER = "Fehler in Zeile";
    public static final String ERROR_PARSER_BHWFD = "B, H, W, F oder D erwartet.";
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

    // Memory errors
    public static final String ERROR_MEMORY_TEXT = "Zugriff auf ungültige Speicheradresse! Speicheradr: ";
    public static final String ERROR_MEMORY_TITLE = "Ungültiger Speicherzugriff";

    // Breakpoint errors
    public static final String ERROR_SETBREAKPOINT_TITLE = "Breakpoint nicht gesetzt";
    public static final String ERROR_SETBREAKPOINT_NOT_ASSEMBLED = "Breakpoint konnte nicht gesetzt werden, da das Programm noch nicht assembliert ist.";
    public static final String ERROR_SETBREAKPOINT_NOT_EXECUTABLE_INSTR = "Breakpoint konnte nicht gesetzt werden, da sich in der Zeile kein ausführbarer Befehl befindet.";

    // Code manipulation warning
    public static final String ERROR_MANIPULATION_MEMORY =
            "Es wurde eine Manipulation im Maschinencode ihres Programms festgestellt. " +
            "Möglicherweise kann der aktuelle Befehl nicht mehr hervorgehoben werden. " +
            "Die aktuelle Instruktion wird unterhalb der Programmeingabe angezeigt.";

    // Assembly status
    public static final String ASSEMBLE_SUCCESSFUL = "Programm wurde erfolgreich übersetzt.";
    public static final String ASSEMBLE_UNSUCCESSFUL = "Programm konnte nicht übersetzt werden.";

    // Execution status
    public static final String PROGRAM_END = "Programmende erreicht";
    public static final String LABEL_INSTR = "dekodierte Instruktion: ";
}
