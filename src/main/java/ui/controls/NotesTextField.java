package ui.controls;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

public class NotesTextField extends JPanel implements FocusListener, DocumentListener {

    private final JTextField textField;

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_CHANGE = "change";

    private boolean changed;
    private boolean listenerActive;

    public NotesTextField(int columns) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(new EmptyBorder(5, 0, 5, 0));

        textField = new JTextField();
        add(textField);
        if (columns > 0)
            textField.setColumns(columns);
        textField.getDocument().addDocumentListener(this);
        textField.addFocusListener(this);

        changed = false;
        listenerActive = true;
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        listenerActive = false;
        textField.setText(text);
        listenerActive = true;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!changed)
            return;

        changed = false;
        fireUpdateField();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed = true;
    }

    private void fireUpdateField() {
        if (!listenerActive)
            return;

        ActionEvent event = new ActionEvent(this, 0, EVENT_CHANGE);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
