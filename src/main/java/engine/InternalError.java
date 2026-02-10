package engine;

/**
 * Exception thrown when the engine encounters an internal error that should never happen
 * in correct operation. These typically indicate programming errors or corrupted state.
 *
 * Examples:
 * - Invalid opcode calculation (should be validated during parsing)
 * - Invalid register access length (should be checked by caller)
 * - Attempting to execute data definitions
 */
public class InternalError extends RuntimeException {

    public InternalError(String message) {
        super(message);
    }

    public InternalError(String message, Throwable cause) {
        super(message, cause);
    }
}
