package engine.parser;

import static engine.ErrorMessages.ERROR_PARSER;

/**
 * Collects parser error messages during assembly.
 */
public class ErrorMessage {

    /** Accumulated error message string */
    private String message = "";

    /**
     * Appends a parser error with line number.
     *
     * @param line  the source line number
     * @param error the error description
     */
    public void append(int line, String error) {
        message += ERROR_PARSER + " " + line + ": " + error + "\n";
    }

    /**
     * Appends an error message.
     *
     * @param s the error message
     */
    public void append(String s) {
        message += s + "\n";
    }

    /**
     * Gets the accumulated error message.
     *
     * @return the error message string
     */
    public String getErrorMessage() {
        return message;
    }
}
