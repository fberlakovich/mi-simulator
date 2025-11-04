package core;

import enviroment.Enviroment;
import enviroment.NumberConversion;
import simulator.Command;
import simulator.Halt;

/**
 * Core execution engine for MI programs that can run without GUI.
 * This class handles the execution logic without any GUI dependencies.
 */
public class ProgramExecutor {

    /**
     * Listener for execution state changes
     */
    public interface ExecutionStateListener {
        /**
         * Called when program execution starts
         */
        void onExecutionStart();

        /**
         * Called when program execution stops
         *
         * @param reason The reason for stopping (halt, breakpoint, error, user stop)
         */
        void onExecutionStop(StopReason reason);

        /**
         * Called after each command is executed
         *
         * @param command The command that was executed
         */
        void onCommandExecuted(Command command);
    }

    /**
     * Reason for program execution stop
     */
    public enum StopReason {
        HALT,           // Program halted normally
        BREAKPOINT,     // Hit a breakpoint
        USER_STOP,      // User requested stop
        ERROR,          // Error occurred
        END_OF_PROGRAM  // No more commands
    }

    private boolean stopRequested = false;
    private ExecutionStateListener listener;

    /**
     * Creates a new program executor
     *
     * @param listener Optional listener for execution state changes (can be null)
     */
    public ProgramExecutor(ExecutionStateListener listener) {
        this.listener = listener;
    }

    /**
     * Creates a new program executor without a listener
     */
    public ProgramExecutor() {
        this(null);
    }

    /**
     * Executes the program until it halts, hits a breakpoint, or is stopped
     *
     * @return The reason execution stopped
     */
    public StopReason executeProgram() {
        stopRequested = false;

        if (listener != null) {
            listener.onExecutionStart();
        }

        Command next = Enviroment.getNextCommand();
        StopReason stopReason = StopReason.END_OF_PROGRAM;

        while (next != null && !stopRequested) {
            next.run();

            if (listener != null) {
                listener.onCommandExecuted(next);
            }

            if (next instanceof Halt) {
                stopReason = StopReason.HALT;
                break;
            }

            next = Enviroment.readNextCommand();

            if (next != null) {
                Command next_precode = Enviroment.getCommandperAddress(next.getAdress());
                if (next_precode != null &&
                    NumberConversion.myByteEqual(next.getOpCode(), next_precode.getOpCode())) {
                    if (next_precode.hasBreakPoint()) {
                        stopReason = StopReason.BREAKPOINT;
                        break;
                    }
                }
            }
        }

        if (stopRequested) {
            stopReason = StopReason.USER_STOP;
        }

        if (listener != null) {
            listener.onExecutionStop(stopReason);
        }

        return stopReason;
    }

    /**
     * Executes a single command (step execution)
     *
     * @return The command that was executed, or null if there are no more commands
     */
    public Command executeSingleCommand() {
        Command next = Enviroment.getNextCommand();
        if (next != null) {
            next.run();
            Enviroment.readNextCommand();

            if (listener != null) {
                listener.onCommandExecuted(next);
            }
        }
        return next;
    }

    /**
     * Requests that program execution be stopped
     */
    public void stop() {
        stopRequested = true;
    }

    /**
     * Checks if execution has been requested to stop
     *
     * @return true if stop was requested
     */
    public boolean isStopRequested() {
        return stopRequested;
    }
}
