package ui.controls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckBoxList extends JList<Object> {

    private final DefaultListModel<Object> model;

    protected class CellRenderer implements ListCellRenderer<Object> {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ListItem item = (ListItem) value;
            JCheckBox checkbox = new JCheckBox(item.name, item.state);
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setOpaque(false);

            return checkbox;
        }

    }

    private static class ListItem {
        public int id;
        public String name;
        public boolean state;
    }

    public CheckBoxList() {
        model = new DefaultListModel<>();
        setModel(model);

        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    ListItem item = (ListItem) model.getElementAt(index);
                    item.state = !item.state;
                    repaint();
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void addItem(int id, String name, boolean state) {
        ListItem item = new ListItem();
        item.id = id;
        item.name = name;
        item.state = state;
        model.add(model.size(), item);
    }

    public int listSize() {
        return model.size();
    }

    public void setItemState(int id, boolean state) {
        ListItem item = getItemById(id);
        if (item != null)
            item.state = state;
    }

    public boolean getItemState(int id) {
        ListItem item = getItemById(id);

        return item != null && item.state;
    }

    private ListItem getItemById(int id) {
        for (var i = 0; i < model.size(); i++) {
            ListItem item = (ListItem) model.get(i);
            if (item != null && item.id == id)
                return item;
        }

        return null;
    }

}
