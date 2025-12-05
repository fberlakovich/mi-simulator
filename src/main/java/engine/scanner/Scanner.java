package engine.scanner;

import static engine.MachineConstants.PC_REGISTER;
import static engine.MachineConstants.SP_REGISTER;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Lexical scanner for MI assembly language.
 * Uses a state machine table for token recognition.
 */
public class Scanner {

    /** State transition table */
    private int[] table;

    /** Row index for token type in table */
    private int zeile;

    /** Table width (columns) */
    private int breite;

    /** Current position in table setup */
    private int pos;

    /** Input source text */
    private String input;

    /** EQU definitions for macro substitution */
    private ArrayList<Equal> equals = new ArrayList<Equal>();

    /** Iterator for current EQU expansion */
    private Iterator<Token> iter = null;

    /** Input as byte array */
    private byte[] b;

    /** Current position in input */
    private int position = 0;

    /** Previous position (start of current token) */
    private int altpos = 0;

    /** Last accepted position */
    private int last = 0;

    /** Current token type */
    private int token = 0;

    /** Current character */
    private int ch = 0;

    /** Current state */
    private int state = 1;

    /** Current line number */
    private int line = 1;

    /** Scanning for syntax highlighting? */
    private boolean highlight;

    // Token type constants

    public final static int TOKEN_NAME = 1;
    public final static int TOKEN_SEG = 2;
    public final static int TOKEN_EQU = 3;
    public final static int TOKEN_RES = 4;
    public final static int TOKEN_ADD = 5;
    public final static int TOKEN_SUB = 6;
    public final static int TOKEN_MULT = 7;
    public final static int TOKEN_DIV = 8;
    public final static int TOKEN_MOVE = 9;
    public final static int TOKEN_MOVEA = 10;
    public final static int TOKEN_MOVEC = 11;
    public final static int TOKEN_MOVEN = 12;
    public final static int TOKEN_JEQ = 14;
    public final static int TOKEN_JNE = 15;
    public final static int TOKEN_JGT = 16;
    public final static int TOKEN_JGE = 17;
    public final static int TOKEN_JLT = 18;
    public final static int TOKEN_JLE = 19;
    public final static int TOKEN_JC = 20;
    public final static int TOKEN_JNC = 21;
    public final static int TOKEN_JUMP = 22;
    public final static int TOKEN_JV = 23;
    public final static int TOKEN_JNV = 24;
    public final static int TOKEN_CALL = 25;
    public final static int TOKEN_RET = 26;
    public final static int TOKEN_POPR = 27;
    public final static int TOKEN_PUSHR = 28;
    public final static int TOKEN_SH = 29;
    public final static int TOKEN_ROT = 30;
    public final static int TOKEN_DD = 31;
    public final static int TOKEN_OR = 32;
    public final static int TOKEN_XOR = 33;
    public final static int TOKEN_ANDNOT = 34;
    public final static int TOKEN_END = 35;
    public final static int TOKEN_CLEAR = 36;
    public final static int TOKEN_LABEL = 37;
    public final static int TOKEN_CMP = 38;
    public final static int TOKEN_HALT = 39;
    public final static int TOKEN_EXT = 40;
    public final static int TOKEN_EXTS = 41;
    public final static int TOKEN_INS = 42;
    public final static int TOKEN_FINDC = 43;
    public final static int TOKEN_FINDS = 44;
    public final static int TOKEN_JBSSI = 45;
    public final static int TOKEN_JBCCI = 46;
    public final static int TOKEN_CONV = 47;
    public final static int TOKEN_SBPSW = 48;

    public final static int TOKEN_NUMBER = 50;
    public final static int TOKEN_BIN = 51;
    public final static int TOKEN_HEX = 52;
    public final static int TOKEN_FLOAT = 53;
    public final static int TOKEN_STRING = 54;
    public final static int TOKEN_COMMENT = 55;

    public final static int TOKEN_BRACKETOPEN = 60;
    public final static int TOKEN_BRACKETCLOSE = 61;
    public final static int TOKEN_PLUS = 62;
    public final static int TOKEN_MINUS = 63;
    public final static int TOKEN_COLON = 64;
    public final static int TOKEN_EXCLAMATIONPOINT = 65;
    public final static int TOKEN_COMMA = 66;
    public final static int TOKEN_APOSTROPHE = 67;
    public final static int TOKEN_STAR = 68;
    public final static int TOKEN_SLASH = 69;
    public final static int TOKEN_EQUALSIGN = 70;

