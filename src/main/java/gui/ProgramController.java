package gui;

import engine.Machine;
import engine.MachineContext;
import engine.ProgramRunner;
import engine.events.ExecutionEvent;
import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryAccessErrorEvent;
import engine.parser.Parser;
import engine.program.Program;
import engine.scanner.Scanner;
import engine.state.MyByte;
import engine.util.MemoryChangeTracker;

import engine.program.Label;

import java.util.ArrayList;

/**
 * Controller for program execution operations.
 * Handles assemble, run, step, stop, and restart logic.
 */
public class ProgramController {

    /**
     * Callback interface for execution events.
     */
    public interface ExecutionCallback {
        void onAssembleSuccess(Program program, ArrayList<Label> labels);
        void onAssembleFailure(String errorMessage);
        void onExecutionStarted();
        void onExecutionStopped();
        void onBreakpointHit();
        void onProgramEnded();
        void onStepCompleted(boolean hasMore);
        void onMemoryAccessError(int address, String type);
    }

    private final MachineContext machine;
    private final MemoryChangeTracker memoryTracker;
    private final ExecutionCallback callback;
    private ProgramRunner runner;
    private boolean listenerSetup = false;

    public ProgramController(MachineContext machine, MemoryChangeTracker memoryTracker, ExecutionCallback callback) {
        this.machine = machine;
        this.memoryTracker = memoryTracker;
        this.callback = callback;
    }

    /**
     * Assembles and compiles the source code.
     *
     * @param sourceCode the assembly source code
     * @return the compilation result, or null if parsing failed
     */
    public AssembleResult assemble(String sourceCode) {
        machine.reset();
        Scanner scanner = new Scanner(false);
        scanner.init(sourceCode);

        Parser parser = new Parser((Machine) machine, scanner);
        parser.start();

        Program program = parser.getProgramm();
        String errorMessage = parser.getErrorMeassge().getErrorMessage();

        if (parser.eval()) {
            if (program.compile()) {
                runner = machine.createRunner();
                runner.readNextCommand();
                setupExecutionListener();
                callback.onAssembleSuccess(program, parser.getLabels());
                return new AssembleResult(true, program, parser.getLabels(), null);
            } else {
                callback.onAssembleFailure(CONSTANTS.ASSEMBLE_UNSUCCESSFUL);
                return new AssembleResult(false, program, null, CONSTANTS.ASSEMBLE_UNSUCCESSFUL);
            }
        } else {
            String msg = errorMessage.length() > 0 ? errorMessage : parser.getErrorMeassge().toString();
            callback.onAssembleFailure(msg);
            return new AssembleResult(false, program, null, msg);
        }
    }

    /**
     * Starts continuous program execution.
     */
    public void run() {
        resetTrackers();
        runner = machine.createRunner();
        runner.startProgram();
    }

    /**
     * Executes a single instruction.
     *
     * @return true if there are more instructions to execute
     */
    public boolean step() {
        resetTrackers();

        if (runner == null || !runner.isAlive()) {
            runner = machine.createRunner();
        }
        boolean hasMore = runner.step();
        callback.onStepCompleted(hasMore);
        return hasMore;
    }

    /**
     * Stops program execution.
     */
    public void stop() {
        if (runner != null) {
            runner.stopProgram();
        }
    }

    /**
     * Restarts the program from the beginning.
     */
    public void restart() {
        if (runner != null) {
            runner.stopProgram();
        }
        Program savedProgram = machine.getProgram();
        machine.reset();
        machine.setProgram(savedProgram);
        machine.getMemory().setContent(0, MyByte.fromByteArray(savedProgram.encode()));
        runner = machine.createRunner();
        runner.readNextCommand();
    }

    /**
     * Gets the current program runner.
     */
    public ProgramRunner getRunner() {
        return runner;
    }

    /**
     * Clears the runner reference (e.g., when text changes).
     */
    public void clearRunner() {
        if (runner != null) {
            runner.stopProgram();
            runner = null;
        }
    }

    private void resetTrackers() {
        if (memoryTracker != null) {
            memoryTracker.reset();
        }
    }

    private void setupExecutionListener() {
        if (listenerSetup) {
            return; // Only setup once
        }
        listenerSetup = true;

        machine.getEventBus().subscribe(ExecutionEvent.class, new MachineEventListener() {
            @Override
            public void onEvent(MachineEvent event) {
                if (event instanceof ExecutionEvent) {
                    ExecutionEvent execEvent = (ExecutionEvent) event;
                    handleExecutionEvent(execEvent);
                }
            }
        });

        machine.getEventBus().subscribe(MemoryAccessErrorEvent.class, new MachineEventListener() {
            @Override
            public void onEvent(MachineEvent event) {
                if (event instanceof MemoryAccessErrorEvent) {
                    MemoryAccessErrorEvent errorEvent = (MemoryAccessErrorEvent) event;
                    callback.onMemoryAccessError(errorEvent.getAddress(), errorEvent.getType().toString());
                }
            }
        });
    }

    private void handleExecutionEvent(ExecutionEvent event) {
        switch (event.getType()) {
            case STARTED:
                callback.onExecutionStarted();
                break;
            case STOPPED:
                callback.onExecutionStopped();
                break;
            case BREAKPOINT_HIT:
                callback.onBreakpointHit();
                break;
            case PROGRAM_ENDED:
                callback.onProgramEnded();
                break;
            case STEP_COMPLETED:
                // Handled separately by step() method
                break;
        }
    }

    /**
     * Result of an assemble operation.
     */
    public static class AssembleResult {
        private final boolean success;
        private final Program program;
        private final ArrayList<Label> labels;
        private final String errorMessage;

        public AssembleResult(boolean success, Program program, ArrayList<Label> labels, String errorMessage) {
            this.success = success;
            this.program = program;
            this.labels = labels;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public Program getProgram() {
            return program;
        }

        public ArrayList<Label> getLabels() {
            return labels;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
