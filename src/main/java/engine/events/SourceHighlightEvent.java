package engine.events;

/**
 * Event emitted when the current execution line changes.
 *
 * Replaces Enviroment.getText().highlightNextCommand() calls.
 * IDE integrations can use this to update editor markers.
 */
public class SourceHighlightEvent extends MachineEvent {

    private final int lineNumber;
    private final int programCounter;
    private final String decodedInstruction;

    public SourceHighlightEvent(int lineNumber, int programCounter, String decodedInstruction) {
        super();
        this.lineNumber = lineNumber;
        this.programCounter = programCounter;
        this.decodedInstruction = decodedInstruction;
    }

    /**
     * @return The source line number to highlight (1-indexed)
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return The current program counter value
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * @return The decoded instruction at this address
     */
    public String getDecodedInstruction() {
        return decodedInstruction;
    }

    @Override
    public String toString() {
        return String.format("SourceHighlightEvent{line=%d, pc=0x%08X, instr='%s'}",
                lineNumber, programCounter, decodedInstruction);
    }
}
