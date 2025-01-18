package ui.controls;

import data.controlles.TagController;
import data.entities.Tag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class TagsSelectForm extends JDialog {

    private final TagController tags;

    private List<Tag> selectedTags;
    private CheckBoxList checkBoxList;

    public TagsSelectForm(final JDialog parentDialog, final TagController tags) throws Exception {
        super(parentDialog, "Tags", ModalityType.TOOLKIT_MODAL);

        if (tags == null)
            throw new Exception("A tags table passed into the dialog is empty!");
        this.tags = tags;

        initDialog();

        setLocationRelativeTo(parentDialog);
    }

    public List<Tag> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<Tag> selectedTags) {
        this.selectedTags = selectedTags;

        for (var tag : selectedTags) {
            checkBoxList.setItemState(tag.getId(), true);
        }
    }

    public void showDialog() {
        setVisible(true);
    }

    private void initDialog() {
        setBounds(0, 0, 400, 400);
        Container dialogContainer = getContentPane();
        dialogContainer.setLayout(new BorderLayout());

        //Root panel
        JPanel rootPanel = new JPanel();
        dialogContainer.add(rootPanel, BorderLayout.CENTER);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.LINE_AXIS));
        rootPanel.setFocusCycleRoot(true);
        rootPanel.add(initList());

        //Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        dialogContainer.add(buttonsPanel, BorderLayout.SOUTH);
        JButton buttonSet = new JButton("Set");
        buttonSet.addActionListener(e -> closeAction(true));
        buttonsPanel.add(buttonSet);
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(e -> closeAction(false));
        buttonsPanel.add(buttonCancel);

        //Keys
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> closeAction(false), escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel initList() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());

        formPanel.add(new JLabel("Select a tag"), BorderLayout.NORTH);

        checkBoxList = new CheckBoxList();
        formPanel.add(checkBoxList, BorderLayout.CENTER);

        for (var i = 0; i < tags.size(); i++) {
            var item = tags.get(i);
            checkBoxList.addItem(item.getId(), item.getName(), false);
        }

        return formPanel;
    }

    private void closeAction(boolean saveResult) {
        if (saveResult) {
            selectedTags.clear();
            for (var i = 0; i < tags.size(); i++) {
                var tag = tags.get(i);
                if (checkBoxList.getItemState(tag.getId()))
                    selectedTags.add(tag);
            }
        } else {
            selectedTags = null;
        }
        setVisible(false);
    }

}
