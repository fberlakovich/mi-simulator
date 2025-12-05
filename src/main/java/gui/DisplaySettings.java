package gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Manages display settings for the MI simulator GUI.
 * Supports property change notifications for settings updates.
 */
public class DisplaySettings {

    public static final String PROP_SYNTAX_HIGHLIGHTING = "syntaxHighlighting";
    public static final String PROP_SHOW_LEADING_ZEROS = "showLeadingZeros";
    public static final String PROP_REGISTER_VIEW = "registerView";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean syntaxHighlighting = true;
    private boolean showLeadingZeros = true;
    private RegisterViewType registerView = RegisterViewType.DECIMAL;

    /**
     * Gets whether syntax highlighting is enabled.
     */
    public boolean isSyntaxHighlighting() {
        return syntaxHighlighting;
    }

    /**
     * Sets whether syntax highlighting is enabled.
     */
    public void setSyntaxHighlighting(boolean enabled) {
        boolean oldValue = this.syntaxHighlighting;
        this.syntaxHighlighting = enabled;
        pcs.firePropertyChange(PROP_SYNTAX_HIGHLIGHTING, oldValue, enabled);
    }

    /**
     * Gets whether to show leading zeros in numeric displays.
     */
    public boolean isShowLeadingZeros() {
        return showLeadingZeros;
    }

    /**
     * Sets whether to show leading zeros in numeric displays.
     */
    public void setShowLeadingZeros(boolean show) {
        boolean oldValue = this.showLeadingZeros;
        this.showLeadingZeros = show;
        pcs.firePropertyChange(PROP_SHOW_LEADING_ZEROS, oldValue, show);
    }

    /**
     * Gets the register view type.
     */
    public RegisterViewType getRegisterView() {
        return registerView;
    }

    /**
     * Sets the register view type.
     */
    public void setRegisterView(RegisterViewType view) {
        RegisterViewType oldValue = this.registerView;
        this.registerView = view;
        pcs.firePropertyChange(PROP_REGISTER_VIEW, oldValue, view);
    }

    /**
     * Adds a property change listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Adds a property change listener for a specific property.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Resets all settings to their default values.
     */
    public void reset() {
        setSyntaxHighlighting(true);
        setShowLeadingZeros(true);
        setRegisterView(RegisterViewType.DECIMAL);
    }
}