    public final static int TOKEN_I = 80;
    public final static int TOKEN_B = 81;
    public final static int TOKEN_H = 82;
    public final static int TOKEN_W = 83;
    public final static int TOKEN_F = 84;
    public final static int TOKEN_D = 85;
    public final static int TOKEN_REGISTER = 86;

    public final static int TOKEN_SPACE = 90;
    public final static int TOKEN_NEWLINE = 91;
    public final static int TOKEN_EOF = 92;

    public final static int TOKEN_FEHLER = 100;

    /** Error messages for scanner errors */
    public static String[] FEHLER = new String[]{
            "Scanning error",
            "Error parsing binary number",
            "Error parsing hexadecimal number",
            "Error parsing floating point number",
            "Error parsing string",
            "Error parsing comment"};

    /**
     * Instantiates a new scanner.
     *
     * @param highlight the highlight
     */
    public Scanner(boolean highlight) {
        this.highlight = highlight;
    }

    /**
     * Adds the equal.
     *
     * @param equal the equal
     */
    public void addEqual(Equal equal) {
        String name = equal.getName();
        for (Equal eq : equals) {
            if (name.equals(eq.getName())) {
                equals.remove(eq);
            }
        }
        equals.add(equal);
    }

    /**
     * Gets the token iterator for an EQU definition.
     *
     * @param name the EQU name
     * @return token iterator, or null if not found
     */
    public Iterator<Token> getEqual(String name) {
        for (Equal eq : equals) {
            if (name.equals(eq.getName())) {
                return eq.getIterator();
            }
        }
        return null;
    }

    /**
     * Returns the next recognized token.
     *
     * @return the next token
     */
    public Token getNextSymbol() {
        // Check if returning tokens from EQU expansion
        if (!highlight && iter != null && iter.hasNext()) {
            return iter.next();
        }

        Token ret = getNextSymbol_help();

        // Skip comments and whitespace unless scanning for highlighting
        if (!highlight) {
            while (ret.getNr() == TOKEN_SPACE || ret.getNr() == TOKEN_COMMENT) {
                ret = getNextSymbol_help();
            }
        }
        return ret;
    }

    /**
     * Helper function that returns the next token using the state machine.
     *
     * @return the next token
     */
    private Token getNextSymbol_help() {
        while (true) {
            ch = b[position];
            if (ch < 0) {
                ch = ch + 128;
            }

            // Get next state from transition table
            state = table[state * breite + ch];

            // No follow state -> token recognized
            if (state == 0) {
                if (token != 0) {
                    last++;
                    state = 1;
                    position = last;
                    if (token < 100) {
                        // System.out.println("Token: " + (token) + " "
                        // + input.substring(altpos, position));
                        Token tok = new Token(token, input.substring(altpos,
                                position),
                                line, altpos, position);

                        altpos = position;
                        token = 0;
                        if (!highlight) {
                            if (tok.getNr() == TOKEN_NAME) {
                                iter = getEqual(tok.getText());
                            }
                            if (iter != null && iter.hasNext()) {

                                return iter.next().getCopy(line);
                            }
                        }
                        return tok;
                    } else {

                        // System.out.println("Token: " + (FEHLER[token - 100])
                        // + " " + input.substring(altpos, position));
                        Token tok = new Token(100, FEHLER[token - 100],
                                line, altpos, position);
                        altpos = position;

                        token = 0;
                        return tok;
                    }

                }
            } else {
                int h = table[state * breite + zeile];

                if (h != 0) {
                    token = h;
                    last = position;
                    if (h == 91) {
                        line++;
                    } else if (h == 92) {
                        break;
                    }
                }
                position++;
            }
        }
        return new Token(TOKEN_EOF, "Ende", line, altpos, position);
    }

