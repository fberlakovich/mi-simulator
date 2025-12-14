package gui;

import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryUpdateEvent;
import engine.MachineContext;
import engine.util.MemoryChangeTracker;
import engine.state.MyByte;
import engine.util.NumberConversion;

import javax.swing.*;
import java.util.ArrayList;

import static engine.MachineConstants.MEMORY_SIZE;
import static engine.MachineConstants.SP_REGISTER;

/**
 * GUI visualization for the Memory.
 *
 * This class contains all GUI-specific code that was previously in Memory.java.
 * It creates Swing components for displaying memory and stack contents.
 */
public class MemoryView implements MachineEventListener {

    private JList<MemoryTableEntry> memoryJList;
    private ArrayList<MemoryTableEntry> data;
    private final MachineContext machine;
    private final MemoryChangeTracker tracker;

    public MemoryView(MachineContext machine, MemoryChangeTracker tracker) {
        this.machine = machine;
        this.tracker = tracker;
        createMemory();
        machine.getEventBus().subscribe(MemoryUpdateEvent.class, this);
    }

    /**
     * Cleans up resources (unsubscribes from event bus).
     */
    public void cleanup() {
        machine.getEventBus().unsubscribe(MemoryUpdateEvent.class, this);
    }

    @Override
    public void onEvent(MachineEvent event) {
        if (event instanceof MemoryUpdateEvent) {
            MemoryUpdateEvent mue = (MemoryUpdateEvent) event;
            int address = mue.getAddress();
            int row = address / 8;
            int col = address % 8;

            if (data != null && row < data.size()) {
                MemoryTableEntry entry = data.get(row);
                // Update the byte in the entry
                // Since MemoryTableEntry.data is package-private, we can access it.
                // But wait, we should check if we can access it.
                // Assuming package-private access works as they are in the same package.
                if (entry.data != null && col < entry.data.size()) {
                    entry.data.set(col, machine.getMemory().getRawByte(address));
                }

                // Notify model
                SwingUtilities.invokeLater(() -> {
                    ListModel model = memoryJList.getModel();
                    if (model instanceof MemoryTable) {
                        ((MemoryTable) model).fireRowUpdated(row);
                    }
                });
            }
        }
    }

    /**
     * Initalisiert den Speicher und die Speicherdarstellung
     */
    public void createMemory() {
        data = new ArrayList<>();
        int count = 0;
        String line = "000000";

        ArrayList<MyByte> list = new ArrayList<>();
        for (int i = 0; i < MEMORY_SIZE; i++) {
            list.add(machine.getMemory().getRawByte(i));
            count++;
            if (count == 8) {
                data.add(new MemoryTableEntry(list, line, tracker));

                line = "00000".substring(0, 6 - Integer.toHexString(i + 1).length())
                        + Integer.toHexString(i + 1);
                line = line.toUpperCase();

                list = new ArrayList<>();
                count = 0;
            }
        }
        memoryJList = new JList<>(new MemoryTable(data));
    }

    /**
     * Gibt die Speicherdarstellung auf einer JScrollPane zurueck
     *
     * @return Speicherdarstellung auf einer JScrollPane
     */
    public MemoryPanel getMemoryTable() {
        memoryJList.setFont(CONSTANTS.FONT);
        return new MemoryPanel(memoryJList);
    }

    /**
     * Gibt die Kellerdarstellung auf einer JScrollPane zurueck
     *
     * @return Kellerdarstellung auf einer JScrollPane
     */
    public JScrollPane getStackTable() {
        int count = 0;
        int merke = 0;
        int ende = machine.getStackBegin();
        String[] liste = null;

        int beg = NumberConversion.myBytetoIntWithSign(
                machine.getRegisters().getRegister(SP_REGISTER).getContent(4));
        beg = beg / 8 * 8;
        if (beg <= ende && ende != 0) {
            String line = "00000".substring(0, 6 - Integer.toHexString(beg).length())
                    + Integer.toHexString(beg);
            line = line.toUpperCase();
            liste = new String[(machine.getStackBegin() - beg) / 4 + 1];

            for (int i = beg; i <= machine.getStackBegin() + 8; i++) {
                MyByte memByte = machine.getMemory().getRawByte(i);
                boolean changed = tracker != null && tracker.isChanged(i);

                if (changed) {
                    line += "<font color=\"#ff0000\"> " + memByte.toString()
                            + "</font>";
                } else {
                    line += " " + memByte.toString();
                }

                count++;
                if (count == 8) {
                    liste[merke++] = "<html>" + line + "</html>";
                    line = "00000".substring(0, 6 - Integer.toHexString(i + 1)
                            .length())
                            + Integer.toHexString(i + 1);
                    line = line.toUpperCase();
                    count = 0;
                }
            }
        }
        JList<String> stackJList = new JList<>(liste == null ? new String[]{} : liste);
        stackJList.setFont(CONSTANTS.FONT);

        return new JScrollPane(stackJList);
    }
}
