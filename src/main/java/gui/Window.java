package gui;

import engine.events.ExecutionEvent;
import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.MemoryAccessErrorEvent;
import engine.ProgramRunner;
import engine.Machine;
import engine.program.Program;
import engine.util.MemoryChangeTracker;
import engine.state.MyByte;
import org.drjekyll.fontchooser.FontDialog;
import engine.parser.Parser;
import engine.scanner.Scanner;
import engine.commands.Command;
import engine.commands.Halt;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Diese Klasse enthält die grafischen Elemente
 */
public class Window extends javax.swing.JFrame {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The j menu5.
     */
    private JMenu jMenu5;

    /**
     * The exit menu item.
     */
    private JMenuItem exitMenuItem;

    /**
     * The j separator2.
     */
    private JSeparator jSeparator2;

    /**
     * The new file.
     */
    private JMenuItem newFile;

    /**
     * The save as menu item.
     */
    private JMenuItem saveAsMenuItem;

    /**
     * The save menu item.
     */
    private JMenuItem saveMenuItem;

    /**
     * The open file menu item.
     */
    private JMenuItem openFileMenuItem;

    /**
     * The about menu item.
     */
    private JMenuItem fontMenuItem;

    /**
     * The undo menu item.
     */
    private JMenuItem undoMenuItem;

    /**
     * The about menu item.
     */
    private JCheckBoxMenuItem shlMenuItem;

    /**
     * The about menu item.
     */
    private JCheckBoxMenuItem label_windowMenuItem;

    /**
     * Dateimenü
     */
    private JMenu file_menu;

    /**
     * Bearbeitenmenü
     */
    private JMenu edit_menu;

    /**
     * Einstellungsmenü
     */
    private JMenu settings_menu;

    /**
     * Menüleiste
     */
    private JMenuBar jMenuBar1;

    /**
     * Quelltextscanner
     */
    private Scanner scanner;

    /**
     * Anzeigebereich für den Speicher
     */
    private MemoryPanel memory;

    /**
     * Anzeigebereich für die Register
     */
    private JScrollPane register;

    /**
     * Anzeige der Flags
     */
    private FlagsPanel flagsPanel;

    /**
     * Anzeigebereich für den Kellerspeicher
     */
    private JScrollPane stack;

    /**
     * The menu_panel.
     */
    private ButtonPanel buttonPanel;


    /**
     * Nächster befehl
     */
    private Command nex = null;

    /**
     * Program execution thread.
     */
    private ProgramRunner runner;

    /**
     * Memory change tracker for highlighting.
     */
    private MemoryChangeTracker memoryTracker;

    /**
     * File manager for handling file operations.
     */
    private FileManager fileManager;

    /**
     * The input_panel.
     */
    private JPanel input_panel;

    /**
     * The register_panel.
     */
    private RegistersPanel registerPanel;

    /**
     * The memory_panel.
     */
    private JPanel memoryPanel;

    /**
     * The flag_panel.
     */
    private JPanel flag_panel;

    /**
     * The north_panel.
     */
    private JScrollPane north_panel;

    /**
     * The south_panel.
     */
    private JScrollPane south_panel;

    /**
     * The ober_panel.
     */
    private JPanel ober_panel;

    /**
     * The ober_panel2.
     */
    private JPanel ober_panel2;

    /**
     * The center_panel.
     */
    private JPanel codePanel;

    /**
     * Quelltexteingabefeld
     */
    private HighlightedJPane text;

    /**
     * Fehlerausgabe
     */
    private JTextPane error;

    /**
     * JTextField mit Zeilenummern
     */
    private NumberedPane numberedPane;

    /**
     * Mainpanel.
     */
    public JSplitPane main_panel;


