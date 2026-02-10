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
    private int tokenTypeColumn;

    /** Table width (columns) */
    private int tableWidth;

    /** Current position in table setup */
    private int pos;

    /** Input source text */
    private String input;

    /** EQU definitions for macro substitution */
    private ArrayList<Equal> equDefinitions = new ArrayList<Equal>();

    /** Iterator for current EQU expansion */
    private Iterator<Token> iter = null;

    /** Input as byte array */
    private byte[] b;

    /** Current position in input */
    private int position = 0;

    /** Previous position (start of current token) */
    private int tokenStart = 0;

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

    // =====================================================
    // State Transition Table Helper Methods
    // =====================================================

    /**
     * Sets a single character transition in the state table.
     */
    private void setTransition(int stateNum, int charCode, int targetState) {
        table[stateNum * tableWidth + charCode] = targetState;
    }

    /**
     * Sets transitions for a character range in a single state.
     */
    private void setRangeTransition(int stateNum, char fromCh, char toCh, int targetState) {
        for (int j = fromCh; j <= toCh; j++) {
            table[stateNum * tableWidth + j] = targetState;
        }
    }

    /**
     * Sets the token type for an accepting state.
     */
    private void setTokenType(int stateNum, int tokenType) {
        table[stateNum * tableWidth + tokenTypeColumn] = tokenType;
    }

    /**
     * Fills all 256 character transitions for a state with a single target.
     */
    private void fillState(int stateNum, int targetState) {
        for (int j = 0; j <= 255; j++) {
            table[stateNum * tableWidth + j] = targetState;
        }
    }

    /**
     * Sets alphanumeric transitions (A-Z, a-z, 0-9, _) for a range of states.
     */
    private void setAlphanumericTransitions(int startState, int endState, int targetState) {
        for (int i = startState; i <= endState; i++) {
            setRangeTransition(i, 'A', 'Z', targetState);
            setRangeTransition(i, 'a', 'z', targetState);
            setRangeTransition(i, '0', '9', targetState);
            setTransition(i, '_', targetState);
        }
    }

    /**
     * Sets digit transitions (0-9) for multiple states to a single target.
     */
    private void setDigitTransitions(int[] states, int targetState) {
        for (int stateNum : states) {
            setRangeTransition(stateNum, '0', '9', targetState);
        }
    }

    /**
     * Sets hex digit transitions (A-F, 0-9) for multiple states to a single target.
     */
    private void setHexDigitTransitions(int[] states, int targetState) {
        for (int stateNum : states) {
            setRangeTransition(stateNum, 'A', 'F', targetState);
            setRangeTransition(stateNum, '0', '9', targetState);
        }
    }

    /**
     * Sets a state row with transitions and token type, then advances pos.
     * @param transitions pairs of (char, targetState), e.g. 'A', 17, 'B', 72
     * @param tokenType the token type for this accepting state (0 = non-accepting)
     */
    private void initState(int tokenType, int... transitions) {
        for (int i = 0; i < transitions.length; i += 2) {
            table[pos + transitions[i]] = transitions[i + 1];
        }
        table[pos + tokenTypeColumn] = tokenType;
        pos += tableWidth;
    }

    /**
     * Sets a state row for a NAME with label possibility (colon -> 155).
     */
    private void initNameState(int... transitions) {
        for (int i = 0; i < transitions.length; i += 2) {
            table[pos + transitions[i]] = transitions[i + 1];
        }
        table[pos + ':'] = 155;
        table[pos + tokenTypeColumn] = TOKEN_NAME;
        pos += tableWidth;
    }

    /**
     * Sets a simple accepting state with just a token type.
     */
    private void initAcceptState(int tokenType) {
        table[pos + tokenTypeColumn] = tokenType;
        pos += tableWidth;
    }

    // State table initialization helper methods (split from init() to reduce method length)

    private void initStateTableStructure() {
        // Create state transition table (257 columns x 157 rows)
        table = new int[(256 + 1) * (157)];
        tableWidth = 257;
        tokenTypeColumn = 256;

        // Default: transition to error state 111
        for (int j = 0; j <= 255; j++) {
            table[tableWidth + j] = 111;
        }
    }

    private void initBaseTransitions() {
        // Initialize NAME state transitions (state 106)
        setAlphanumericTransitions(1, 70, 106);
        setAlphanumericTransitions(98, 100, 106);
        setAlphanumericTransitions(106, 108, 106);
        setAlphanumericTransitions(112, 154, 106);
        for (int state : new int[]{72, 76, 110}) {
            setAlphanumericTransitions(state, state, 106);
        }

        // STRING state transitions
        fillState(85, 86);
        fillState(86, 86);

        // COMMENT state transitions
        fillState(88, 88);

        // HEX number transitions
        setHexDigitTransitions(new int[]{77, 78}, 78);

        // NUMBER transitions
        setDigitTransitions(new int[]{1, 69, 70, 71}, 71);

        // FLOAT transitions
        setDigitTransitions(new int[]{80, 81}, 81);
        setDigitTransitions(new int[]{83, 84}, 84);
    }

    private void initStartState() {
        // State 1 - Start state
        pos = tableWidth;
        table[pos + 0] = 101;     // #0 -> whitespace
        table[pos + 9] = 101;     // TAB
        table[pos + 26] = 103;    // EOF
        table[pos + 10] = 102;    // LF
        table[pos + 13] = 102;    // CR
        table[pos + 32] = 101;    // Space
        table[pos + '-'] = 70;
        table[pos + '_'] = 106;
        table[pos + '+'] = 69;
        table[pos + '!'] = 93;
        table[pos + '/'] = 96;
        table[pos + ','] = 94;
        table[pos + ':'] = 92;
        table[pos + ';'] = 95;
        table[pos + '='] = 109;
        table[pos + 39] = 85;     // Single quote -> string
        table[pos + '('] = 90;
        table[pos + ')'] = 91;
        table[pos + '*'] = 97;
        // Letter transitions to keyword start states
        table[pos + 'A'] = 17;    // ADD, ANDNOT
        table[pos + 'B'] = 72;    // B (type)
        table[pos + 'C'] = 53;    // CALL, CLEAR, CMP, CONV
        table[pos + 'D'] = 26;    // D (type), DD, DIV
        table[pos + 'E'] = 5;     // END, EQU, EXT, EXTS
        table[pos + 'F'] = 100;   // F (type), FINDC, FINDS
        table[pos + 'H'] = 76;    // H (type), HALT
        table[pos + 'I'] = 98;    // I (immediate), INS
        table[pos + 'J'] = 35;    // Jump instructions
        table[pos + 'M'] = 22;    // MOVE, MULT
        table[pos + 'O'] = 112;   // OR
        table[pos + 'P'] = 58;    // POPR, PUSHR
        table[pos + 'R'] = 10;    // R (register), RES, RET, ROT
        table[pos + 'S'] = 2;     // SEG, SH, SUB, SBPSW
        table[pos + 'W'] = 99;    // W (type)
        table[pos + 'X'] = 114;   // XOR
        table[pos + tokenTypeColumn] = 0;
        pos += tableWidth;
    }

    private void initSegEquResStates() {
        // States 2-16: SEG, EQU, RES, RESERVE keywords
        initState(TOKEN_NAME, 'B', 151, 'E', 3, 'H', 66, 'U', 20, ':', 131);  // S -> SBPSW, SEG, SH, SUB
        initNameState('G', 4);                                                  // SE
        initAcceptState(TOKEN_SEG);                                             // SEG
        initNameState('N', 107, 'Q', 6, 'X', 131);                              // E -> END, EQU, EXT
        initNameState('U', 7);                                                  // EQ
        initState(TOKEN_EQU, 'A', 8);                                           // EQU (also EQUAL)
        initNameState('L', 9);                                                  // EQUA
        initAcceptState(TOKEN_EQU);                                             // EQUAL

        // R states (10-16): RES, RESERVE, RET, ROT, Register
        table[pos + '0'] = 105; table[pos + '1'] = 105; table[pos + '2'] = 105;
        table[pos + '3'] = 105; table[pos + '4'] = 105; table[pos + '5'] = 105;
        table[pos + '6'] = 105; table[pos + '7'] = 105; table[pos + '8'] = 105;
        table[pos + '9'] = 105; table[pos + '1'] = 104;
        table[pos + 'E'] = 11; table[pos + 'O'] = 67; table[pos + ':'] = 155;
        table[pos + tokenTypeColumn] = TOKEN_NAME;
        pos += tableWidth;

        initNameState('S', 12, 'T', 57);                // RE -> RES, RET
        initState(TOKEN_RES, 'E', 13);                  // RES (also RESERVE)
        initNameState('R', 14);                         // RESE
        initNameState('V', 15);                         // RESER
        initNameState('E', 16);                         // RESERV
        initAcceptState(TOKEN_RES);                     // RESERVE
    }

    private void initArithmeticMoveStates() {
        // States 17-34: ADD, SUB, MULT, DIV, MOVE variants
        initNameState('D', 18, 'N', 117);              // A -> ADD, ANDNOT
        initNameState('D', 19);                        // AD
        initAcceptState(TOKEN_ADD);                    // ADD
        initNameState('B', 21);                        // SU
        initAcceptState(TOKEN_SUB);                    // SUB
        initNameState('O', 29, 'U', 23);              // M -> MOVE, MULT
        initNameState('L', 24);                        // MU
        initNameState('T', 25);                        // MUL
        initAcceptState(TOKEN_MULT);                   // MULT
        initState(TOKEN_D, 'D', 110, 'I', 27);        // D -> D(type), DD, DIV
        initNameState('V', 28);                        // DI
        initAcceptState(TOKEN_DIV);                    // DIV
        initNameState('V', 30);                        // MO
        initNameState('E', 31);                        // MOV
        initState(TOKEN_MOVE, 'A', 32, 'C', 33, 'N', 34);  // MOVE -> MOVEA, MOVEC, MOVEN
        initAcceptState(TOKEN_MOVEA);                  // MOVEA
        initAcceptState(TOKEN_MOVEC);                  // MOVEC
        initAcceptState(TOKEN_MOVEN);                  // MOVEN
    }

    private void initJumpStates() {
        // States 35-52: All jump instructions
        initNameState('B', 141, 'C', 46, 'E', 36, 'G', 40, 'L', 43, 'N', 38, 'U', 48, 'V', 51);
        initNameState('Q', 37);                        // JE
        initAcceptState(TOKEN_JEQ);                    // JEQ
        initNameState('C', 47, 'E', 39, 'V', 52);     // JN -> JNC, JNE, JNV
        initAcceptState(TOKEN_JNE);                    // JNE
        initNameState('E', 42, 'T', 41);              // JG -> JGE, JGT
        initAcceptState(TOKEN_JGT);                    // JGT
        initAcceptState(TOKEN_JGE);                    // JGE
        initNameState('E', 45, 'T', 44);              // JL -> JLE, JLT
        initAcceptState(TOKEN_JLT);                    // JLT
        initAcceptState(TOKEN_JLE);                    // JLE
        initAcceptState(TOKEN_JC);                     // JC
        initAcceptState(TOKEN_JNC);                    // JNC
        initNameState('M', 49);                        // JU
        initNameState('P', 50);                        // JUM
        initAcceptState(TOKEN_JUMP);                   // JUMP
        initAcceptState(TOKEN_JV);                     // JV
        initAcceptState(TOKEN_JNV);                    // JNV
    }

    private void initCallRetPushPopStates() {
        // States 53-68: CALL, CMP, CLEAR, CONV, POPR, PUSHR, SH, ROT
        initNameState('A', 54, 'L', 122, 'M', 126, 'O', 148);  // C -> CALL, CLEAR, CMP, CONV
        initNameState('L', 55);                        // CA
        initNameState('L', 56);                        // CAL
        initAcceptState(TOKEN_CALL);                   // CALL
        initAcceptState(TOKEN_RET);                    // RET
        initNameState('O', 59, 'U', 62);              // P -> POPR, PUSHR
        initNameState('P', 60);                        // PO
        initNameState('R', 61);                        // POP
        initAcceptState(TOKEN_POPR);                   // POPR
        initNameState('S', 63);                        // PU
        initNameState('H', 64);                        // PUS
        initNameState('R', 65);                        // PUSH
        initAcceptState(TOKEN_PUSHR);                  // PUSHR
        initAcceptState(TOKEN_SH);                     // SH
        initNameState('T', 68);                        // RO
        initAcceptState(TOKEN_ROT);                    // ROT
    }

    private void initNumberStates() {
        // States 69-84: NUMBER, FLOAT, BIN, HEX
        initAcceptState(TOKEN_PLUS);                   // +
        initState(TOKEN_MINUS, '-', 88);              // - (comment if --)
        initState(TOKEN_NUMBER, '.', 80, 'E', 83);    // Number, float continues
        initState(TOKEN_B, 39, 73);                   // B -> B' (binary)
        initState(101, '0', 74, '1', 74);             // B' -> binary digits
        initState(101, 39, 75, '0', 74, '1', 74);     // Binary digits
        initAcceptState(TOKEN_BIN);                    // Binary number complete
        initState(TOKEN_H, 39, 77, 'A', 128);         // H -> H' (hex) or HALT
        initState(102);                                // H' error state
        initState(102, 39, 79);                       // Hex digits, ' ends
        initAcceptState(TOKEN_HEX);                    // Hex number complete
        initState(103);                                // Float mantissa error
        initState(TOKEN_FLOAT, 'E', 83);              // Float mantissa ok
        initAcceptState(TOKEN_FLOAT);                  // Float complete
        initState(103, '-', 84, '+', 84);             // Float exponent sign
        initAcceptState(TOKEN_FLOAT);                  // Float exponent digits
    }

    private void initStringCommentPunctuationStates() {
        // States 85-97: STRING, COMMENT, punctuation
        initState(104, 26, 0, 10, 0, 13, 0);          // String start (error on EOF/newline)
        initState(104, 26, 0, 10, 0, 13, 0, 39, 87);  // String content
        initState(TOKEN_STRING, 39, 86);              // String quote (escaped or end)
        initState(TOKEN_COMMENT, 26, 0, 10, 0, 13, 0); // Comment
        initAcceptState(TOKEN_COMMENT);                // Comment end
        initAcceptState(TOKEN_BRACKETOPEN);            // (
        initAcceptState(TOKEN_BRACKETCLOSE);           // )
        initAcceptState(TOKEN_COLON);                  // :
        initAcceptState(TOKEN_EXCLAMATIONPOINT);       // !
        initAcceptState(TOKEN_COMMA);                  // ,
        initAcceptState(TOKEN_APOSTROPHE);             // ;
        initAcceptState(TOKEN_SLASH);                  // /
        initAcceptState(TOKEN_STAR);                   // *
    }

    private void initTypeAndSpecialStates() {
        // States 98-111: I, W, F types, whitespace, register, NAME, END, errors
        initState(TOKEN_I, 'N', 134);                 // I -> I or INS
        initAcceptState(TOKEN_W);                      // W
        initState(TOKEN_F, 'I', 136);                 // F -> F or FIND*
        initAcceptState(TOKEN_SPACE);                  // Whitespace
        initAcceptState(TOKEN_NEWLINE);                // Newline
        initAcceptState(TOKEN_EOF);                    // EOF

        // R + digit states
        table[pos + '0'] = 105; table[pos + '1'] = 105; table[pos + '2'] = 105;
        table[pos + '3'] = 105; table[pos + '4'] = 105; table[pos + '5'] = 105;
        table[pos + tokenTypeColumn] = TOKEN_REGISTER;
        pos += tableWidth;

        initAcceptState(TOKEN_REGISTER);               // R + 2 digits
        initState(TOKEN_NAME, ':', 155);              // NAME with label
        initNameState('D', 108);                       // EN
        initAcceptState(TOKEN_END);                    // END
        initAcceptState(TOKEN_EQUALSIGN);              // =
        initAcceptState(TOKEN_DD);                     // DD
        initState(101);                                // Error state
    }

    private void initLogicalAndBitfieldStates() {
        // States 112-155: OR, XOR, ANDNOT, CLEAR, CMP, HALT, EXT, INS, FIND, JB*, CONV, SBPSW
        initNameState('R', 113);                       // O -> OR
        initAcceptState(TOKEN_OR);                     // OR
        initNameState('O', 115);                       // X -> XOR
        initNameState('R', 116);                       // XO
        initAcceptState(TOKEN_XOR);                    // XOR
        initNameState('D', 118);                       // AN
        initNameState('N', 119);                       // AND
        initNameState('O', 120);                       // ANDN
        initNameState('T', 121);                       // ANDNO
        initAcceptState(TOKEN_ANDNOT);                 // ANDNOT
        initNameState('E', 123);                       // CL
        initNameState('A', 124);                       // CLE
        initNameState('R', 125);                       // CLEA
        initAcceptState(TOKEN_CLEAR);                  // CLEAR
        initNameState('P', 127);                       // CM
        initAcceptState(TOKEN_CMP);                    // CMP
        initNameState('L', 129);                       // HA
        initNameState('T', 130);                       // HAL
        initAcceptState(TOKEN_HALT);                   // HALT

        // EXT, EXTS states
        initState(TOKEN_NAME, 'T', 132);              // EX -> EXT
        initState(TOKEN_EXT, 'S', 133);               // EXT -> EXTS
        initAcceptState(TOKEN_EXTS);                   // EXTS

        // INS states
        initState(TOKEN_NAME, 'S', 135);              // IN
        initAcceptState(TOKEN_INS);                    // INS

        // FIND* states
        initState(TOKEN_NAME, 'N', 137);              // FI
        initState(TOKEN_NAME, 'D', 138);              // FIN
        initState(TOKEN_NAME, 'C', 140, 'S', 139);   // FIND -> FINDC, FINDS
        initAcceptState(TOKEN_FINDS);                  // FINDS
        initAcceptState(TOKEN_FINDC);                  // FINDC

        // JB* states
        initState(TOKEN_NAME, 'C', 145, 'S', 142);   // JB -> JBCCI, JBSSI
        initState(TOKEN_NAME, 'S', 143);              // JBS
        initState(TOKEN_NAME, 'I', 144);              // JBSS
        initAcceptState(TOKEN_JBSSI);                  // JBSSI
        initState(TOKEN_NAME, 'C', 146);              // JBC
        initState(TOKEN_NAME, 'I', 147);              // JBCC
        initAcceptState(TOKEN_JBCCI);                  // JBCCI

        // CONV states
        initState(TOKEN_NAME, 'N', 149);              // CO
        initState(TOKEN_NAME, 'V', 150);              // CON
        initAcceptState(TOKEN_CONV);                   // CONV

        // SBPSW states
        initState(TOKEN_NAME, 'P', 152);              // SB
        initState(TOKEN_NAME, 'S', 153);              // SBP
        initState(TOKEN_NAME, 'W', 154);              // SBPS
        initAcceptState(TOKEN_SBPSW);                  // SBPSW

        // LABEL state
        initAcceptState(TOKEN_LABEL);                  // Label (NAME:)
    }

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

    public final static int TOKEN_ERROR = 100;

    /** Error messages for scanner errors */
    public static final String[] SCANNER_ERRORS = new String[]{
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
        for (Equal eq : equDefinitions) {
            if (name.equals(eq.getName())) {
                equDefinitions.remove(eq);
            }
        }
        equDefinitions.add(equal);
    }

    /**
     * Gets the token iterator for an EQU definition.
     *
     * @param name the EQU name
     * @return token iterator, or null if not found
     */
    public Iterator<Token> getEqual(String name) {
        for (Equal eq : equDefinitions) {
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

        Token ret = scanNextToken();

        // Skip comments and whitespace unless scanning for highlighting
        if (!highlight) {
            while (ret.getNr() == TOKEN_SPACE || ret.getNr() == TOKEN_COMMENT) {
                ret = scanNextToken();
            }
        }
        return ret;
    }

    /**
     * Scans and returns the next token using the state machine.
     *
     * @return the next token
     */
    private Token scanNextToken() {
        while (true) {
            ch = b[position];
            if (ch < 0) {
                ch = ch + 128;
            }

            // Get next state from transition table
            state = table[state * tableWidth + ch];

            // No follow state -> token recognized
            if (state == 0) {
                if (token != 0) {
                    last++;
                    state = 1;
                    position = last;
                    if (token < 100) {
                        // System.out.println("Token: " + (token) + " "
                        // + input.substring(tokenStart, position));
                        Token tok = new Token(token, input.substring(tokenStart,
                                position),
                                line, tokenStart, position);

                        tokenStart = position;
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

                        // System.out.println("Token: " + (SCANNER_ERRORS[token - 100])
                        // + " " + input.substring(tokenStart, position));
                        Token tok = new Token(100, SCANNER_ERRORS[token - 100],
                                line, tokenStart, position);
                        tokenStart = position;

                        token = 0;
                        return tok;
                    }

                }
            } else {
                int h = table[state * tableWidth + tokenTypeColumn];

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
        return new Token(TOKEN_EOF, "Ende", line, tokenStart, position);
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

        // Normalize line endings and prepare input
        input = s.replaceAll("\r", "");
        b = (input + " ").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        b[b.length - 1] = 0x1A; // EOF marker

        // Initialize state transition table
        initStateTableStructure();
        initBaseTransitions();

        // Initialize state rows
        initStartState();
        initSegEquResStates();
        initArithmeticMoveStates();
        initJumpStates();
        initCallRetPushPopStates();
        initNumberStates();
        initStringCommentPunctuationStates();
        initTypeAndSpecialStates();
        initLogicalAndBitfieldStates();
    }

}
