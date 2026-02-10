package engine.events;

/**
 * Event emitted when a program is loaded into the machine.
 *
 * Frontends should subscribe to this event to update their displays
 * when a new program is loaded.
 */
public class ProgramLoadedEvent extends MachineEvent {

    private final String programName;
    private final int instructionCount;

    /**
     * Creates a new ProgramLoadedEvent.
     *
     * @param programName      the name of the loaded program
     * @param instructionCount the number of instructions in the program
     */
    public ProgramLoadedEvent(String programName, int instructionCount) {
        super();
        this.programName = programName;
        this.instructionCount = instructionCount;
    }

    /**
     * @return the name of the loaded program
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * @return the number of instructions in the program
     */
    public int getInstructionCount() {
        return instructionCount;
    }

    @Override
    public String toString() {
        return String.format("ProgramLoadedEvent{program='%s', instructions=%d}",
                programName, instructionCount);
    }
}
