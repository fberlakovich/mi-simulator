package gui;

import engine.Machine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Die Klasse ist für das Anzeigen eines JTextPane mit Zeilennummern
 *
 * @author Matthias Oehme
 */
public class NumberedPane extends JPanel {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * JTextPane
     */
    private HighlightedJPane textPane;

    /**
     * JScrollPane auf dem das ganze angezeigt wird
     */
    JScrollPane scrollPane;

    /**
     * Array mit Labeln
     */
    private List<LineNumberLabel> lineNumberLabels = new ArrayList<>();

    /**
     * The max.
     */
    private int max = 20;

    /**
     * The icon1.
     */
    private ImageIcon icon1;

    /**
     * The icon2.
     */
    private ImageIcon icon2;

    /**
     * Instanziiert ein NumberedPane
     */
    public NumberedPane() {
        super();

        URL resource1 = getClass().getResource("/images/icon2.gif");
        URL resource2 = getClass().getResource("/images/icon1.gif");
        icon1 = new ImageIcon(resource1);
        icon2 = new ImageIcon(resource2);

        setMinimumSize(new Dimension(50, 40));
        setPreferredSize(new Dimension(50, 40));

        textPane = new HighlightedJPane() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                NumberedPane.this.repaint();
            }

            @Override
            public void paintComponent(Graphics g) {
                setAntiAliasEnabled(g);
                super.paintComponent(g);
            }
        };

        scrollPane = new JScrollPane(textPane);

        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                textPane.setSize(new Dimension(scrollPane.getWidth() - 20,
                        scrollPane.getHeight() - 20));
            }
        });

        scrollPane.setMinimumSize(new Dimension(50, 40));
        scrollPane.setPreferredSize(new Dimension(50, 40));
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    }

    /**
     * Gibt das HiglightedJPane zurueck
     *
     * @return the text pane
     */
    public HighlightedJPane getTextPane() {
        return textPane;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // We need to properly convert the points to match the view port
        // Read docs for view port
        int start = textPane.viewToModel(scrollPane.getViewport().getViewPosition());
        // starting pos in document
        int end = textPane.viewToModel(
                new Point(scrollPane.getViewport().getViewPosition().x + textPane.getWidth(),
                        scrollPane.getViewport().getViewPosition().y + textPane.getHeight()));
        // end pos in doc

        // translate offsets to lines
        Document doc = textPane.getDocument();
        int startline = doc.getDefaultRootElement().getElementIndex(start) + 1;
        int endline = doc.getDefaultRootElement().getElementIndex(end) + 1;

        int fontHeight = g.getFontMetrics(textPane.getFont()).getHeight();
        int fontDesc = g.getFontMetrics(textPane.getFont()).getDescent();
        int starting_y = -1;

        try {
            starting_y = textPane.modelToView(start).y - scrollPane.getViewport()
                    .getViewPosition().y
                    + fontHeight - fontDesc;
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        int merke = 0;
        for (LineNumberLabel lineNumberLabel : lineNumberLabels) {
            this.remove(lineNumberLabel);
        }

        if (endline <= lineNumberLabels.size()) {
            lineNumberLabels.subList(endline, lineNumberLabels.size()).clear();
        }


        for (int line = startline, y = starting_y;
             line <= endline; y += fontHeight, line++) {
            if (line > lineNumberLabels.size()) {
                LineNumberLabel newLabel = new LineNumberLabel(Integer.toString(line), icon1, icon2);
                newLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        if (arg0.getClickCount() == 2) {

                            if (Machine.getInstance().isCompiled()) {
                                LineNumberLabel inp = (LineNumberLabel) arg0.getSource();
                                inp.setStatus(!inp.getStatus());

                            } else {
                                JOptionPane.showMessageDialog(
                                        GuiState.getFrame(),
                                        CONSTANTS.ERROR_SETBREAKPOINT_NOT_ASSEMBLED,
                                        CONSTANTS.ERROR_SETBREAKPOINT_TITEL,
                                        JOptionPane.ERROR_MESSAGE);
                            }

                        }
                    }
                });
                lineNumberLabels.add(newLabel);

            }
            LineNumberLabel label = lineNumberLabels.get(line - 1);
            label.setBounds(0, y - 15, 40, 20);
            Font currentFont = label.getFont();
            label.setFont(currentFont.deriveFont(currentFont.getStyle(), textPane.getFont().getSize()));
            this.add(label);
            merke++;
        }

        max = merke + startline;

    }

    /**
     * Reset.
     */
    public void reset() {
        for (LineNumberLabel lab : lineNumberLabels) {
            if (lab != null) {
                lab.reset();
            }
        }
    }

    private void setAntiAliasEnabled(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

}
