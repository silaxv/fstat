package ui.controls;

import data.controlles.CategoryController;
import data.entities.Category;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CategorySelectForm extends JDialog {

    private final CategoryController categories;

    private Category selectedCategory;

    DataTreeTable dataTreeTable;

    public CategorySelectForm(final JDialog parentDialog, final CategoryController categories) throws Exception {
        super(parentDialog, "Categories", ModalityType.TOOLKIT_MODAL);

        if (categories == null)
            throw new Exception("A categories table passed into the dialog is empty!");
        this.categories = categories;

        initDialog();

        setLocationRelativeTo(parentDialog);
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category category) {
        selectedCategory = category;
    }

    public void showDialog() {
        //Select current node
        dataTreeTable.setSelectedRow(selectedCategory != null ? selectedCategory.getId() : null);

        setVisible(true);
    }

    private void initDialog() {
        setBounds(0, 0, 600, 600);
        Container dialogContainer = getContentPane();
        dialogContainer.setLayout(new BorderLayout());

        //Root panel
        JPanel rootPanel = new JPanel();
        dialogContainer.add(rootPanel, BorderLayout.CENTER);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.LINE_AXIS));
        rootPanel.setFocusCycleRoot(true);
        rootPanel.add(initTreeList());

        //Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        dialogContainer.add(buttonsPanel, BorderLayout.SOUTH);
        JButton buttonSelect = new JButton("Select");
        buttonSelect.addActionListener(e -> closeAction(true));
        buttonsPanel.add(buttonSelect);
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(e -> closeAction(false));
        buttonsPanel.add(buttonCancel);

        //Keys
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> closeAction(false), escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel initTreeList() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());

        formPanel.add(new JLabel("Select a category"), BorderLayout.NORTH);

        dataTreeTable = new DataTreeTable();
        formPanel.add(dataTreeTable, BorderLayout.CENTER);

        addTreeItem(null, null);
        dataTreeTable.update();

        dataTreeTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JXTreeTable table = (JXTreeTable) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1)
                    closeAction(true);
            }
        });

        return formPanel;
    }

    private void addTreeItem(Category category, DataTreeTable.DataTreeTableNode parentNode) {
        var node = category != null ? dataTreeTable.addNode(category.getId(), category.getName(), parentNode) : null;
        for (var item : categories.getCategoriesByParent(category)) {
            addTreeItem(item, node);
        }
    }

    private void closeAction(boolean saveResult) {
        if (saveResult) {
            var selectedItemId = dataTreeTable.getSelectedItemId();
            if (selectedItemId != null) {
                selectedCategory = categories.getCategoryById(selectedItemId);
                setVisible(false);
            }
        } else {
            selectedCategory = null;
            setVisible(false);
        }
    }

}
