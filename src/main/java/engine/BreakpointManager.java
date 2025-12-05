package engine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages breakpoints for program execution.
 * Breakpoints are stored by memory address rather than being embedded in Command objects.
 */
public class BreakpointManager {

    /** Set of addresses where breakpoints are set */
    private final Set<Integer> breakpoints = new HashSet<>();

    /**
     * Sets a breakpoint at the specified address.
     *
     * @param address the memory address
     */
    public void setBreakpoint(int address) {
        breakpoints.add(address);
    }

    /**
     * Removes a breakpoint at the specified address.
     *
     * @param address the memory address
     */
    public void removeBreakpoint(int address) {
        breakpoints.remove(address);
    }

    /**
     * Toggles a breakpoint at the specified address.
     *
     * @param address the memory address
     * @return true if breakpoint is now set, false if removed
     */
    public boolean toggleBreakpoint(int address) {
        if (breakpoints.contains(address)) {
            breakpoints.remove(address);
            return false;
        } else {
            breakpoints.add(address);
            return true;
        }
    }

    /**
     * Checks if a breakpoint is set at the specified address.
     *
     * @param address the memory address
     * @return true if breakpoint is set
     */
    public boolean hasBreakpoint(int address) {
        return breakpoints.contains(address);
    }

    /**
     * Clears all breakpoints.
     */
    public void clearAll() {
        breakpoints.clear();
    }

    /**
     * Gets an unmodifiable view of all breakpoint addresses.
     *
     * @return set of breakpoint addresses
     */
    public Set<Integer> getBreakpoints() {
        return Collections.unmodifiableSet(breakpoints);
    }

    /**
     * Gets the number of breakpoints set.
     *
     * @return breakpoint count
     */
    public int getCount() {
        return breakpoints.size();
    }
}
