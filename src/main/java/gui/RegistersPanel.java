package gui;

import engine.events.MachineEvent;
import engine.events.MachineEventListener;
import engine.events.RegisterChangeEvent;
import engine.Machine;
import engine.state.MyByte;
import engine.util.NumberConversion;
import engine.state.Register;
import engine.util.RegisterChangeTracker;

import javax.swing.*;
import java.awt.*;

class RegistersPanel extends JPanel implements MachineEventListener {
    private final JTextField[] registerTextFields = new JTextField[CONSTANTS.NUMBER_OF_REGISTER];
    private final RegisterChangeTracker changeTracker = new RegisterChangeTracker();

    public RegistersPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createScrollPanel());
        Machine.getInstance().getEventBus().subscribe(RegisterChangeEvent.class, this);
    }

    @Override
    public void onEvent(MachineEvent event) {
        if (event instanceof RegisterChangeEvent) {
            RegisterChangeEvent rce = (RegisterChangeEvent) event;
            SwingUtilities.invokeLater(() -> updateRegister(rce.getRegisterIndex(), GuiState.getRegView()));
        }
    }

    /**
     * Resets the change tracking for highlighting.
     * Call this after each instruction or when refreshing the display.
     */
    public void resetChangedFlags() {
        changeTracker.reset();
    }

    private void updateRegister(int regNum, RegisterViewType regview) {
        Register register = Machine.getInstance().getRegisters().getRegister(regNum);
        MyByte[] content = register.getContent(4);
        boolean changed = changeTracker.isChanged(regNum);

        String text = "";
        switch (regview) {
            case DECIMAL:
                text = Integer.toString(NumberConversion.myBytetoIntWithSign(content));
                break;
            case BINARY:
                text = NumberConversion.myBytetoBin(content);
                if (GuiState.isShowLeadingZeros()) {
                    text = String.format("%1$" + CONSTANTS.WORD_SIZE * 8 + "s", text).replace(" ", "0");
                }
                break;
            case HEX:
                text = NumberConversion.myBytetoHex(content);
                if (GuiState.isShowLeadingZeros()) {
                    text = String.format("%1$" + CONSTANTS.WORD_SIZE * 2 + "s", text).replace(" ", "0");
                }
                break;
            case FLOAT:
                float number = Float.intBitsToFloat(
                        NumberConversion.myBytetoIntWithoutSign(content));
                if (Float.isNaN(number)) {
                    text = "NaN";
                } else {
                    text = Float.toString(number);
                }
                break;
        }
        JTextField textField = registerTextFields[regNum];

        textField.setText(text);
        textField.setForeground(changed ? Color.red : Color.black);
        textField.setColumns(Math.max(text.length(), 15));
    }


    public void updateRegisterValues() {
        for (int i = 0; i < CONSTANTS.NUMBER_OF_REGISTER; i++) {
            updateRegister(i, GuiState.getRegView());
        }
    }

    private JScrollPane createScrollPanel() {
        JPanel chooserAndRegisters = new JPanel();
        JPanel chooserPanel = new JPanel();
        JComboBox<RegisterViewType> chooser = new JComboBox<>();
        chooser.addItem(RegisterViewType.DECIMAL);
        chooser.addItem(RegisterViewType.BINARY);
        chooser.addItem(RegisterViewType.HEX);
        chooser.addItem(RegisterViewType.FLOAT);
        chooser.setSelectedItem(GuiState.getRegView());
        chooser.addActionListener(evt -> {
            @SuppressWarnings("unchecked") JComboBox<RegisterViewType> cb = (JComboBox<RegisterViewType>) evt.getSource();

            RegisterViewType selectedViewType = cb.getItemAt(cb.getSelectedIndex());
            GuiState.setRegView(selectedViewType);
            GuiState.getFrame().updateUI();
        });

        chooserPanel.add(chooser);
        chooserAndRegisters.add(chooserPanel);

        for (int i = 0; i < CONSTANTS.NUMBER_OF_REGISTER; i++) {
            JPanel registerPanel = new JPanel();
            registerPanel.add(new JLabel("R" + ((i < 10) ? i + "  " : i)));

            JTextField textField = new JTextField();
            textField.setFont(CONSTANTS.FONT);
            textField.setHorizontalAlignment(SwingConstants.RIGHT);
            textField.setFocusable(false);
            textField.setMargin(new Insets(0, 5, 0, 5));
            registerTextFields[i] = textField;
            registerPanel.add(textField);
            chooserAndRegisters.add(registerPanel);
        }

        chooserAndRegisters.setLayout(new BoxLayout(chooserAndRegisters, BoxLayout.Y_AXIS));

        return new JScrollPane(chooserAndRegisters);
    }

    public void setFontSize(int size) {
        GuiUtils.changeFont(this, size);
    }
}
