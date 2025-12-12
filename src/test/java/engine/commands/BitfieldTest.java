package engine.commands;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Bitfield instructions: EXTS, EXT, INS, FINDC, FINDS.
 *
 * MI Specification (page 29-30):
 * - EXT a1,a2,a3,a4: P := S[a1] (position), S = S[a2] (size), A := a3 (base address)
 *   Result: S[a4] := bitfield extracted and zero-extended to 32 bits
 * - EXTS: Same but sign-extended
 * - For register addressing: 0 <= P <= 32 and P + S <= 64
 *   The bitfield must be contained in two consecutive registers
 *
 * MI Bit Numbering (MSB-0):
 * - Bit 0 is the most significant bit (leftmost)
 * - For a 32-bit word 0xAABBCCDD at address A:
 *   - Bits 0-7: 0xAA (at address A)
 *   - Bits 8-15: 0xBB (at address A+1)
 *   - Bits 16-23: 0xCC (at address A+2)
 *   - Bits 24-31: 0xDD (at address A+3)
 *
 * Condition Codes (page 35):
 * - EXTS/EXT: C=0, V=0, Z=*, N=*
 * - INS: C=-, V=-, Z=-, N=-
 * - FINDC/FINDS: C=0, V=0, Z=BNF (bit not found), N=0
 */
public class BitfieldTest extends InstructionTestBase {

    // ==================== EXT Tests ====================
    // EXT extracts a bitfield from memory starting at address A,
    // at bit position P with size S bits.

    @Test
    public void ext_fromMemory_simple() {
        // Extract bits 0-7 (MSB) from memory
        // MI uses MSB-0 bit numbering: bit 0 is the most significant bit
        // For value 0xFF000000: bits 0-7 contain 0xFF
        // Flags: C=0, V=0, Z=*, N=*
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0 (bit position - MSB)
                "MOVE W I 8, R2",     // S=8 (size in bits)
                "EXT R1, R2, data, R3",
                "HALT",
                "data: DD W -16777216"  // 0xFF000000 - MSB byte is 0xFF
        ));
        assertEquals(0xFF, getRegister(3));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertFalse("Negative flag should be 0 (unsigned extraction)", isNegativeFlag());
    }

    @Test
    public void ext_fromMemory_lowBits() {
        // Extract bits 24-31 (LSB) from memory
        // For value 0x000000FF: bits 24-31 contain 0xFF
        assembleAndRun(program(
                "MOVE W I 24, R1",    // P=24 (bit position - LSB area)
                "MOVE W I 8, R2",     // S=8 (size in bits)
                "EXT R1, R2, data, R3",
                "HALT",
                "data: DD W H'000000FF'"  // LSB byte is 0xFF
        ));
        assertEquals(0xFF, getRegister(3));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertFalse("Negative flag should be 0 (unsigned extraction)", isNegativeFlag());
    }

    @Test
    public void ext_fromMemory_zeroResult() {
        // Extract bits 0-7 from a value where those bits are zero
        // For value 0x00FF0000: bits 0-7 are 0x00
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0 (start at MSB)
                "MOVE W I 8, R2",     // S=8 (extract 8 bits)
                "EXT R1, R2, data, R3",
                "HALT",
                "data: DD W H'00FF0000'"  // Bits 0-7 are zero
        ));
        assertEquals(0, getRegister(3));
        assertTrue("Zero flag should be set", isZeroFlag());
    }

    // ==================== EXTS Tests ====================
    // EXTS extracts with sign extension

    @Test
    public void exts_fromMemory_signExtend() {
        // Extract 8 bits with sign extension
        // Value 0xFF at bits 0-7 sign-extended to 32 bits = -1
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0 (MSB)
                "MOVE W I 8, R2",     // S=8
                "EXTS R1, R2, data, R3",
                "HALT",
                "data: DD W -16777216"  // 0xFF000000 - MSB byte = 0xFF
        ));
        assertEquals(-1, getRegister(3));
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Zero flag should be 0", isZeroFlag());
        assertTrue("Negative flag should be set (sign extended)", isNegativeFlag());
    }

    @Test
    public void exts_fromMemory_positiveValue() {
        // Extract 8 bits with value 0x7F (positive when sign-extended)
        // 0x7F000000 has bits 0-7 = 0x7F (sign bit 0, positive)
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0 (MSB)
                "MOVE W I 8, R2",     // S=8
                "EXTS R1, R2, data, R3",
                "HALT",
                "data: DD W 2130706432"  // 0x7F000000
        ));
        assertEquals(0x7F, getRegister(3));
        assertFalse("Negative flag should be 0 (positive value)", isNegativeFlag());
    }

    // ==================== FINDS Tests ====================
    // FINDS (Find first Set bit)
    // Returns position of first '1' bit, or P+S if not found
    // Spec: Z=BNF (set if bit not found)

    @Test
    public void finds_fromMemory_found() {
        // Find first set bit starting from bit 0 (MSB)
        // For value 0x40000000 = 0100 0000 ... binary, bit 1 is first set bit
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0 (start position)
                "MOVE W I 32, R2",    // S=32 (search all 32 bits)
                "FINDS R1, R2, data, R3",
                "HALT",
                "data: DD W 1073741824"  // 0x40000000 - bit 1 is set
        ));
        assertEquals(1, getRegister(3));  // First set bit at position 1
        assertFalse("Zero flag should be 0 (bit found)", isZeroFlag());
        assertFalse("Carry flag should be 0", isCarryFlag());
        assertFalse("Overflow flag should be 0", isOverflowFlag());
        assertFalse("Negative flag should be 0", isNegativeFlag());
    }

    @Test
    public void finds_fromMemory_notFound() {
        // Find first set bit in 0x00000000 - none found
        // Result should be P+S = 0+32 = 32
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0
                "MOVE W I 32, R2",    // S=32
                "FINDS R1, R2, data, R3",
                "HALT",
                "data: DD W 0"        // No bits set
        ));
        assertEquals(32, getRegister(3));  // P+S when not found
        assertTrue("Zero flag should be set (bit NOT found - BNF)", isZeroFlag());
    }

    // ==================== FINDC Tests ====================
    // FINDC (Find first Clear bit)
    // Returns position of first '0' bit, or P+S if not found

    @Test
    public void findc_fromMemory_found() {
        // Find first clear bit starting from bit 0
        // For value 0xBFFFFFFF = 1011 1111... binary, bit 1 is first clear bit
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0
                "MOVE W I 32, R2",    // S=32 (search all bits)
                "FINDC R1, R2, data, R3",
                "HALT",
                "data: DD W -1073741825"  // 0xBFFFFFFF - bit 1 is clear
        ));
        assertEquals(1, getRegister(3));  // First '0' at bit 1
        assertFalse("Zero flag should be 0 (bit found)", isZeroFlag());
    }

    @Test
    public void findc_fromMemory_notFound() {
        // Find first clear bit in 0xFFFFFFFF - none in 32 bits
        assembleAndRun(program(
                "MOVE W I 0, R1",     // P=0
                "MOVE W I 32, R2",    // S=32
                "FINDC R1, R2, data, R3",
                "HALT",
                "data: DD W -1"       // 0xFFFFFFFF - all bits set
        ));
        assertEquals(32, getRegister(3));  // P+S when not found
        assertTrue("Zero flag should be set (bit NOT found - BNF)", isZeroFlag());
    }

}
