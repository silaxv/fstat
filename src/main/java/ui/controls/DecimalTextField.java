package ui.controls;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DecimalTextField extends JPanel implements FocusListener {

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_CHANGE = "change";

    private final JFormattedTextField textField;
    private BigDecimal value;

    private boolean listenerActive;

    public DecimalTextField(int integerDigits, int fractionDigits, int columns) {
        textField = new JFormattedTextField();
        add(textField);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(fractionDigits);
        numberFormat.setMinimumFractionDigits(fractionDigits);
        numberFormat.setMaximumIntegerDigits(integerDigits);
        numberFormat.setGroupingUsed(false);

        NumberFormatter numberFormatter = new NumberFormatter(numberFormat);
        DefaultFormatterFactory numberFormatFactory = new DefaultFormatterFactory(numberFormatter);
        textField.setFormatterFactory(numberFormatFactory);

        textField.addFocusListener(this);
        textField.addPropertyChangeListener("value", this::actionPropertyChange);

        textField.setColumns(columns);
        reset();

        listenerActive = true;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        listenerActive = false;
        this.value = value;
        textField.setValue(value);
        listenerActive = true;
    }

    public void reset() {
        listenerActive = false;
        this.value = BigDecimal.ZERO;
        textField.setValue(0.0);
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textField.selectAll();
            }
        });
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    public void requestFocus() {
        textField.requestFocus();
    }

    private void actionPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object objectValue = textField.getValue();
        double value;
        if (objectValue instanceof Number) {
            value = ((Number) objectValue).doubleValue();
            this.value = BigDecimal.valueOf(value);
        } else {
            reset();
        }

        fireUpdateField();
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
