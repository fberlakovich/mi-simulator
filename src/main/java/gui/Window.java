package gui;

import enviroment.Enviroment;
import org.drjekyll.fontchooser.FontDialog;
import parser.Parser;
import scanner.Scanner;
import simulator.Command;
import simulator.Halt;
import simulator.RunProgram;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
     * Übersetzungsstatus
     */
    private boolean compiled = false;

    /**
     * The run.
     */
    private RunProgram run;

    /**
     * The file.
     */
    private File file;

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
     * Letzer Text der Quelltexteingabe
     */
    private String oldtext = "";

    /**
     * Dateiauswahl.
     */
    private final JFileChooser fc = new JFileChooser();

    /**
     * Konstruktor für das Fenster
     */
    public Window() {
        super();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (saveOld()) {
                    System.exit(0);
                }

            }

        });
        initGUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Window.this.updateUI();
            }
        });

    }

    /**
     * Gibt das akutell ablaufende Programm zurueck
     *
     * @return akutell ablaufende Programm
     */
    public RunProgram getRun() {
        return run;
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

        // Diese Methode sollte unbedingt in de nächsten Version auf mehrere
        // Klassen aufgeteilt werden.
        fc.setFileFilter(new FileNameExtensionFilter("MI-File", "mi"));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        codePanel = new JPanel();
        buttonPanel = new ButtonPanel(compiled);
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
        Enviroment.setJFrame(this);
        Enviroment.init();

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
            Enviroment.setText(text);

            DropTarget target = new DropTarget(text, new DropTargetAdapter() {

                public void drop(DropTargetDropEvent dtde) {
                    if (dtde.isDataFlavorSupported(
                            DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(dtde.getDropAction());
                        try {
                            List<File> list = (List<File>) dtde.getTransferable()
                                    .getTransferData(
                                            DataFlavor.javaFileListFlavor);
                            for (File file1 : list) {
                                if (saveOld()) {
                                    file = file1;
                                    int ch;
                                    StringBuffer strContent = new StringBuffer(
                                            "");

                                    FileInputStream in = new FileInputStream(
                                            file);
                                    while ((ch = in.read())
                                            != -1) {
                                        strContent.append(
                                                (char) ch);
                                    }

                                    in.close();
                                    text.setText(
                                            strContent.toString());
                                    text.setCaretPosition(0);
                                    oldtext = text.getText();
                                    buttonPanel.getBtnRestart().setEnabled(
                                            false);
                                    buttonPanel.getBtnRun().setEnabled(false);
                                    buttonPanel.getBtnStep().setEnabled(false);
                                    buttonPanel.getBtnStop().setEnabled(false);
                                    updateUI();
                                    Enviroment.init();
                                    reset_Title();

                                }

                            }

                        } catch (Exception E) {
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
        buttonPanel.getBtnAssemble().addActionListener(e2 -> {
            numberedPane.reset();
            Enviroment.init();
            scanner = new Scanner(false);
            scanner.init(text.getText());

            Parser p = new Parser(scanner);
            p.start();

            if (p.eval()) {

                if (p.getProgramm().getCommands().size()
                        > 0) {
                    Enviroment.setProgram(
                            p.getProgramm());
                    Enviroment.MEMORY.setContent(0,
                            p.getProgramm()
                                    .getOpCode());

                    nex = Enviroment.readNextCommand();
                    text.highlightNextCommand();

                    run = new RunProgram(buttonPanel.getBtnRun(),
                            buttonPanel.getBtnStop(),
                            buttonPanel.getBtnStep(),
                            buttonPanel.getBtnRestart());
                    error.setText(
                            CONSTANTS.ASSEMBLE_SUCCESSFUL);
                    compiled = true;
                } else {
                    error.setText(
                            CONSTANTS.ASSEMBLE_UNSUCCESSFUL);
                    compiled = false;
                }
            } else {
                error.setText(
                        p.getErrorMeassge().toString());
                compiled = false;
            }

            updateUI();
            if (p.getErrorMeassge().getErrorMessage().length()
                    > 0) {

                error.setText(p.getErrorMeassge()
                        .getErrorMessage());
                compiled = false;
            }
            Enviroment.setCompiled(compiled);
            text.setCompiled(compiled);
            buttonPanel.getBtnRun().setEnabled(compiled);
            buttonPanel.getBtnStep().setEnabled(compiled);
            buttonPanel.getBtnRestart().setEnabled(compiled);

        });
        buttonPanel.getBtnRun().addActionListener(e1 -> {
            buttonPanel.getBtnRun().setEnabled(false);
            buttonPanel.getBtnStop().setEnabled(true);
            buttonPanel.getBtnStep().setEnabled(false);
            Enviroment.REGISTERS.reset();
            Enviroment.MEMORY.resetChanges();
            run = new RunProgram(buttonPanel.getBtnRun(), buttonPanel.getBtnStop(), buttonPanel.getBtnStep(),
                    buttonPanel.getBtnRestart());
            run.start();

            memory = Enviroment.MEMORY.getMemoryTable();
        });
        buttonPanel.getBtnStep().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                Enviroment.REGISTERS.reset();
                Enviroment.MEMORY.resetChanges();
                nex = Enviroment.getNextCommand();

                if (nex != null && !(nex instanceof Halt)) {

                    nex.run();

                } else {

                    buttonPanel.getBtnRun().setEnabled(false);
                    buttonPanel.getBtnStep().setEnabled(false);
                    error.setText("Programmende");
                }
                Enviroment.readNextCommand();
                text.highlightNextCommand();

                updateUI();

            }
        });
        buttonPanel.getBtnStop().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                run.stopProgram();
                text.highlightNextCommand();
                text.highlightNextCommand();

                buttonPanel.getBtnStop().setEnabled(false);
            }
        });
        buttonPanel.getBtnRestart().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                run.stopProgram();
                Enviroment.init();
                Enviroment.MEMORY.setContent(0,
                        Enviroment.getProgram()
                                .getOpCode());
                nex = Enviroment.readNextCommand();
                text.highlightNextCommand();
                error.setText(CONSTANTS.ASSEMBLE_SUCCESSFUL);
                buttonPanel.getBtnRun().setEnabled(true);
                buttonPanel.getBtnStep().setEnabled(true);
                buttonPanel.getBtnStop().setEnabled(false);
                updateUI();
            }
        });

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
                newFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!text.getText().equals(oldtext)) {
                            saveOld();
                        }
                        file = null;
                        fc.setSelectedFile(new File(""));
                        reset_Title();
                        text.setText("");
                        oldtext = "";
                        Enviroment.init();
                        buttonPanel.getBtnRestart().setEnabled(false);
                        buttonPanel.getBtnRun().setEnabled(false);
                        buttonPanel.getBtnStep().setEnabled(false);
                        buttonPanel.getBtnStop().setEnabled(false);
                        updateUI();

                    }
                });

                file_menu.add(newFile);
                newFile.setText("Neu");
            }
            {
                openFileMenuItem = new JMenuItem();
                openFileMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (saveOld()) {
                            int returnVal = fc.showOpenDialog(
                                    Window.this);
                            if (returnVal
                                    == JFileChooser.APPROVE_OPTION) {
                                file = fc.getSelectedFile();
                                int ch;
                                StringBuffer strContent = new StringBuffer(
                                        "");
                                try {
                                    FileInputStream in = new FileInputStream(
                                            file);
                                    while ((ch = in.read())
                                            != -1) {
                                        strContent.append(
                                                (char) ch);
                                    }

                                    in.close();
                                    text.setText(
                                            strContent.toString());
                                    text.setCaretPosition(0);
                                    oldtext = text.getText();
                                    buttonPanel.getBtnRestart().setEnabled(
                                            false);
                                    buttonPanel.getBtnRun().setEnabled(false);
                                    buttonPanel.getBtnStep().setEnabled(false);
                                    buttonPanel.getBtnStop().setEnabled(false);
                                    updateUI();
                                    Enviroment.init();
                                    reset_Title();

                                } catch (Exception E) {
                                    error.setText(
                                            CONSTANTS.ERROR_OPENFILE);

                                }
                            }

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
                saveMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (file == null) {
                            int returnVal = fc.showSaveDialog(
                                    Window.this);
                            if (returnVal
                                    == JFileChooser.APPROVE_OPTION) {
                                file = fc.getSelectedFile();
                            }
                        }
                        if (file != null) {
                            try {
                                FileOutputStream in = new FileOutputStream(
                                        file);

                                in.write(text.getText().getBytes());
                                oldtext = text.getText();
                                in.close();
                                reset_Title();

                            } catch (Exception E) {
                                error.setText(
                                        CONSTANTS.ERROR_SAVEFILE);

                            }

                        }

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
                saveAsMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        int returnVal = fc.showSaveDialog(Window.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            file = fc.getSelectedFile();
                            try {
                                FileOutputStream in = new FileOutputStream(
                                        file);

                                in.write(text.getText().getBytes());
                                oldtext = text.getText();
                                in.close();
                                reset_Title();
                            } catch (Exception E) {
                                error.setText(
                                        CONSTANTS.ERROR_SAVEFILE);
                            }

                        }

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
                exitMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (saveOld()) {
                            System.exit(0);
                        }
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
                undoMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // text.getUndoManager().end();

                        if (text.getUndoManager().canUndo()) {
                            text.getUndoManager().undo();
                            Window.this.textChanged();
                        }
                        text.requestFocus();
                    }
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
                shlMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Enviroment.shl = ((JCheckBoxMenuItem) e.getSource()).getState();
                        Enviroment.text.doHighLighting();
                    }
                });
                settings_menu.add(shlMenuItem);
                shlMenuItem.setText("Syntaxhighlighting");

                label_windowMenuItem = new JCheckBoxMenuItem();
                label_windowMenuItem.setSelected(false);

                label_windowMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Enviroment.label_Window = ((JCheckBoxMenuItem) e.getSource()).getState();
                        if (Enviroment.compiled) {
                            Enviroment.getLabelWindow().setVisible(
                                    Enviroment.label_Window);
                        }
                    }
                });
                settings_menu.add(label_windowMenuItem);
                label_windowMenuItem.setText("Fenster mit Labeladressen");

                JCheckBoxMenuItem showLeadingZerosItem = new JCheckBoxMenuItem();
                showLeadingZerosItem.setSelected(false);

                showLeadingZerosItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Enviroment.showLeadingZeros = showLeadingZerosItem.getState();
                        Enviroment.frame.updateUI();
                    }
                });
                settings_menu.add(showLeadingZerosItem);
                showLeadingZerosItem.setText("Führende Nullen anzeigen");
                showLeadingZerosItem.setState(Enviroment.showLeadingZeros);

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

                fontMenuItem = new JMenuItem();
                fontMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        JOptionPane.showMessageDialog(Window.this,
                                CONSTANTS.INFO_TEXT,
                                CONSTANTS.INFO_TITLE,
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                jMenu5.add(fontMenuItem);
                fontMenuItem.setText("Über");
            }
        }
        getContentPane().add(main_panel);
        updateUI();

    }

    /**
     * Setzt den Titel des JFrames neu
     */
    public void reset_Title() {
        setTitle(
                CONSTANTS.TITLE + " - " + (file != null ? file.getName() : "unbenannt.mi"));
    }

    /**
     * Zeigt den Speicherdialog, wenn sich der Inhalt geändert hat
     *
     * @return true, if successful
     */
    private boolean saveOld() {
        if (text.getText().equals(oldtext)) {
            return true; // keine Änderungen
        }
        int option = JOptionPane.showConfirmDialog(Window.this,
                "Möchten Sie die Änderungen in " + (
                        file != null ?
                                file.getName() :
                                "unbenannt.mi")
                        + " speichern?",
                "Änderungen speichern?",
                JOptionPane.YES_NO_CANCEL_OPTION);
        switch (option) {
            case JOptionPane.YES_OPTION:
                if (file == null) {
                    int returnVal = fc.showSaveDialog(Window.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fc.getSelectedFile();
                    }
                }
                if (file != null) {
                    try {
                        FileOutputStream in = new FileOutputStream(file);

                        in.write(text.getText().getBytes());
                        oldtext = text.getText();
                        in.close();

                    } catch (Exception E) {
                        error.setText(CONSTANTS.ERROR_SAVEFILE);
                    }
                } else {
                    return false;
                }
                return true;

            case JOptionPane.NO_OPTION:
                return true;

            case JOptionPane.CANCEL_OPTION:
                return false;

            default:
                return false;
        }

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
        if (run != null) {
            run.stopProgram();
            run = null;
        }

        numberedPane.reset();
        Enviroment.setCompiled(false);
        Enviroment.getLabelWindow().setVisible(false);
        error.setText("");
        text.setCompiled(false);
        buttonPanel.getBtnRun().setEnabled(false);
        buttonPanel.getBtnStep().setEnabled(false);
        buttonPanel.getBtnStop().setEnabled(false);
        buttonPanel.getBtnRestart().setEnabled(false);
        Enviroment.init();
        updateUI();
    }

    /**
     * Aktualisiert die grafische Oberflaeche
     */
    public void updateUI() {
        if (Enviroment.getNextCommand() != null
                && Enviroment.getNextCommand() instanceof Halt) {
            error.setText(CONSTANTS.PROGRAM_END);
            buttonPanel.getBtnRun().setEnabled(false);
            buttonPanel.getBtnStep().setEnabled(false);
        }

        registerPanel.updateRegisterValues();

        int val = (memory == null) ? 0 : memory.getVerticalScrollBar().getValue();
        int val2 = (stack == null) ? 0 : stack.getVerticalScrollBar().getValue();
        flagsPanel = new FlagsPanel(Enviroment.flags);

        JPanel instr_panel = new JPanel();
        if (compiled) {
            JLabel instr = new JLabel(" " + (Enviroment.getNextCommand() != null ?
                    Enviroment.getNextCommand().toString() :
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
        memory = Enviroment.MEMORY.getMemoryTable();
        memory.setSize(CONSTANTS.MEMORY_WIDTH, CONSTANTS.MEMORY_HEIGHT);
        memory.getVerticalScrollBar().setValue(val);
        memory.getVerticalScrollBar().setValue(val);
        stack = Enviroment.MEMORY.getStackTable();
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

}
