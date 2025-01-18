package ui;

import data.DataManager;
import data.controlles.*;
import data.entities.*;
import ui.controls.DataTableColumn;
import utils.DateUtils;
import utils.NumericUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsPage extends JPanel {

    private final DataManager dataManager = DataManager.getInstance();

    private final DocumentController documents = dataManager.getDocuments();
    private final AccountController accounts = dataManager.getAccounts();
    private final CategoryController categories = dataManager.getCategories();
    private final ProductController products = dataManager.getProducts();
    private final UserController users = dataManager.getUsers();
    private final TagController tags = dataManager.getTags();

    private Date lastDate;

    private List<TagRef> tagsRefs;

    private final WorkPanel parentPanel;
    private JTable table;
    private DataTableModel tableModel;

    private JButton buttonAdd;
    private JButton buttonEdit;
    private JButton buttonDelete;

    static class CurrencyRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            BigDecimal number = (BigDecimal) value;
            super.getTableCellRendererComponent(table, number != null ? NumericUtils.numberToCurrencyString(number) : "", isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            return this;
        }

    }

    public static class DataTableModel extends AbstractTableModel {

        private final List<DataTableColumn> columns;
        private final List<Integer> ids;
        private final List<Object[]> data;

        private final boolean editableTable;

        public DataTableModel(boolean editableTable) {
            super();
            columns = new ArrayList<>();
            ids = new ArrayList<>();
            data = new ArrayList<>();
            this.editableTable = editableTable;
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

        public Class<?> getColumnClass(int columnIndex) {
            return columns.get(columnIndex).getColumnClass();
        }

        public String getColumnName(int col) {
            return columns.get(col).getName();
        }

        public Object getValueAt(int row, int col) {
            return data.get(row)[col];
        }

        public Integer getRowId(int row) {
            return row < ids.size() ? ids.get(row) : null;
        }

        public void setValueAt(Object value, int row, int col) {
            data.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }

        public boolean isCellEditable(int row, int col) {
            return editableTable;
        }

    }

    public TransactionsPage(final WorkPanel parentPanel) {
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout());
        //setBorder(BorderFactory.createLineBorder(Color.RED));

        initToolBar();
        initTable();
    }

    public void updateRows() {
        tableModel.clear();
        if (parentPanel.getPeriodBegin() == null || parentPanel.getPeriodEnd() == null)
            return;

        try {
            loadDocuments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            DocumentDetail mainDetail = document.getMainDetail();
            tableModel.addRow(document.getId(), new Object[] {
                    DateUtils.dateToStringISO(document.getDocDate()),
                    document.getTransGroup().name(),
                    mainDetail != null && mainDetail.getCategory() != null ? mainDetail.getCategory().getName() : "-",
                    mainDetail != null && mainDetail.getProduct() != null ? mainDetail.getProduct().getName() : "-",
                    null, //Deposit
                    document.getTotal(), //Withdrawal
                    mainDetail != null && mainDetail.getUser() != null ? mainDetail.getUser().getName() : "-",
                    getTagsList(document) //Tags
            });
        }
        table.updateUI();
    }

    private String getTagsList(Document document) {
        StringBuilder tagsList = new StringBuilder();

        for (var tagRef : tagsRefs) {
            if (tagRef.getDocument() == document)
                tagsList.append(tagsList.length() > 0 ? ", " : "").append(tagRef.getTag().getName());
        }

        return tagsList.toString();
    }

    public void createTransaction() {
        showTransactionForm(null);
    }

    public void editTransaction() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;

        Integer documentId = tableModel.getRowId(row);
        showTransactionForm(documentId);
    }

    public void deleteTransaction() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;

        int dialogResult = JOptionPane.showConfirmDialog(null, "Do you want to delete the transaction?","Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            int documentId = tableModel.getRowId(row);
            try {
                documents.deleteDocument(documentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            updateRows();
        }
    }

    public void updatePage() {
        updateRows();
    }

    private void initToolBar() {
        JPanel toolBarPanel = new JPanel();
        add(toolBarPanel, BorderLayout.NORTH);

        JLabel labelTransactions = new JLabel("Transactions");
        toolBarPanel.add(labelTransactions);

        buttonAdd = new JButton("Add new");
        toolBarPanel.add(buttonAdd);
        buttonAdd.addActionListener(e -> createTransaction());

        buttonEdit = new JButton("Edit");
        toolBarPanel.add(buttonEdit);
        buttonEdit.addActionListener(e -> editTransaction());

        buttonDelete = new JButton("Delete");
        toolBarPanel.add(buttonDelete);
        buttonDelete.addActionListener(e -> deleteTransaction());
    }

    private void initTable() {
        //Create table model
        tableModel = new DataTableModel(false);
        tableModel.addColumn("Date", String.class);
        tableModel.addColumn("Transfer", String.class);
        tableModel.addColumn("Category", String.class);
        tableModel.addColumn("Product", String.class);
        tableModel.addColumn("Deposit", BigDecimal.class);
        tableModel.addColumn("Withdrawal", BigDecimal.class);
        tableModel.addColumn("User", String.class);
        tableModel.addColumn("Tags", String.class);

        //Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Create cell renderer for currency cols
        table.setDefaultRenderer(BigDecimal.class, new CurrencyRenderer());

        //Add mouse listener
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1)
                    mouseDblClickEvent(row);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);
        setVisible(true);
    }

    private void loadDocuments() throws Exception {
        Date periodBegin = parentPanel.getPeriodBegin();
        Date periodEnd = parentPanel.getPeriodEnd();

        accounts.loadAccountsList();
        categories.loadCategoriesList();
        products.loadProductsList();
        tags.loadTagsList();
        documents.loadDocumentsList(periodBegin, periodEnd);
        tagsRefs = tags.loadDocumentsTags(documents, periodBegin, periodEnd);
    }

    private void showTransactionForm(Integer documentId) {
        TransactionEditForm transactionEditForm;
        Window parentDialog = parentPanel.getParentWindow();
        Date periodBegin = parentPanel.getPeriodBegin();
        Date periodEnd = parentPanel.getPeriodEnd();

        try {
            if (documentId == null) {
                Account account = null;
                try {
                    accounts.loadAccountsList();
                    account = accounts.get(0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                //Select a date for the new document
                Date docDate = lastDate != null ? lastDate : DateUtils.getCurrentDate();
                if (periodEnd.compareTo(periodBegin) != 0) { //Month
                    if (docDate.before(periodBegin) || docDate.after(periodEnd)) //todo: bug: not includes the last day (remove time)
                        docDate = periodBegin;
                } else {
                    docDate = periodBegin;
                }
                transactionEditForm = new TransactionEditForm(parentDialog, account, docDate, TransGroup.EXPENSES, null);
            } else {
                transactionEditForm = new TransactionEditForm(parentDialog, null, null, TransGroup.EXPENSES, documentId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean result = transactionEditForm.show();
        if (result) {
            Document document = transactionEditForm.getDocument();
            lastDate = document.getDocDate();
            updateRows();
        }
    }

    private void mouseDblClickEvent(int row) {
        editTransaction();
    }
}