    /**
     * Initializes the scanner with input source code.
     * Sets up the state transition table for lexical analysis.
     *
     * @param s the source code to scan
     */
    public void init(String s) {
        // Register aliases: SP -> R14, PC -> R15
        ArrayList<Token> sp = new ArrayList<Token>();
        sp.add(new Token(TOKEN_REGISTER, "R" + SP_REGISTER, 0, 0, 0));
        addEqual(new Equal("SP", sp));
        sp = new ArrayList<Token>();
        sp.add(new Token(TOKEN_REGISTER, "R" + PC_REGISTER, 0, 0, 0));
        addEqual(new Equal("PC", sp));

        // Normalize line endings
        input = s.replaceAll("\r", "");

        // Add EOF marker
        // Use ISO-8859-1 encoding to ensure 1:1 mapping between chars and bytes
        // This prevents StringIndexOutOfBoundsException with multi-byte UTF-8 chars
        try {
            b = (input + " ").getBytes("ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            // ISO-8859-1 is always supported, but fallback just in case
            b = (input + " ").getBytes();
        }
        b[b.length - 1] = 0x1A; // EOF

        // Create state transition table (257 columns x 157 rows)
        table = new int[(256 + 1) * (157)];

        // Table width
        breite = 257;

        // Last row stores token type
        zeile = 256;

        // Default: transition to error state 111
        for (int j = 0; j <= 255; j++) {
            table[breite + j] = 111;
        }

        // Initialize NAME state transitions (state 106)
        for (int i = 1; i < 71; i++) {
            for (int j = 'A'; j <= 'Z'; j++) {
                table[i * breite + j] = 106;
            }
            for (int j = 'a'; j <= 'z'; j++) {
                table[i * breite + j] = 106;
            }

            for (int j = '0'; j <= '9'; j++) {
                table[i * breite + j] = 106;
            }
            table[i * breite + '_'] = 106;
        }

        for (int i = 98; i < 101; i++) {
            for (int j = 'A'; j <= 'Z'; j++) {
                table[i * breite + j] = 106;
            }
            for (int j = 'a'; j <= 'z'; j++) {
                table[i * breite + j] = 106;
            }

            for (int j = '0'; j <= '9'; j++) {
                table[i * breite + j] = 106;
            }
            table[i * breite + '_'] = 106;
        }

        for (int i = 106; i < 109; i++) {
            for (int j = 'A'; j <= 'Z'; j++) {
                table[i * breite + j] = 106;
            }
            for (int j = 'a'; j <= 'z'; j++) {
                table[i * breite + j] = 106;
            }

            for (int j = '0'; j <= '9'; j++) {
                table[i * breite + j] = 106;
            }

            table[i * breite + '_'] = 106;
        }

        for (int i = 112; i < 155; i++) {
            for (int j = 'A'; j <= 'Z'; j++) {
                table[i * breite + j] = 106;
            }
            for (int j = 'a'; j <= 'z'; j++) {
                table[i * breite + j] = 106;
            }

            for (int j = '0'; j <= '9'; j++) {
                table[i * breite + j] = 106;
            }

            table[i * breite + '_'] = 106;
        }

        for (int j = 'A'; j <= 'Z'; j++) {
            table[72 * breite + j] = 106;
            table[76 * breite + j] = 106;
            table[110 * breite + j] = 106;
        }

        for (int j = 'a'; j <= 'z'; j++) {
            table[72 * breite + j] = 106;
            table[76 * breite + j] = 106;
            table[110 * breite + j] = 106;
        }

        table[72 * breite + '_'] = 106;
        table[76 * breite + '_'] = 106;
        table[110 * breite + '_'] = 106;

        // Initialize STRING state transitions
        for (int j = 0; j <= 255; j++) {
            table[85 * breite + j] = 86;
            table[86 * breite + j] = 86;
        }

        // Initialize COMMENT state transitions
        for (int j = 0; j <= 255; j++) {
            table[88 * breite + j] = 88;
        }

        // Initialize HEX number state transitions

        for (int j = 'A'; j <= 'F'; j++) {
            table[77 * breite + j] = 78;
            table[78 * breite + j] = 78;
        }

        for (int j = '0'; j <= '9'; j++) {
            table[77 * breite + j] = 78;
            table[78 * breite + j] = 78;
        }

        // Initialize NUMBER state transitions
        for (int j = '0'; j <= '9'; j++) {
            table[1 * breite + j] = 71;
            table[69 * breite + j] = 71;
            table[70 * breite + j] = 71;
            table[71 * breite + j] = 71;
        }

        // Initialize FLOAT state transitions

        for (int j = '0'; j <= '9'; j++) {
            table[80 * breite + j] = 81;
            table[81 * breite + j] = 81;
            table[83 * breite + j] = 84;
            table[84 * breite + j] = 84;
        }

        // Zeile 1
        pos = breite;
        table[pos + 0] = 101; // #0
        table[pos + 9] = 101; // TAB
        table[pos + 26] = 103; // EOF
        table[pos + 10] = 102; // LF
        table[pos + 13] = 102; // CR
        table[pos + 32] = 101; // CR
        table[pos + '-'] = 70;
        table[pos + '_'] = 106;
        table[pos + '+'] = 69;
        table[pos + '!'] = 93;
        table[pos + '/'] = 96;
        table[pos + ','] = 94;
        table[pos + ':'] = 92;
        table[pos + ';'] = 95;
        table[pos + '='] = 109;
        table[pos + 39] = 85;
        table[pos + '('] = 90;
        table[pos + ')'] = 91;
        table[pos + '*'] = 97;

        table[pos + 'A'] = 17;
        table[pos + 'B'] = 72;
        table[pos + 'C'] = 53;
        table[pos + 'D'] = 26;
        table[pos + 'E'] = 5;
        table[pos + 'F'] = 100;
        table[pos + 'H'] = 76;
        table[pos + 'I'] = 98;
        table[pos + 'J'] = 35;
        table[pos + 'M'] = 22;
        table[pos + 'O'] = 112;
        table[pos + 'P'] = 58;
        table[pos + 'R'] = 10;
        table[pos + 'S'] = 2;
        table[pos + 'W'] = 99;
        table[pos + 'X'] = 114;
        table[pos + zeile] = 0; // kein Endzustand
        pos += breite;

        // Zeile 2
        table[pos + 'B'] = 151;
        table[pos + 'E'] = 3;
        table[pos + 'H'] = 66;
        table[pos + 'U'] = 20;
        table[pos + ':'] = 131;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 3
        table[pos + 'G'] = 4;
        table[pos + ':'] = 131;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 4
        table[pos + zeile] = TOKEN_SEG; // SEG
        pos += breite;

        // Zeile 5
        table[pos + 'N'] = 107;
        table[pos + 'Q'] = 6;
        table[pos + 'X'] = 131;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 6
        table[pos + 'U'] = 7;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 7
        table[pos + 'A'] = 8;
        table[pos + zeile] = TOKEN_EQU; // EQU
        pos += breite;

        // Zeile 8
        table[pos + 'L'] = 9;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 9
        table[pos + zeile] = TOKEN_EQU; // EQUAL
        pos += breite;

        // Zeile 10
        table[pos + '0'] = 105;
        table[pos + '1'] = 105;
        table[pos + '2'] = 105;
        table[pos + '3'] = 105;
        table[pos + '4'] = 105;
        table[pos + '5'] = 105;
        table[pos + '6'] = 105;
        table[pos + '7'] = 105;
        table[pos + '8'] = 105;
        table[pos + '9'] = 105;
        table[pos + '1'] = 104;
        table[pos + 'E'] = 11;
        table[pos + 'O'] = 67;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 11
        table[pos + 'S'] = 12;
        table[pos + 'T'] = 57;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 12
        table[pos + 'E'] = 13;
        table[pos + zeile] = TOKEN_RES; // RES
        pos += breite;

        // Zeile 13
        table[pos + 'R'] = 14;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 14
        table[pos + 'V'] = 15;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 15
        table[pos + 'E'] = 16;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 16
        table[pos + zeile] = TOKEN_RES; // RESERVE
        pos += breite;

        // Zeile 17
        table[pos + 'D'] = 18;
        table[pos + 'N'] = 117;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 18
        table[pos + 'D'] = 19;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 19
        table[pos + zeile] = TOKEN_ADD; // ADD
        pos += breite;

        // Zeile 20
        table[pos + 'B'] = 21;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 21
        table[pos + zeile] = TOKEN_SUB; // SUB
        pos += breite;

        // Zeile 22
        table[pos + 'O'] = 29;
        table[pos + 'U'] = 23;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 23
        table[pos + 'L'] = 24;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 24
        table[pos + 'T'] = 25;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 25
        table[pos + zeile] = TOKEN_MULT; // MULT
        pos += breite;

        // Zeile 26
        table[pos + 'D'] = 110;
        table[pos + 'I'] = 27;
        table[pos + zeile] = TOKEN_D; // D
        pos += breite;

        // Zeile 27
        table[pos + 'V'] = 28;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 28
        table[pos + zeile] = TOKEN_DIV; // DIV
        pos += breite;

        // Zeile 29
        table[pos + 'V'] = 30;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 30
        table[pos + 'E'] = 31;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 31
        table[pos + 'A'] = 32;
        table[pos + 'C'] = 33;
        table[pos + 'N'] = 34;
        table[pos + zeile] = TOKEN_MOVE; // MOVE
        pos += breite;

        // Zeile 32
        table[pos + zeile] = TOKEN_MOVEA; // MOVEA
        pos += breite;

        // Zeile 33
        table[pos + zeile] = TOKEN_MOVEC; // MOVEC
        pos += breite;

        // Zeile 34
        table[pos + zeile] = TOKEN_MOVEN; // MOVEN
        pos += breite;

        // Zeile 35
        table[pos + 'B'] = 141;
        table[pos + 'C'] = 46;
        table[pos + 'E'] = 36;
        table[pos + 'G'] = 40;
        table[pos + 'L'] = 43;
        table[pos + 'N'] = 38;
        table[pos + 'U'] = 48;
        table[pos + 'V'] = 51;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 36
        table[pos + 'Q'] = 37;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 37
        table[pos + zeile] = TOKEN_JEQ; // JEQ
        pos += breite;

        // Zeile 38
        table[pos + 'C'] = 47;
        table[pos + 'E'] = 39;
        table[pos + 'V'] = 52;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 39
        table[pos + zeile] = TOKEN_JNE; // JNE
        pos += breite;

        // Zeile 40
        table[pos + 'E'] = 42;
        table[pos + 'T'] = 41;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 41

        table[pos + zeile] = TOKEN_JGT; // JGT
        pos += breite;

        // Zeile 42
        table[pos + zeile] = TOKEN_JGE; // JGE
        pos += breite;

        // Zeile 43
        table[pos + 'E'] = 45;
        table[pos + 'T'] = 44;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 44
        table[pos + zeile] = TOKEN_JLT; // JLT
        pos += breite;

        // Zeile 45
        table[pos + zeile] = TOKEN_JLE; // JLE
        pos += breite;

        // Zeile 46
        table[pos + zeile] = TOKEN_JC; // JC
        pos += breite;

        // Zeile 47
        table[pos + zeile] = TOKEN_JNC; // JNC
        pos += breite;

        // Zeile 48
        table[pos + 'M'] = 49;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 49
        table[pos + 'P'] = 50;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 50
        table[pos + zeile] = TOKEN_JUMP; // JUMP
        pos += breite;

        // Zeile 51
        table[pos + zeile] = TOKEN_JV; // JV
        pos += breite;

        // Zeile 52
        table[pos + zeile] = TOKEN_JNV; // JNV
        pos += breite;

        // Zeile 53
        table[pos + 'A'] = 54;
        table[pos + 'L'] = 122;
        table[pos + 'M'] = 126;
        table[pos + 'O'] = 148;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 54
        table[pos + 'L'] = 55;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 55
        table[pos + 'L'] = 56;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 56
        table[pos + zeile] = TOKEN_CALL; // CALL
        pos += breite;

        // Zeile 57
        table[pos + zeile] = TOKEN_RET; // RET
        pos += breite;

        // Zeile 58
        table[pos + 'O'] = 59;
        table[pos + 'U'] = 62;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 59
        table[pos + 'P'] = 60;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 60
        table[pos + 'R'] = 61;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 61
        table[pos + zeile] = TOKEN_POPR; // POPR
        pos += breite;

        // Zeile 62
        table[pos + 'S'] = 63;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 63
        table[pos + 'H'] = 64;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 64
        table[pos + 'R'] = 65;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 65
        table[pos + zeile] = TOKEN_PUSHR; // PUSHR
        pos += breite;

        // Zeile 66
        table[pos + zeile] = TOKEN_SH; // SH
        pos += breite;

        // Zeile 67
        table[pos + 'T'] = 68;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 68
        table[pos + zeile] = TOKEN_ROT; // ROT
        pos += breite;

        // Zeile 69
        table[pos + zeile] = TOKEN_PLUS; // PLUS
        pos += breite;

        // Zeile 70
        table[pos + '-'] = 88;
        table[pos + zeile] = TOKEN_MINUS; // PLUS
        pos += breite;

        // Zeile 71
        table[pos + '.'] = 80;
        table[pos + 'E'] = 83;
        table[pos + zeile] = TOKEN_NUMBER; // NUMBER
        pos += breite;

        // Zeile 72
        table[pos + 39] = 73;
        table[pos + zeile] = TOKEN_B; // B
        pos += breite;

        // Zeile 73
        table[pos + '0'] = 74;
        table[pos + '1'] = 74;
        table[pos + zeile] = 101; // Error parsing binary number
        pos += breite;

        // Row 74
        table[pos + 39] = 75;
        table[pos + '0'] = 74;
        table[pos + '1'] = 74;
        table[pos + zeile] = 101; // Error parsing binary number
        pos += breite;

        // Row 75
        table[pos + zeile] = TOKEN_BIN; // Binary number
        pos += breite;

        // Zeile 76
        table[pos + 39] = 77;
        table[pos + 'A'] = 128;
        table[pos + zeile] = TOKEN_H; // H
        pos += breite;

        // Zeile 77
        table[pos + zeile] = 102; // Error parsing hex number
        pos += breite;

        // Zeile 78
        table[pos + 39] = 79;
        table[pos + zeile] = 102; // Error parsing hex number
        pos += breite;

        // Zeile 79
        table[pos + zeile] = TOKEN_HEX; // HEX
        pos += breite;

        // Zeile 80
        table[pos + zeile] = 103; // Error parsing float number
        pos += breite;

        // Zeile 81
        table[pos + 'E'] = 83;
        table[pos + zeile] = TOKEN_FLOAT; // Float
        pos += breite;

        // Zeile 82
        table[pos + zeile] = TOKEN_FLOAT; // Float
        pos += breite;

        // Zeile 83
        table[pos + '-'] = 84;
        table[pos + '+'] = 84;
        table[pos + zeile] = 103; // Error parsing float number
        pos += breite;

        // Zeile 84
        table[pos + zeile] = TOKEN_FLOAT; // Float
        pos += breite;

        // Zeile 85
        table[pos + 26] = 0; // EOF
        table[pos + 10] = 0; // LF
        table[pos + 13] = 0; // CR
        table[pos + zeile] = 104; // Error parsing string
        pos += breite;

        // Zeile 86
        table[pos + 26] = 0; // EOF
        table[pos + 10] = 0; // LF
        table[pos + 13] = 0; // CR
        table[pos + 39] = 87;
        table[pos + zeile] = 104; // Error parsing string
        pos += breite;

        // Zeile 87
        table[pos + 39] = 86;
        table[pos + zeile] = TOKEN_STRING; // String
        pos += breite;

        // Zeile 88
        table[pos + 26] = 0; // EOF
        table[pos + 10] = 0; // LF
        table[pos + 13] = 0; // CR
        table[pos + zeile] = TOKEN_COMMENT; // Comment
        pos += breite;

        // Zeile 89
        table[pos + zeile] = TOKEN_COMMENT; // Comment
        pos += breite;

        // Zeile 90
        table[pos + zeile] = TOKEN_BRACKETOPEN; // (
        pos += breite;

        // Zeile 91
        table[pos + zeile] = TOKEN_BRACKETCLOSE; // )
        pos += breite;

        // Zeile 92
        table[pos + zeile] = TOKEN_COLON; // :
        pos += breite;

        // Zeile 93
        table[pos + zeile] = TOKEN_EXCLAMATIONPOINT; // !
        pos += breite;

        // Zeile 94
        table[pos + zeile] = TOKEN_COMMA; // ,
        pos += breite;

        // Zeile 95
        table[pos + zeile] = TOKEN_APOSTROPHE; // ;
        pos += breite;

        // Zeile 96
        table[pos + zeile] = TOKEN_SLASH; // SLASH
        pos += breite;

        // Zeile 97
        table[pos + zeile] = TOKEN_STAR; // *
        pos += breite;

        // Zeile 98
        table[pos + 'N'] = 134;
        table[pos + zeile] = TOKEN_I; // I
        pos += breite;

        // Zeile 99
        table[pos + zeile] = TOKEN_W; // W
        pos += breite;

        // Zeile 100
        table[pos + 'I'] = 136;
        table[pos + zeile] = TOKEN_F; // F
        pos += breite;

        // Zeile 101
        table[pos + zeile] = TOKEN_SPACE; // Whitespace
        pos += breite;

        // Zeile 102
        table[pos + zeile] = TOKEN_NEWLINE; // NEWLINE
        pos += breite;

        // Zeile 103
        table[pos + zeile] = TOKEN_EOF; // EOF
        pos += breite;

        // Zeile 104
        table[pos + '0'] = 105;
        table[pos + '1'] = 105;
        table[pos + '2'] = 105;
        table[pos + '3'] = 105;
        table[pos + '4'] = 105;
        table[pos + '5'] = 105;
        table[pos + zeile] = TOKEN_REGISTER; // REGISTER
        pos += breite;

        // Zeile 105
        table[pos + zeile] = TOKEN_REGISTER; // REGISTER
        pos += breite;

        // Zeile 106
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 107
        table[pos + 'D'] = 108;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 108
        table[pos + zeile] = TOKEN_END; // END
        pos += breite;

        // Zeile 109
        table[pos + zeile] = TOKEN_EQUALSIGN; // ==
        pos += breite;

        // Zeile 110
        table[pos + zeile] = TOKEN_DD; // DD
        pos += breite;

        // Zeile 111
        table[pos + zeile] = 101; // Error - invalid character
        pos += breite;

        // Zeile 112
        table[pos + 'R'] = 113;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 113
        table[pos + zeile] = TOKEN_OR; // OR
        pos += breite;

        // Zeile 114
        table[pos + 'O'] = 115;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 115
        table[pos + 'R'] = 116;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 116
        table[pos + zeile] = TOKEN_XOR; // XOR
        pos += breite;

        // Zeile 117
        table[pos + 'D'] = 118;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 118
        table[pos + 'N'] = 119;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 119
        table[pos + 'O'] = 120;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 120
        table[pos + 'T'] = 121;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 121
        table[pos + zeile] = TOKEN_ANDNOT; // ANDNOT
        pos += breite;

        // Zeile 122
        table[pos + 'E'] = 123;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 123
        table[pos + 'A'] = 124;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 124
        table[pos + 'R'] = 125;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 125
        table[pos + zeile] = TOKEN_CLEAR; // CLEAR
        pos += breite;

        // Zeile 126
        table[pos + 'P'] = 127;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 127
        table[pos + zeile] = TOKEN_CMP; // CMP
        pos += breite;

        // Zeile 128
        table[pos + 'L'] = 129;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 129
        table[pos + 'T'] = 130;
        table[pos + ':'] = 155;
        table[pos + zeile] = TOKEN_NAME; // Name
        pos += breite;

        // Zeile 130
        table[pos + zeile] = TOKEN_HALT; // HALT
        pos += breite;

        // Zeile 131
        table[pos + 'T'] = 132;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 132
        table[pos + 'S'] = 133;
        table[pos + zeile] = TOKEN_EXT; // EXT
        pos += breite;

        // Zeile 133
        table[pos + zeile] = TOKEN_EXTS; // EXTS
        pos += breite;

        // Zeile 134
        table[pos + 'S'] = 135;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 135
        table[pos + zeile] = TOKEN_INS; // INS
        pos += breite;

        // Zeile 136
        table[pos + 'N'] = 137;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 137
        table[pos + 'D'] = 138;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 138
        table[pos + 'C'] = 140;
        table[pos + 'S'] = 139;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 139
        table[pos + zeile] = TOKEN_FINDS; // FINDS
        pos += breite;

        // Zeile 140
        table[pos + zeile] = TOKEN_FINDC; // FINDC
        pos += breite;

        // Zeile 141
        table[pos + 'C'] = 145;
        table[pos + 'S'] = 142;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 142
        table[pos + 'S'] = 143;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 143
        table[pos + 'I'] = 144;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 144
        table[pos + zeile] = TOKEN_JBSSI; // JBSSI
        pos += breite;

        // Zeile 145
        table[pos + 'C'] = 146;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 146
        table[pos + 'I'] = 147;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 147
        table[pos + zeile] = TOKEN_JBCCI; // JBCCI
        pos += breite;

        // Zeile 148
        table[pos + 'N'] = 149;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 149
        table[pos + 'V'] = 150;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 150
        table[pos + zeile] = TOKEN_CONV; // CONV
        pos += breite;

        // Zeile 151
        table[pos + 'P'] = 152;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 152
        table[pos + 'S'] = 153;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 153
        table[pos + 'W'] = 154;
        table[pos + zeile] = TOKEN_NAME; // NAME
        pos += breite;

        // Zeile 154
        table[pos + zeile] = TOKEN_SBPSW; // SBPSW
        pos += breite;

        // Zeile 155
        table[pos + zeile] = TOKEN_LABEL; // LABEL

    }

}
