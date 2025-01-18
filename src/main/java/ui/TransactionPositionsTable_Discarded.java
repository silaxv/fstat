package ui;

import data.controlles.CategoryController;
import data.controlles.ProductController;
import data.entities.*;
import ui.controls.CategorySelectForm;
import ui.controls.DataTableColumn;
import ui.controls.ProductComboBox;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionPositionsTable_Discarded extends JPanel {

    private final TransactionPositionsTableModel positionsTableModel;

    private final JDialog dialog;
    private final CategoryController categories;
    private ProductController products;

    static class TransactionPositionsTableModel extends AbstractTableModel {

        private final List<DataTableColumn> columns;
        private final List<Integer> ids;
        private final List<Object[]> data;

        public TransactionPositionsTableModel() {
            super();
            columns = new ArrayList<>();
            ids = new ArrayList<>();
            data = new ArrayList<>();
        }

        public void addColumn(String columnName, Class<?> aClass) {
            columns.add(new DataTableColumn(columnName, aClass, 0));
        }

        public void addRow(int id, Object[] rowData) {
            ids.add(id);
            data.add(rowData);
        }

        public void clear() {
            ids.clear();
            data.clear();
        }

        public int getColumnCount() {
            return columns.size();
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return columns.get(col).getName();
        }

        public Object getValueAt(int row, int col) {
            return data.get(row)[col];
        }

        public Integer getRowId(int row) {
            return ids.get(row);
        }

        public void setValueAt(Object value, int row, int col) {
            data.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return columns.get(columnIndex).getColumnClass();
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

    }

    static class CategoryRenderer extends JLabel implements TableCellRenderer {

        public CategoryRenderer() {
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Category category = (Category) value;
            setText(category != null ? category.getName() : "<Select>");

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }

    }

    static class CategoryEditor extends AbstractCellEditor implements TableCellEditor {

        JDialog dialog;
        CategoryController categories;
        private Category currentCategory;
        private JTable table;
        private final JButton button;

        public CategoryEditor(JDialog dialog, CategoryController categories) {
            this.dialog = dialog;
            this.categories = categories;

            //Set up the editor (from the table's point of view),
            //which is a button.
            button = new JButton();
            button.addActionListener(e -> buttonAction());
            button.setBorderPainted(false);

            //...
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentCategory;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentCategory = (Category) value;
            this.table = table;
            button.setBackground(table.getSelectionBackground());

            return button;
        }

        private void buttonAction() {
            CategorySelectForm categorySelectForm;
            try {
                categorySelectForm = new CategorySelectForm(dialog, categories);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            categorySelectForm.setVisible(true);
            var selectedCategory = categorySelectForm.getSelectedCategory();
            if (selectedCategory != null) {
                currentCategory = selectedCategory;
            }

            //Make the renderer reappear.
            fireEditingStopped();
        }

    }

    static class ProductEditor extends AbstractCellEditor implements TableCellEditor {

        private ProductSelector currentProduct;
        private JTable table;
        private final ProductComboBox comboBox;

        public ProductEditor(ProductController products) {
            //Set up the editor (from the table's point of view), which is a ComboBox
            comboBox = new ProductComboBox(products);

            comboBox.addActionListener(this::comboBoxAction);
            //...
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            currentProduct.setTextValue((String) comboBox.getEditor().getItem());
            System.out.println("Get cell: " + currentProduct.textValue);

            return currentProduct;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentProduct = (ProductSelector) value;
            this.table = table;

            //Update drop list
            TransactionPositionsTableModel tableModel = (TransactionPositionsTableModel) (table.getModel());
            Category category = (Category) tableModel.getValueAt(row, 0);
            comboBox.setList(category/*, currentProduct.textValue*/);

            //System.out.println("getTableCellEditorComponent: " + currentProduct.textValue);

            return comboBox;
        }

        private void comboBoxAction(ActionEvent e) {
            //System.out.println(e.getActionCommand());
            if (e.getActionCommand().equals("comboBoxChanged")) {
                //currentProduct.setTextValue((String) comboBox.getEditor().getItem());
                //System.out.println("comboBoxAction: " + currentProduct.textValue);
            }
        }

    }

    static class ProductSelector {

        private final Product product;
        private String textValue;

        public ProductSelector(Product product) {
            this.product = product;
            textValue = product != null ? product.getName() : "";
        }

        public Product getProduct() {
            return product;
        }

        public String getTextValue() {
            return textValue;
        }

        public void setTextValue(String textValue) {
            this.textValue = textValue;
        }

        @Override
        public String toString() {
            return textValue;
        }

    }

    public TransactionPositionsTable_Discarded(JDialog dialog, CategoryController categories, ProductController products) {
        super();

        this.dialog = dialog;
        this.categories = categories;
        this.products = products;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel tableControls = new JPanel();
        add(tableControls);

        positionsTableModel = new TransactionPositionsTableModel();
        positionsTableModel.addColumn("Category", Category.class);
        positionsTableModel.addColumn("Product", ProductSelector.class);
        positionsTableModel.addColumn("Price", BigDecimal.class);
        positionsTableModel.addColumn("Quantity", BigDecimal.class);
        positionsTableModel.addColumn("Subtotal", BigDecimal.class);
        positionsTableModel.addColumn("Person", User.class);
        positionsTableModel.addColumn("Tags", Tag.class);
        positionsTableModel.addColumn("Notes", String.class);

        JTable table = new JTable(positionsTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(table);
        add(sp);

        table.setDefaultRenderer(Category.class, new CategoryRenderer());
        table.setDefaultEditor(Category.class, new CategoryEditor(dialog, categories));
        table.setDefaultEditor(ProductSelector.class, new ProductEditor(products));
    }

    public void addPositionRow(DocumentDetail documentDetail) {
        Object[] rowColumns = new Object[] {
                documentDetail.getCategory(),
                new ProductSelector(documentDetail.getProduct()),
                documentDetail.getPrice(),
                documentDetail.getQuantity(),
                documentDetail.getSubtotal(),
                documentDetail.getUser(),
                documentDetail.getTags(),
                documentDetail.getNotes()
        };
        positionsTableModel.addRow(documentDetail.getId(), rowColumns);
    }


}
