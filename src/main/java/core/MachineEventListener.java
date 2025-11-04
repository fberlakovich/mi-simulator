package core;

/**
 * Interface for receiving notifications about machine state changes and errors.
 * Implementations can update UI, log messages, or perform other actions.
 */
public interface MachineEventListener {
    
    /**
     * Called when an error occurs during program execution.
     *
     * @param message The error message
     * @param title The error title
     */
    void onError(String message, String title);
    
    /**
     * Called when a memory access error occurs.
     *
     * @param address The invalid memory address
     */
    void onMemoryAccessError(int address);
    
    /**
     * Called when memory manipulation is detected.
     */
    void onMemoryManipulation();
    
    /**
     * Called when the program should be stopped.
     */
    void onProgramStop();
    
    /**
     * Called when the machine state should be updated/refreshed.
     */
    void onStateUpdate();
}
