package engine.events;

import java.util.Collections;
import java.util.Map;

/**
 * Event emitted during assembly (compilation) process.
 *
 * Replaces direct LabelWindow updates in CodeGenerator.java.
 */
public class AssemblyEvent extends MachineEvent {

    public enum Type {
        /** Assembly started */
        STARTED,
        /** Assembly completed successfully */
        SUCCESS,
        /** Assembly failed with errors */
        FAILED,
        /** Labels have been resolved */
        LABELS_RESOLVED,
        /** Warning (e.g., memory manipulation detected) */
        WARNING
    }

    private final Type type;
    private final String message;
    private final Map<String, Integer> labels;
    private final int errorLine;

    public AssemblyEvent(Type type, String message) {
        this(type, message, Collections.emptyMap(), -1);
    }

    public AssemblyEvent(Type type, String message, Map<String, Integer> labels) {
        this(type, message, labels, -1);
    }

    public AssemblyEvent(Type type, String message, int errorLine) {
        this(type, message, Collections.emptyMap(), errorLine);
    }

    private AssemblyEvent(Type type, String message, Map<String, Integer> labels, int errorLine) {
        super();
        this.type = type;
        this.message = message;
        this.labels = Collections.unmodifiableMap(labels);
        this.errorLine = errorLine;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    /**
     * @return Map of label names to their resolved addresses (for LABELS_RESOLVED events)
     */
    public Map<String, Integer> getLabels() {
        return labels;
    }

    /**
     * @return The line number where an error occurred, or -1 if not applicable
     */
    public int getErrorLine() {
        return errorLine;
    }

    @Override
    public String toString() {
        return String.format("AssemblyEvent{type=%s, message='%s', labels=%d, errorLine=%d}",
                type, message, labels.size(), errorLine);
    }
}
