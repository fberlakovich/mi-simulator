package core;

/**
 * No-op implementation of MachineEventListener for headless execution.
 */
public class HeadlessMachineEventListener implements MachineEventListener {
    
    @Override
    public void onError(String message, String title) {
        // In headless mode, errors can be thrown as exceptions or ignored
        System.err.println(title + ": " + message);
    }
    
    @Override
    public void onMemoryAccessError(int address) {
        System.err.println(Constants.ERROR_MEMORY_TEXT + address);
    }
    
    @Override
    public void onMemoryManipulation() {
        System.err.println(Constants.ERROR_MANIPULATION_MEMORY);
    }
    
    @Override
    public void onProgramStop() {
        // No-op in headless mode
    }
    
    @Override
    public void onStateUpdate() {
        // No-op in headless mode
    }
}
