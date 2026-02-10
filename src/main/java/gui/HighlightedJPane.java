package gui;

import engine.MachineContext;
import engine.ProgramRunner;
import engine.scanner.Scanner;
import engine.scanner.Token;
import engine.commands.Command;
import engine.commands.DD;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Diese Klasse stellt den SourceCode eines MI-Programms dar. Dabei wird dieser
 * entsprechend eingefärbt.
 *
 * @author Nico Krebs und Mattias Oehme
 */
public class HighlightedJPane extends JTextPane implements KeyListener {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Beginn des zuletzt hervorgehobenen Wortes
     */
    private int highl_beg_old;

    /**
     * Ende des zuletzt hervorgehobenen Wortes
     */
    private int highl_end_old;

    /**
     * Modus: true, Programm ist assembliert, false sonst
     */
    private boolean compiled = false;

    /**
     * Letzte Inhalt des Textfeldes
     */
    private String oldText = "";

    // die einzelnen Farben

    /**
     * The key word.
     */
    private MutableAttributeSet keyWord = new SimpleAttributeSet();

    /**
     * The number.
     */
    private MutableAttributeSet number = new SimpleAttributeSet();

    /**
     * The string.
     */
    private MutableAttributeSet string = new SimpleAttributeSet();

    /**
     * The hexnumber.
     */
    private MutableAttributeSet hexnumber = new SimpleAttributeSet();

    /**
     * The float.
     */
    private MutableAttributeSet floating = new SimpleAttributeSet();

    /**
     * The comment.
     */
    private MutableAttributeSet comment = new SimpleAttributeSet();

    /**
     * The label.
     */
    private MutableAttributeSet label = new SimpleAttributeSet();

    /**
     * The fehler.
     */
    private MutableAttributeSet fehler = new SimpleAttributeSet();

    /**
     * The akt.
     */
    private MutableAttributeSet akt = new SimpleAttributeSet();

    /**
     * The norm.
     */
    private MutableAttributeSet norm = new SimpleAttributeSet();

    /**
     * FarbDefinition für falschen Opcode
     */
    private MutableAttributeSet different_opcode = new SimpleAttributeSet();

    final UndoManager undomanager = new UndoManager();

    /**
     * Display settings for syntax highlighting preference.
     */
    private DisplaySettings displaySettings;

    /**
     * Callback for text change notifications.
     */
    private Runnable textChangeCallback;

