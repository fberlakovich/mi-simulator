package gui;

import engine.state.Flags;

import javax.swing.*;
import java.awt.*;

public class FlagsPanel extends JPanel {
    private final FixedStateCheckBox chkCarry = new FixedStateCheckBox("Carry");
    private final FixedStateCheckBox chkZero = new FixedStateCheckBox("Zero");
    private final FixedStateCheckBox chkOverflow = new FixedStateCheckBox("Overflow");
    private final FixedStateCheckBox chkNegative = new FixedStateCheckBox("Negative");


    public FlagsPanel(Flags flags) {
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        innerPanel.add(chkZero);
        innerPanel.add(chkOverflow);
        innerPanel.add(chkCarry);
        innerPanel.add(chkNegative);
        this.add(innerPanel);
        update(flags);
    }

    private void update(Flags flags) {
        chkOverflow.setFixedState(flags.isOverflow());
        chkNegative.setFixedState(flags.isNegative());
        chkZero.setFixedState(flags.isZero());
        chkCarry.setFixedState(flags.isCarry());
    }

    public void setFontSize(int size) {
        GuiUtils.changeFont(this, size);
    }
}
