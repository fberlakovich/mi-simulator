/**
 *
 */
package gui;

import java.awt.*;

/**
 * Diese Klasse enthält die Konstanten der MI-Implementierung
 *
 * @author Matthias Oehme
 */
public class CONSTANTS {

    public final static String VERSION = "Version 1.12 (14. Januar 2025)";
    // Fehlermeldungen
    public final static String ERROR_SAVEFILE = "Fehler beim Speichern der Datei.";
    public final static String ERROR_OPENFILE = "Fehler beim Öffnen der Datei.";
    public final static String ERROR_PARSER = "Fehler in Zeile";
    public final static String ERROR_PARSER_BHWFD = "B, H, W,F oder D erwartet.";
    public final static String ERROR_PARSER_BHW = "B, H oder W erwartet.";
    public final static String ERROR_PARSER_NUMBER_OF_OPERANDS = "ungültige Anzahl an Operanden";
    public final static String ERROR_PARSER_NONUMBER = "ganze Zahl erwartet.";
    public final static String ERROR_PARSER_NO_END_SIGN = "Komma, Semikolon oder Zeilenwechsel erwartet.";
    public final static String ERROR_PARSER_FALSE_SYMBOL = "unerwartetes Symbol.";
    public final static String ERROR_PARSER_PARSE_FLOAT = "Fehler beim Parsen eines Floatwertes.";
    public final static String ERROR_PARSER_PARSE_DOUBLE = "Fehler beim Parsen eines Doublewertes.";
    public final static String ERROR_PARSER_NO_IMMEDIATE_OPERAND = "kein gültiger Direkter Operand.";
    public final static String ERROR_PARSER_NO_FLOAT = "Float hier nicht möglich.";
    public final static String ERROR_PARSER_NO_REGISTER = "Register, SP oder PC erwartet.";
    public final static String ERROR_PARSER_NO_STAR = "*Zahl erwartet nach einer Datengruppe.";
    public final static String ERROR_PARSER_NO_VALIDNUMBER = "Zahl befindet sich nicht im gültigen Zahlenbereich.";

    public final static String ERROR_SETBREAKPOINT_TITEL = "Breakpoint nicht gesetzt";
    public final static String ERROR_SETBREAKPOINT_NOT_ASSEMBLED = "Breakoint konnte nicht gesetzt werden, da das Programm noch nicht assembliert ist.";
    public final static String ERROR_SETBREAKPOINT_NOT_EXECUTABLE_INSTR = "Breakoint konnte nicht gesetzt werden, da sich in der Zeile kein ausführbarer Befehl befindet.";

    public final static String ERROR_MANIPULATION_MEMORY =
            "Es wurde eine Manipulation im Maschinencode ihres Programms festgestellt. Möglichweise kann der aktuelle Befehl nicht mehr hervorgehoben werden. "
                    + "\nDie aktuelle Instruktion wird unterhalb der Programmeingabe angezeigt.";

    public final static String ERROR_MEMORY_TEXT = "Zugriff auf ungültige Speicheradresse! Speicheradr: ";
    public final static String ERROR_MEMORY_TITLE = "Ungültiger Speicherzugriff";

    public final static String INFO_TEXT =
            "MI-Assembler erstellt von Matthias Oehme\n" + VERSION;
    public final static String INFO_TITLE = "Info";
    public final static String HELP_TEXT = "Bei Fragen wenden Sie sich bitte an Ihren Betreuer.";
    public final static String HELP_TITLE = "Hilfe";

    public final static String ASSEMBLE_SUCCESSFUL = "Programm wurde erfolgreich übersetzt.";
    public final static String ASSEMBLE_UNSUCCESSFUL = "Programm konnte nicht übersetzt werden.";
    public final static String PROGRAM_END = "Programmende erreicht";
    public final static String LABEL_INSTR = "dekodierte Instruktion: ";
    public final static String TITLE = "MI-Assembler " + VERSION;

    public final static Font FONT = new Font("Courier", Font.PLAIN, 14);
    public final static Color DARK_GREY = new Color(90, 90, 90);

    // Konstanten für die Interpreterumgebung
    public final static int MEMORY_LENGTH = 1048576; // 1MByte Speicher
    public final static int SP_REGISTER = 14; // Stackpointer
    public final static int PC_REGISTER = 15; // Programcounter
    public final static int NUMBER_OF_REGISTER = 16; // Sollte nicht verändert
    public final static int WORD_SIZE = 4;
    // werden, da die Opcodes
    // auf 16 Register
    // ausgelegt sind

    // Konstanten GUI
    public final static int WINDOW_HEIGHT = 800;
    public final static int WINDOW_WIDTH = 900;

    public final static int MEMORY_HEIGHT = 260;
    public final static int MEMORY_WIDTH = 220;

    public final static int STACK_HEIGHT = 260;
    public final static int STACK_WIDTH = 220;

}