    /**
     * Constructor with dependency injection.
     *
     * @param displaySettings settings for syntax highlighting
     * @param textChangeCallback callback when text changes
     */
    public HighlightedJPane(DisplaySettings displaySettings, Runnable textChangeCallback) {
        super();
        this.displaySettings = displaySettings;
        this.textChangeCallback = textChangeCallback;
        setFont(CONSTANTS.FONT);
        StyleConstants.setForeground(keyWord, new Color(60, 120, 60));
        StyleConstants.setForeground(number, new Color(60, 60, 180));
        StyleConstants.setForeground(string, new Color(180, 60, 60));
        StyleConstants.setForeground(hexnumber, new Color(0, 150, 120));
        StyleConstants.setForeground(floating, new Color(0, 150, 120));
        StyleConstants.setForeground(comment, new Color(140, 140, 140));
        StyleConstants.setForeground(label, new Color(140, 80, 80));
        StyleConstants.setForeground(akt, new Color(0, 0, 0));
        StyleConstants.setBackground(akt, new Color(255, 255, 0));
        StyleConstants.setForeground(norm, new Color(0, 0, 0));
        StyleConstants.setForeground(fehler, new Color(255, 0, 0));
        StyleConstants.setBackground(different_opcode, new Color(255, 0, 0));

        // behandel selbst alle Veränderungen des Cursors...
        addKeyListener(this);

        getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                // style changes are done automatically by the text pane. it
                // doesn't make sense to undo them
                if (e.getEdit().getPresentationName() != "Formatvorlagenänderung") {
                    undomanager.addEdit(e.getEdit());
                }

            }
        });

    }

    /**
     * Checks if syntax highlighting is enabled.
     */
    private boolean isSyntaxHighlightingEnabled() {
        return displaySettings != null && displaySettings.isSyntaxHighlighting();
    }

    /**
     * Notifies that text has changed.
     */
    private void notifyTextChanged() {
        if (textChangeCallback != null) {
            textChangeCallback.run();
        }
    }

    /**
     * Diese Methode färbt alle erkannten Token im im ganzen Text
     */
    public void doHighLighting() {
        if (isSyntaxHighlightingEnabled()) {
            Token ret;
            Scanner scan = new Scanner(true);
            scan.init(getText());
            while (true) {
                ret = scan.getNextSymbol();
                if (ret.getNr() == Scanner.TOKEN_EOF) {
                    break;
                }
                highl(ret);
            }
        } else {
            setCharacterAttributes(getStyledDocument(), 0, this.getText().length(),
                    norm);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JEditorPane#getScrollableTracksViewportWidth()
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Highlightet das übergegebene Token im Quelltext
     *
     * @param in erkanntes Token
     */

    public void highl(Token in) {
        switch (in.getNr()) {
            case Scanner.TOKEN_NAME:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), label);
                break;
            case Scanner.TOKEN_LABEL:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), label);
                break;
            case Scanner.TOKEN_NUMBER:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), number);
                break;
            case Scanner.TOKEN_HEX:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), hexnumber);
                break;
            case Scanner.TOKEN_COMMENT:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), comment);
                break;
            case Scanner.TOKEN_REGISTER:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), number);
                break;
            case Scanner.TOKEN_BIN:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), number);
                break;
            case Scanner.TOKEN_STRING:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), string);
                break;
            case Scanner.TOKEN_ERROR:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), fehler);
                break;
            case Scanner.TOKEN_FLOAT:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), floating);
                break;
            default:
                setCharacterAttributes(getStyledDocument(), in.getBeg(),
                        in.getEnd() - in.getBeg(), keyWord);
                break;
        }

    }

    /**
     * Highlightet den aktuellen Assemblerbefehl
     *
     * @param beg         Beginn des Bereiches
     * @param end         End des Bereiches
     * @param diff_opcode true, wenn der Text rot hervorgehoben werden soll
     */
    public void highlight(int beg, int end, boolean diff_opcode) {
        setCharacterAttributes(getStyledDocument(), highl_beg_old, highl_end_old,
                isSyntaxHighlightingEnabled() ? keyWord : norm);
        highl_beg_old = beg;
        highl_end_old = end;
        setCharacterAttributes(getStyledDocument(), beg, end,
                diff_opcode ? different_opcode : akt);
    }

    /**
     * Highlighted den nächsten Befehl
     *
     * @param runner  the program runner (may be null)
     * @param machine the machine context for command lookup
     */
    public void highlightNextCommand(ProgramRunner runner, MachineContext machine) {
        Command nextCommand = runner != null ? runner.getNextCommand() : null;
        if (nextCommand != null) {
            Command next = machine.getCommandAtAddress(nextCommand.getAdress());
            if (next != null) {
                if (java.util.Arrays.equals(next.encode(), nextCommand.encode())
                        && !(next instanceof DD)) {
                    highlight(next.getBeg(), next.getEnd() - next.getBeg(),
                            false);
                    setCaretPosition(next.getEnd());
                } else {
                    highlight(next.getBeg(), next.getEnd() - next.getBeg(),
                            true);
                    setCaretPosition(next.getEnd());
                }
            } else {
                highlight(0, 0, false);
            }
        } else {
            highlight(0, 0, false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent arg0) {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent arg0) {

        // prüfen ob sich der Inhalt des Textfeldes wirklich geändert hat
        if (!(getText().equals(oldText))) {
            if (compiled) {
                compiled = false;
                setBackground(new Color(255, 255, 255));
                notifyTextChanged();
            }
            doHighLighting();
            oldText = getText();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent arg0) {

    }

    /**
     * Die Mathode färbt das aktuell erkannte Token entsprechend ein. Dazu wird
     * das gestylte Dokument übergeben, die Start- und Endposition und die
     * jeweilig Farbe.
     *
     * @param doc  the doc
     * @param pos1 the pos1
     * @param pos2 the pos2
     * @param k    the k
     */
    public void setCharacterAttributes(StyledDocument doc, int pos1, int pos2,
                                       MutableAttributeSet k) {

        // solange, bis es klappt wiederholen...
        // Es kann hier Schreib-/Lesekonflikte geben, die
        // sich aber schnell auflösen...

        doc.setCharacterAttributes(pos1, pos2, k, true);

    }

    /**
     * setzt die Farbe des Hintergrundes des Textfeldes
     *
     * @param compiled the new compiled
     */
    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
        if (compiled) {
            setBackground(new Color(228, 228, 228));

        } else {
            setBackground(new Color(255, 255, 255));
        }
    }

    // Vergrößern falls nötig
    /*
     * (non-Javadoc)
     *
     * @see java.awt.Component#setSize(java.awt.Dimension)
     */
    @Override
    public void setSize(Dimension d) {
        if (d.width < getParent().getSize().width) {
            d.width = getParent().getSize().width;
        }

        super.setSize(d);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JEditorPane#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        setCompiled(false);
        notifyTextChanged();
        doHighLighting();
    }

    public UndoManager getUndoManager() {
        return undomanager;
    }

}