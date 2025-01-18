package ui.controls;

import data.controlles.TagController;
import data.entities.Tag;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TagsField extends JPanel {

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_CHANGE = "change";

    private final JDialog parentDialog;

    private final TagController tags;
    private List<Tag> selectedTags;

    private final JTextField tagsTextField;
    private final JButton buttonTagsSelect;

    public TagsField(JDialog parentDialog, TagController tags) {
        this.parentDialog = parentDialog;
        this.tags = tags;

        tagsTextField = new JTextField();
        tagsTextField.setEditable(false);
        add(tagsTextField);

        buttonTagsSelect = new JButton("...");
        add(buttonTagsSelect);
        buttonTagsSelect.addActionListener(this::actionTagsSelect);
    }

    public void setValues(List<Tag> selectedTags) {
        this.selectedTags = selectedTags;
        updateField();
    }

    public List<Tag> getValues() {
        return selectedTags;
    }

    public void clear() {
        selectedTags.clear();
        updateField();
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void setEnabled(boolean enabled) {
        buttonTagsSelect.setEnabled(enabled);
    }

    private void updateField() {
        tagsTextField.setText(Tag.tagsListToString(selectedTags));
    }

    private void actionTagsSelect(ActionEvent actionEvent) {
        TagsSelectForm tagsSelectForm;
        try {
            tagsSelectForm = new TagsSelectForm(parentDialog, tags);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        tagsSelectForm.setSelectedTags(selectedTags);
        tagsSelectForm.showDialog();
        if (tagsSelectForm.getSelectedTags() != null) {
            selectedTags = tagsSelectForm.getSelectedTags();
            updateField();
            fireActionChange();
        }
    }

    private void fireActionChange() {
        ActionEvent event = new ActionEvent(this, 0, EVENT_CHANGE);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