    /**
     * Konstruktor für das Fenster
     */
    public Window() {
        super();
        initGUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (fileManager.promptSaveIfNeeded(text.getText())) {
                    System.exit(0);
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Window.this.updateUI();
            }
        });
    }

    /**
     * Gets the current program runner.
     *
     * @return program runner
     */
    public ProgramRunner getRunner() {
        return runner;
    }

    /**
     * Gibt das Quelltexteinhabefeld zurueck
     *
     * @return Quelltexteinhabefeld
     */
    public HighlightedJPane getTextPane() {
        return text;
    }

    /**
     * initalisiert die grafische Oberfläche
     */
    private void initGUI() {
        Locale.setDefault(Locale.GERMAN);

        // Initialize file manager
        fileManager = new FileManager(this);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        codePanel = new JPanel();
        buttonPanel = new ButtonPanel(false);
        buttonPanel.setLayout(new FlowLayout(10));
        ober_panel = new JPanel();
        ober_panel.setLayout(new BoxLayout(ober_panel, BoxLayout.X_AXIS));
        ober_panel2 = new JPanel();
        ober_panel2.setLayout(new BoxLayout(ober_panel2, BoxLayout.Y_AXIS));
        ober_panel2.add(buttonPanel);
        ober_panel2.add(ober_panel);
        north_panel = new JScrollPane(ober_panel2);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.Y_AXIS));
        setTitle(CONSTANTS.TITLE + " - unbenannt.mi");
        GuiState.setFrame(this);
        Machine.resetInstance();
        GuiState.resetMemoryView();

        memoryPanel = new JPanel();
        memoryPanel.setLayout(new BoxLayout(memoryPanel, BoxLayout.Y_AXIS));


        {
            input_panel = new JPanel();
            input_panel.setLayout(new BorderLayout());
            numberedPane = new NumberedPane();
            input_panel.add(numberedPane, BorderLayout.WEST);
            input_panel.add(numberedPane.scrollPane, BorderLayout.CENTER);
            codePanel.add(input_panel);
            text = numberedPane.getTextPane();
            GuiState.setText(text);

            new DropTarget(text, new DropTargetAdapter() {

                public void drop(DropTargetDropEvent dtde) {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(dtde.getDropAction());
                        try {
                            @SuppressWarnings("unchecked")
                            List<File> list = (List<File>) dtde.getTransferable()
                                    .getTransferData(DataFlavor.javaFileListFlavor);
                            for (File droppedFile : list) {
                                if (fileManager.promptSaveIfNeeded(text.getText())) {
                                    FileManager.FileResult result = fileManager.openFile(droppedFile);
                                    if (result.isSuccess()) {
                                        text.setText(result.getContent());
                                        text.setCaretPosition(0);
                                        disableExecutionButtons();
                                        updateUI();
                                        Machine.resetInstance();
                                        reset_Title();
                                    } else if (result.getErrorMessage() != null) {
                                        error.setText(result.getErrorMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            error.setText(CONSTANTS.ERROR_OPENFILE);
                        }
                    }
                }
            });

        }
        {
            registerPanel = new RegistersPanel();
            ober_panel.add(registerPanel);
            ober_panel.add(codePanel);
            ober_panel.add(memoryPanel);

        }
        {
            south_panel = new JScrollPane();
            south_panel.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            error = new JTextPane();
            error.setEditable(false);
            south_panel.setViewportView(error);
            main_panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, north_panel,
                    south_panel);
            main_panel.setOneTouchExpandable(true);
            main_panel.setResizeWeight(0.75);
        }
        // Wire up button panel handlers
        buttonPanel.getBtnAssemble().addActionListener(e -> handleAssemble());
        buttonPanel.getBtnRun().addActionListener(e -> handleRun());
        buttonPanel.getBtnStep().addActionListener(e -> handleStep());
        buttonPanel.getBtnStop().addActionListener(e -> handleStop());
        buttonPanel.getBtnRestart().addActionListener(e -> handleRestart());

        flag_panel = new JPanel();
        codePanel.add(flag_panel);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        jMenuBar1 = new JMenuBar();
        setJMenuBar(jMenuBar1);
        {
            file_menu = new JMenu();
            jMenuBar1.add(file_menu);
            file_menu.setText("Datei");
            {
                newFile = new JMenuItem();
                newFile.addActionListener(e -> {
                    if (fileManager.promptSaveIfNeeded(text.getText())) {
                        fileManager.newFile();
                        reset_Title();
                        text.setText("");
                        Machine.resetInstance();
                        disableExecutionButtons();
                        updateUI();
                    }
                });

                file_menu.add(newFile);
                newFile.setText("Neu");
            }
            {
                openFileMenuItem = new JMenuItem();
                openFileMenuItem.addActionListener(e -> {
                    if (fileManager.promptSaveIfNeeded(text.getText())) {
                        FileManager.FileResult result = fileManager.openFile();
                        if (result.isSuccess()) {
                            text.setText(result.getContent());
                            text.setCaretPosition(0);
                            disableExecutionButtons();
                            updateUI();
                            Machine.resetInstance();
                            reset_Title();
                        } else if (result.getErrorMessage() != null) {
                            error.setText(result.getErrorMessage());
                        }
                    }
                });

                file_menu.add(openFileMenuItem);
                openFileMenuItem.setText("Öffnen");
                openFileMenuItem.setAccelerator(
                        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
                                java.awt.Event.CTRL_MASK));
            }

            {
                saveMenuItem = new JMenuItem();
                saveMenuItem.addActionListener(e -> {
                    FileManager.FileResult result = fileManager.save(text.getText());
                    if (result.isSuccess()) {
                        reset_Title();
                    } else if (result.getErrorMessage() != null) {
                        error.setText(result.getErrorMessage());
                    }
                });
                file_menu.add(saveMenuItem);
                saveMenuItem.setText("Speichern");
                saveMenuItem.setAccelerator(
                        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                                java.awt.Event.CTRL_MASK));
            }
            {
                saveAsMenuItem = new JMenuItem();
                saveAsMenuItem.addActionListener(e -> {
                    FileManager.FileResult result = fileManager.saveAs(text.getText());
                    if (result.isSuccess()) {
                        reset_Title();
                    } else if (result.getErrorMessage() != null) {
                        error.setText(result.getErrorMessage());
                    }
                });
                file_menu.add(saveAsMenuItem);
                saveAsMenuItem.setText("Speichern unter ...");
            }
            {
                jSeparator2 = new JSeparator();
                file_menu.add(jSeparator2);
            }
            {
                exitMenuItem = new JMenuItem();
                exitMenuItem.addActionListener(e -> {
                    if (fileManager.promptSaveIfNeeded(text.getText())) {
                        System.exit(0);
                    }
                });
                file_menu.add(exitMenuItem);

                exitMenuItem.setText("Beenden");
            }
        }

        {
            edit_menu = new JMenu();
            jMenuBar1.add(edit_menu);
            edit_menu.setText("Bearbeiten");
            {
                undoMenuItem = new JMenuItem();
                undoMenuItem.addActionListener(e -> {
                    if (text.getUndoManager().canUndo()) {
                        text.getUndoManager().undo();
                        textChanged();
                    }
                    text.requestFocus();
                });
                edit_menu.add(undoMenuItem);
                undoMenuItem.setText("Rückgängig");
                undoMenuItem.setAccelerator(
                        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
                                java.awt.Event.CTRL_MASK));
            }
        }

        {
            settings_menu = new JMenu();
            jMenuBar1.add(settings_menu);
            settings_menu.setText("Einstellungen");
            {
                shlMenuItem = new JCheckBoxMenuItem();
                shlMenuItem.setSelected(true);
                shlMenuItem.addActionListener(e -> {
                    GuiState.setSyntaxHighlighting(shlMenuItem.getState());
                    GuiState.getText().doHighLighting();
                });
                settings_menu.add(shlMenuItem);
                shlMenuItem.setText("Syntaxhighlighting");

                label_windowMenuItem = new JCheckBoxMenuItem();
                label_windowMenuItem.setSelected(false);
                label_windowMenuItem.addActionListener(e -> {
                    GuiState.setLabelWindowVisible(label_windowMenuItem.getState());
                    if (Machine.getInstance().isCompiled()) {
                        GuiState.getLabelWindow().setVisible(GuiState.isLabelWindowVisible());
                    }
                });
                settings_menu.add(label_windowMenuItem);
                label_windowMenuItem.setText("Fenster mit Labeladressen");

                JCheckBoxMenuItem showLeadingZerosItem = new JCheckBoxMenuItem();
                showLeadingZerosItem.setSelected(GuiState.isShowLeadingZeros());
                showLeadingZerosItem.addActionListener(e -> {
                    GuiState.setShowLeadingZeros(showLeadingZerosItem.getState());
                    updateUI();
                });
                settings_menu.add(showLeadingZerosItem);
                showLeadingZerosItem.setText("Führende Nullen anzeigen");

                fontMenuItem = new JMenuItem("Schriftart ändern");
                fontMenuItem.addActionListener(e -> {
                    FontDialog fontDialog = new FontDialog(this, true);
                    fontDialog.setSelectedFont(text.getFont());
                    fontDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    fontDialog.setVisible(true);
                    if (!fontDialog.isCancelSelected()) {
                        Font selectedFont = fontDialog.getSelectedFont();
                        text.setFont(selectedFont);
                        registerPanel.setFontSize(selectedFont.getSize());
                        flagsPanel.setFontSize(selectedFont.getSize());
                        buttonPanel.setFontSize(selectedFont.getSize());
                        memory.setFontSize(selectedFont.getSize());
                    }
                });
                settings_menu.add(fontMenuItem);
            }
        }

        {
            jMenu5 = new JMenu();
            jMenuBar1.add(jMenu5);
            jMenu5.setText("Über");
            {
                JMenuItem aboutMenuItem = new JMenuItem("Über");
                aboutMenuItem.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,
                            CONSTANTS.INFO_TEXT,
                            CONSTANTS.INFO_TITLE,
                            JOptionPane.INFORMATION_MESSAGE));
                jMenu5.add(aboutMenuItem);
            }
        }
        getContentPane().add(main_panel);
        updateUI();

    }

    /**
     * Setzt den Titel des JFrames neu
     */
    public void reset_Title() {
        setTitle(CONSTANTS.TITLE + " - " + fileManager.getCurrentFileName());
    }

    /**
     * Disables all execution-related buttons.
     */
    private void disableExecutionButtons() {
        buttonPanel.getBtnRestart().setEnabled(false);
        buttonPanel.getBtnRun().setEnabled(false);
        buttonPanel.getBtnStep().setEnabled(false);
        buttonPanel.getBtnStop().setEnabled(false);
    }

    // ========================================================================
    // Button Panel Handlers
    // ========================================================================

    /**
     * Handles the Assemble button: parses and compiles the source code.
     */
    private void handleAssemble() {
        numberedPane.reset();
        Machine.resetInstance();
        scanner = new Scanner(false);
        scanner.init(text.getText());

        Parser p = new Parser(Machine.getInstance(), scanner);
        p.start();

        Program program = p.getProgramm();
        if (p.eval()) {
            if (program.compile()) {
                runner = Machine.getInstance().createRunner();
                nex = runner.readNextCommand();
                text.highlightNextCommand();

                setupExecutionListener();
                error.setText(CONSTANTS.ASSEMBLE_SUCCESSFUL);

                GuiState.getLabelWindow().setContent(p.getLabels());
                GuiState.getLabelWindow().setVisible(GuiState.isLabelWindowVisible());
                GuiState.getLabelWindow().pack();
            } else {
                error.setText(CONSTANTS.ASSEMBLE_UNSUCCESSFUL);
            }
        } else {
            error.setText(p.getErrorMeassge().toString());
        }

        updateUI();
        if (p.getErrorMeassge().getErrorMessage().length() > 0) {
            error.setText(p.getErrorMeassge().getErrorMessage());
        }
        text.setCompiled(program.isCompiled());
        buttonPanel.getBtnRun().setEnabled(program.isCompiled());
        buttonPanel.getBtnStep().setEnabled(program.isCompiled());
        buttonPanel.getBtnRestart().setEnabled(program.isCompiled());
    }

    /**
     * Handles the Run button: starts continuous program execution.
     */
    private void handleRun() {
        registerPanel.resetChangedFlags();
        if (memoryTracker != null) {
            memoryTracker.reset();
        }
        runner = Machine.getInstance().createRunner();
        runner.startProgram();

        memory = GuiState.getMemoryView().getMemoryTable();
    }

    /**
     * Handles the Step button: executes one instruction.
     */
    private void handleStep() {
        registerPanel.resetChangedFlags();
        if (memoryTracker != null) {
            memoryTracker.reset();
        }

        if (runner == null || !runner.isAlive()) {
            runner = Machine.getInstance().createRunner();
        }
        boolean hasMore = runner.step();

        if (!hasMore) {
            buttonPanel.getBtnRun().setEnabled(false);
            buttonPanel.getBtnStep().setEnabled(false);
            error.setText("Programmende");
        }

        text.highlightNextCommand();
        updateUI();
    }

    /**
     * Handles the Stop button: halts program execution.
     */
    private void handleStop() {
        if (runner != null) {
            runner.stopProgram();
        }
        text.highlightNextCommand();
        text.highlightNextCommand();

        buttonPanel.getBtnStop().setEnabled(false);
    }

    /**
     * Handles the Restart button: resets machine state and reloads program.
     */
    private void handleRestart() {
        if (runner != null) {
            runner.stopProgram();
        }
        Program savedProgram = Machine.getInstance().getProgram();
        Machine.resetInstance();
        Machine.getInstance().setProgram(savedProgram);
        Machine.getInstance().getMemory().setContent(0,
                MyByte.fromByteArray(savedProgram.encode()));
        runner = Machine.getInstance().createRunner();
        nex = runner.readNextCommand();
        text.highlightNextCommand();
        error.setText(CONSTANTS.ASSEMBLE_SUCCESSFUL);
        buttonPanel.getBtnRun().setEnabled(true);
        buttonPanel.getBtnStep().setEnabled(true);
        buttonPanel.getBtnStop().setEnabled(false);
        updateUI();
    }

    /**
     * Setzt den Fehlertext
     *
     * @param text Fehlertext
     */
    public void setErrorText(String text) {
        error.setText(text);
    }

    /**
     * Wird ausgefuehrt, wenn eine Veränderung des Quelltextes festgestellt wird
     */
    public void textChanged() {
        if (runner != null) {
            runner.stopProgram();
            runner = null;
        }

        numberedPane.reset();
        GuiState.getLabelWindow().setVisible(false);
        error.setText("");
        text.setCompiled(false);
        buttonPanel.getBtnRun().setEnabled(false);
        buttonPanel.getBtnStep().setEnabled(false);
        buttonPanel.getBtnStop().setEnabled(false);
        buttonPanel.getBtnRestart().setEnabled(false);
        Machine.resetInstance();
        updateUI();
    }

    /**
     * Aktualisiert die grafische Oberflaeche
     */
    public void updateUI() {
        if (Machine.getInstance().getNextCommand() != null
                && Machine.getInstance().getNextCommand() instanceof Halt) {
            error.setText(CONSTANTS.PROGRAM_END);
            buttonPanel.getBtnRun().setEnabled(false);
            buttonPanel.getBtnStep().setEnabled(false);
        }

        registerPanel.updateRegisterValues();

        int val = (memory == null) ? 0 : memory.getVerticalScrollBar().getValue();
        int val2 = (stack == null) ? 0 : stack.getVerticalScrollBar().getValue();
        flagsPanel = new FlagsPanel(Machine.getInstance().getFlags());

        JPanel instr_panel = new JPanel();
        if (Machine.getInstance().isCompiled()) {
            JLabel instr = new JLabel(" " + (Machine.getInstance().getNextCommand() != null ?
                    Machine.getInstance().getNextCommand().toString() :
                    "no Instr"));
            instr.setFont(CONSTANTS.FONT);
            instr.setForeground(CONSTANTS.DARK_GREY);
            instr_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            instr_panel.add(instr);
        }
        flagsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        flag_panel.setLayout(new BoxLayout(flag_panel, BoxLayout.Y_AXIS));

        flag_panel.removeAll();

        flag_panel.add(flagsPanel);
        flag_panel.add(instr_panel);

        text.highlightNextCommand();

        memoryPanel.removeAll();
        memory = GuiState.getMemoryView().getMemoryTable();
        memory.setSize(CONSTANTS.MEMORY_WIDTH, CONSTANTS.MEMORY_HEIGHT);
        memory.getVerticalScrollBar().setValue(val);
        memory.getVerticalScrollBar().setValue(val);
        stack = GuiState.getMemoryView().getStackTable();
        stack.setSize(CONSTANTS.STACK_WIDTH, CONSTANTS.STACK_HEIGHT);
        stack.getVerticalScrollBar().setValue(val2);
        stack.getVerticalScrollBar().setValue(val2);
        memoryPanel.add("Speicher", memory);
        memoryPanel.add("Stack", stack);
        validate();
        this.repaint();

    }

    /**
     * Setzt die Größe des Fensters
     */
    public void resetSize() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        int y = this.getHeight();
        int x = this.getWidth();
        setExtendedState(JFrame.NORMAL);
        setSize(Math.min(CONSTANTS.WINDOW_WIDTH, x), Math.min(CONSTANTS.WINDOW_HEIGHT, y));
    }

    /**
     * Sets up the execution event listener for program execution.
     */
    private void setupExecutionListener() {
        // Create memory tracker if not already created
        if (memoryTracker == null) {
            memoryTracker = new MemoryChangeTracker();
        }

        // Subscribe to execution events
        Machine.getInstance().getEventBus().subscribe(ExecutionEvent.class, new MachineEventListener() {
            @Override
            public void onEvent(MachineEvent event) {
                if (event instanceof ExecutionEvent) {
                    ExecutionEvent execEvent = (ExecutionEvent) event;
                    SwingUtilities.invokeLater(() -> handleExecutionEvent(execEvent));
                }
            }
        });

        // Subscribe to memory access errors
        Machine.getInstance().getEventBus().subscribe(MemoryAccessErrorEvent.class, new MachineEventListener() {
            @Override
            public void onEvent(MachineEvent event) {
                if (event instanceof MemoryAccessErrorEvent) {
                    MemoryAccessErrorEvent errorEvent = (MemoryAccessErrorEvent) event;
                    SwingUtilities.invokeLater(() -> handleMemoryAccessError(errorEvent));
                }
            }
        });
    }

    /**
     * Handles memory access errors by displaying an error dialog.
     */
    private void handleMemoryAccessError(MemoryAccessErrorEvent event) {
        String message = String.format("Memory access error at address 0x%X: %s",
                event.getAddress(), event.getType());
        JOptionPane.showMessageDialog(this, message, "Memory Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Handles execution events from the program runner.
     */
    private void handleExecutionEvent(ExecutionEvent event) {
        switch (event.getType()) {
            case STARTED:
                buttonPanel.getBtnStop().setEnabled(true);
                buttonPanel.getBtnRun().setEnabled(false);
                buttonPanel.getBtnStep().setEnabled(false);
                buttonPanel.getBtnRestart().setEnabled(false);
                break;

            case STOPPED:
            case BREAKPOINT_HIT:
                buttonPanel.getBtnStop().setEnabled(false);
                buttonPanel.getBtnRestart().setEnabled(true);
                buttonPanel.getBtnRun().setEnabled(true);
                buttonPanel.getBtnStep().setEnabled(true);
                text.highlightNextCommand();
                updateUI();
                break;

            case PROGRAM_ENDED:
                buttonPanel.getBtnStop().setEnabled(false);
                buttonPanel.getBtnRestart().setEnabled(true);
                buttonPanel.getBtnRun().setEnabled(false);
                buttonPanel.getBtnStep().setEnabled(false);
                text.highlightNextCommand();
                updateUI();
                break;

            case STEP_COMPLETED:
                updateUI();
                break;
        }
    }

    /**
     * Gets the memory change tracker.
     *
     * @return the memory change tracker, or null if not initialized
     */
    public MemoryChangeTracker getMemoryTracker() {
        return memoryTracker;
    }
}
