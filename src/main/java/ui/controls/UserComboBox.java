package ui.controls;

import data.controlles.UserController;
import data.entities.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class UserComboBox extends JPanel {

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_CHANGE = "change";

    private final UserController users;

    private User selectedUser;

    private final JComboBox<String> userField;
    private final JButton buttonUserClear;
    private int[] itemsIds;
    private boolean listenerActive;

    public UserComboBox(UserController users) {
        this.users = users;
        listenerActive = false;

        userField = new JComboBox<>();
        add(userField);
        userField.setEditable(false);
        userField.addItemListener(this::itemListener);

        buttonUserClear = new JButton("X");
        add(buttonUserClear);
        buttonUserClear.addActionListener(e -> actionClearUserField());
    }

    public void setList() {
        listenerActive = false;

        userField.removeAllItems();
        itemsIds = users.size() > 0 ? new int[users.size()] : null;

        for (var i = 0; i < users.size(); i++) {
            userField.addItem(users.get(i).getName());
            itemsIds[i] = users.get(i).getId();
        }

        userField.setSelectedIndex(-1);

        listenerActive = true;
    }

    public void setValue(User user) {
        listenerActive = false;

        if (user == null) {
            userField.setSelectedIndex(-1);
            listenerActive = true;
            return;
        }

        int selectedIndex = -1;
        for (var i = 0; i < itemsIds.length; i++) {
            if (itemsIds[i] == user.getId()) {
                selectedIndex = i;
                break;
            }
        }
        userField.setSelectedIndex(selectedIndex);

        listenerActive = true;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void setEnabled(boolean enabled) {
        userField.setEnabled(enabled);
        buttonUserClear.setEnabled(enabled);
    }

    private void itemListener(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() != ItemEvent.SELECTED || !listenerActive)
            return;

        //Get a user by an index
        int selectedUserId = itemsIds[userField.getSelectedIndex()];
        selectedUser = users.getUserById(selectedUserId);

        fireActionChange();
    }

    private void actionClearUserField() {
        listenerActive = false;
        userField.setSelectedItem(null);
        selectedUser = null;
        listenerActive = true;

        fireActionChange();
    }

    private void fireActionChange() {
        if (!listenerActive)
            return;

        ActionEvent event = new ActionEvent(this, 0, EVENT_CHANGE);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
