package gui;

/**
 * Holds GUI-specific state that was previously in Enviroment.
 *
 * This isolates all GUI state into the gui package, allowing the core
 * engine package to be GUI-independent.
 */
public class GuiState {

    /**
     * The main window reference
     */
    private static Window frame;

    /**
     * The source text editor pane
     */
    private static HighlightedJPane text;

    /**
     * Display settings (syntax highlighting, leading zeros, register view)
     */
    private static final DisplaySettings displaySettings = new DisplaySettings();

    /**
     * Whether to show the label window
     */
    private static boolean labelWindow = false;

    /**
     * The label window instance
     */
    private static LabelWindow labelWindowInstance = new LabelWindow();

    /**
     * Memory view for GUI visualization
     */
    private static MemoryView memoryView;

    /**
     * Gets the main window
     */
    public static Window getFrame() {
        return frame;
    }

    /**
     * Sets the main window
     */
    public static void setFrame(Window window) {
        frame = window;
    }

    /**
     * Gets the source text editor
     */
    public static HighlightedJPane getText() {
        return text;
    }

    /**
     * Sets the source text editor
     */
    public static void setText(HighlightedJPane pane) {
        text = pane;
    }

    /**
     * Gets the register view type
     */
    public static RegisterViewType getRegView() {
        return displaySettings.getRegisterView();
    }

    /**
     * Sets the register view type
     */
    public static void setRegView(RegisterViewType view) {
        displaySettings.setRegisterView(view);
    }

    /**
     * Gets whether the label window should be shown
     */
    public static boolean isLabelWindowVisible() {
        return labelWindow;
    }

    /**
     * Sets whether the label window should be shown
     */
    public static void setLabelWindowVisible(boolean visible) {
        labelWindow = visible;
    }

    /**
     * Gets the label window instance
     */
    public static LabelWindow getLabelWindow() {
        return labelWindowInstance;
    }

    /**
     * Sets the label window instance
     */
    public static void setLabelWindow(LabelWindow window) {
        labelWindowInstance = window;
    }

    /**
     * Gets the memory view for GUI visualization
     */
    public static MemoryView getMemoryView() {
        if (memoryView == null) {
            memoryView = new MemoryView();
        }
        return memoryView;
    }

    /**
     * Resets the memory view (should be called after Machine.resetInstance())
     */
    public static void resetMemoryView() {
        if (memoryView != null) {
            memoryView.cleanup();
        }
        memoryView = new MemoryView();
    }

    /**
     * Gets whether syntax highlighting is enabled
     */
    public static boolean isSyntaxHighlighting() {
        return displaySettings.isSyntaxHighlighting();
    }

    /**
     * Sets whether syntax highlighting is enabled
     */
    public static void setSyntaxHighlighting(boolean enabled) {
        displaySettings.setSyntaxHighlighting(enabled);
    }

    /**
     * Gets whether to show leading zeros in numeric displays
     */
    public static boolean isShowLeadingZeros() {
        return displaySettings.isShowLeadingZeros();
    }

    /**
     * Sets whether to show leading zeros in numeric displays
     */
    public static void setShowLeadingZeros(boolean show) {
        displaySettings.setShowLeadingZeros(show);
    }

    /**
     * Gets the display settings instance for direct access.
     */
    public static DisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    /**
     * Resets all GUI state
     */
    public static void reset() {
        labelWindowInstance = new LabelWindow();
        if (memoryView != null) {
            memoryView.cleanup();
        }
        memoryView = null;
        displaySettings.reset();
    }
}
