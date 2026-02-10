package engine;

/**
 * Machine-level constants for the MI simulator.
 *
 * This class contains only the hardware configuration constants
 * required by the MI machine specification. It has no GUI or AWT dependencies.
 *
 * @see <a href="https://www.unibw.de/inf2/lehre/wt19/maschprog/mi-manual-pdf.pdf">MI Manual</a>
 */
public final class MachineConstants {

    private MachineConstants() {
        // Prevent instantiation
    }

    /**
     * Total memory size in bytes (1 MByte).
     */
    public static final int MEMORY_SIZE = 1048576;

    /**
     * Stack Pointer register index (R14).
     */
    public static final int SP_REGISTER = 14;

    /**
     * Program Counter register index (R15).
     */
    public static final int PC_REGISTER = 15;

    /**
     * Total number of registers (R0-R15).
     */
    public static final int REGISTER_COUNT = 16;

    /**
     * Word size in bytes.
     */
    public static final int WORD_SIZE = 4;

    /**
     * Half-word size in bytes.
     */
    public static final int HALF_WORD_SIZE = 2;

    /**
     * Byte size (for completeness).
     */
    public static final int BYTE_SIZE = 1;

    /**
     * Float size in bytes.
     */
    public static final int FLOAT_SIZE = 4;

    /**
     * Double size in bytes.
     */
    public static final int DOUBLE_SIZE = 8;
}