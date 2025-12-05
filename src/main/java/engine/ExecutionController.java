package engine;

/**
 * Interface for controlling program execution.
 *
 * This abstraction allows the core library to request execution control
 * (like stopping the program) without depending on GUI classes.
 *
 * Implementations:
 * - GUI: Window.RunProgram.stopProgram()
 * - CLI: MIMachine.halt flag
 * - Test: NoOpExecutionController
 */
public interface ExecutionController {

    /**
     * Start program execution.
     * Must be called explicitly to begin running the program.
     */
    void startProgram();

    /**
     * Request the program to stop execution.
     */
    void stopProgram();

    /**
     * Check if the program is currently running.
     *
     * @return true if program is executing
     */
    boolean isRunning();

}
