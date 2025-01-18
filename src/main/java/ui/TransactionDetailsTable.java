package ui;

import data.entities.*;
import ui.controls.DataTableColumn;
import utils.NumericUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionDetailsTable extends JPanel implements ListSelectionListener {

    private Document document;

    private BigDecimal mainDetailSubtotal;

    private final JTable table;
    private final TransactionDetailsTableModel tableModel;

    private JButton buttonAddPosition;
    private JButton buttonCopyPosition;
    private JButton buttonExcludePosition;
    private JButton buttonDeletePosition;

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_SELECT = "select";
    public static final String EVENT_CHANGE = "change";

    private boolean listenerActive;

    static class TransactionDetailsTableModel extends AbstractTableModel {

        private final List<DataTableColumn> columns;
        private final List<Object> links;
        private final List<Object[]> data;

        public TransactionDetailsTableModel() {
            super();
            columns = new ArrayList<>();
            links = new ArrayList<>();
            data = new ArrayList<>();
        }

        public void addColumn(String columnName, Class<?> aClass) {
            columns.add(new DataTableColumn(columnName, aClass, 0));
        }

        public void addRow(Object object, Object[] rowData) {
            links.add(object);
            data.add(rowData);
        }

        public void updateRow(Object object, Object[] rowObjects) {
            int row = getRowByObject(object);
            if (row == -1)
                return;

            data.set(row, rowObjects);
            fireTableRowsUpdated(row, row);
        }

        public void clear() {
            links.clear();
            data.clear();
        }

        public void updateTable() {
            fireTableDataChanged();
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

        public Object getRowObject(int row) {
            return links.get(row);
        }

        public void setValueAt(Object value, int row, int col) {
            data.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return columns.get(columnIndex).getColumnClass();
        }

        private int getRowByObject(Object object) {
            for (var i = 0; i < getRowCount(); i++) {
                if (links.get(i).equals(object))
                    return i;
            }

            return -1;
        }

    }

    static class NumberRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            BigDecimal number = (BigDecimal) value;
            String formattedNumber = number != null ? (column == 3 ? NumericUtils.numberToQuantityString(number) : NumericUtils.numberToCurrencyString(number)) : "";
            super.getTableCellRendererComponent(table, formattedNumber, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            return this;
        }

    }

    public TransactionDetailsTable() {
        super();

        setLayout(new BorderLayout());

        listenerActive = false;

        add(initControlPanel(), BorderLayout.NORTH);

        tableModel = new TransactionDetailsTableModel();
        tableModel.addColumn("Category", Category.class);
        tableModel.addColumn("Product", String.class);
        tableModel.addColumn("Price", BigDecimal.class);
        tableModel.addColumn("Quantity", BigDecimal.class);
        tableModel.addColumn("Subtotal", BigDecimal.class);
        tableModel.addColumn("Person", String.class);
        tableModel.addColumn("Tags", String.class);
        tableModel.addColumn("Notes", String.class);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(BigDecimal.class, new NumberRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setMainDetailSubtotal(BigDecimal mainDetailSubtotal) {
        this.mainDetailSubtotal = mainDetailSubtotal;
    }

    public void updateTable() {
        if (document == null || document.getDetails() == null)
            return;

        tableModel.clear();
        for (var documentDetail : document.getDetails()) {
            tableModel.addRow(documentDetail, documentDetailToObjectArray(documentDetail));
        }
        tableModel.updateTable();
    }

    public void updateRow(DocumentDetail documentDetail) {
        if (documentDetail == null)
            return;

        tableModel.updateRow(documentDetail, documentDetailToObjectArray(documentDetail));
    }

    public void setSelectedRow(int row) {
        listenerActive = false;
        if (row >= 0 && row < tableModel.getRowCount())
            table.setRowSelectionInterval(row, row);
        listenerActive = true;
    }

    public void selectRowByDetail(DocumentDetail documentDetail) {
        var row = getRowNumber(documentDetail);
        setSelectedRow(row);
    }

    public DocumentDetail getSelectedDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || document == null || document.getDetails() == null)
            return null;

        DocumentDetail selectedDetail = null;
        Object detailObject = tableModel.getRowObject(row);
        for (var detail : document.getDetails()) {
            if (detail.equals(detailObject)) {
                selectedDetail = detail;
                break;
            }
        }

        return selectedDetail;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            fireActionChange(EVENT_SELECT);
            updateControlPanel();
        }
    }

    private JPanel initControlPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        buttonAddPosition = new JButton("Add position");
        detailPanel.add(buttonAddPosition);
        buttonAddPosition.addActionListener(e -> actionAddPosition());

        buttonCopyPosition = new JButton("Copy position");
        detailPanel.add(buttonCopyPosition);
        buttonCopyPosition.addActionListener(e -> actionCopyPosition());

        buttonExcludePosition = new JButton("Exclude position");
        detailPanel.add(buttonExcludePosition);
        buttonExcludePosition.addActionListener(e -> actionExcludePosition());

        buttonDeletePosition = new JButton("Delete position");
        detailPanel.add(buttonDeletePosition);
        buttonDeletePosition.addActionListener(e -> actionDeletePosition());

        return detailPanel;
    }

    private void updateControlPanel() {
        boolean enabled = document != null && document.getDetails() != null && !document.getDetails().isEmpty();
        DocumentDetail detail = getSelectedDetail();
        if (detail == null)
            enabled = false;

        buttonAddPosition.setEnabled(enabled && !document.isExclude());
        buttonCopyPosition.setEnabled(enabled && (!document.isExclude() || !detail.isMain()));
        buttonExcludePosition.setEnabled(enabled && (document.isExclude() || document.getDetails().size() == 1));
        buttonDeletePosition.setEnabled(enabled && document.getDetails().size() > 1 && (!document.isExclude() || !detail.isMain()));
    }

    private int getRowNumber(DocumentDetail documentDetail) {
        if (documentDetail == null)
            return -1;

        for (var i = 0; i < tableModel.getRowCount(); i++) {
            if (documentDetail.equals(tableModel.getRowObject(i)))
                return i;
        }

        return -1;
    }

    private Object[] documentDetailToObjectArray(DocumentDetail documentDetail) {
        return new Object[] {
                documentDetail.getCategory() != null ? documentDetail.getCategory().getName() : "",
                documentDetail.getProduct() != null ? documentDetail.getProduct().getName() : "",
                documentDetail.getPrice(),
                documentDetail.getQuantity(),
                document != null && document.isExclude() && documentDetail.isMain() ? mainDetailSubtotal : documentDetail.getSubtotal(), //Calculated subtotal if excluded mode
                documentDetail.getUser() != null ? documentDetail.getUser().getName() : "",
                (documentDetail.isMain() ? (document != null && document.isExclude() ? "#" : "*") + " " : "") + Tag.tagsListToString(documentDetail.getTags()),
                documentDetail.getNotes()
        };
    }

    private void fireActionChange(String command) {
        if (!listenerActive)
            return;

        ActionEvent event = new ActionEvent(this, 0, command);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    private void actionAddPosition() {
        if (document == null || document.isExclude())
            return;

        DocumentDetail documentDetail = new DocumentDetail(document);
        document.getDetails().add(documentDetail);
        updateTable();
        selectRowByDetail(documentDetail);
        fireActionChange(EVENT_CHANGE);
    }

    private void actionCopyPosition() {
        DocumentDetail detail = getSelectedDetail();
        if (document == null || detail == null)
            return;
        if (document.isExclude() && detail.isMain())
            return;

        DocumentDetail documentDetail = new DocumentDetail(document);
        documentDetail.setCategory(detail.getCategory());
        documentDetail.setProduct(detail.getProduct());
        if (detail.getTags() != null && !detail.getTags().isEmpty())
            documentDetail.setTags(new ArrayList<>(detail.getTags()));
        document.getDetails().add(documentDetail);
        updateTable();
        selectRowByDetail(documentDetail);
        fireActionChange(EVENT_CHANGE);
    }

    private void actionExcludePosition() {
        if (document == null || document.getDetails() == null)
            return;
        if (!document.isExclude() && document.getDetails().size() != 1)
            return;

        DocumentDetail parentDetail = document.getDetails().get(0);
        if (parentDetail == null || parentDetail.getSubtotal() == null || parentDetail.getSubtotal().equals(BigDecimal.ZERO))
            return;

        if (!document.isExclude()) {
            document.setExclude(true);
            parentDetail.setMain(true);
            parentDetail.setPrice(parentDetail.getSubtotal());
            parentDetail.setQuantity(BigDecimal.ONE);
            parentDetail.modified();
        }

        DocumentDetail documentDetail = new DocumentDetail(document);
        document.getDetails().add(documentDetail);
        updateTable();
        selectRowByDetail(documentDetail);
        fireActionChange(EVENT_CHANGE);
    }

    private void actionDeletePosition() {
        DocumentDetail detail = getSelectedDetail();
        if (document == null || detail == null || document.getDetails() == null || document.getDetails().size() <= 1)
            return;
        if (document.isExclude() && detail.isMain())
            return;

        document.getDetails().remove(detail);

        if (document.isExclude()) {
            if (document.getDetails().size() == 1)
                document.setExclude(false);
            DocumentDetail mainDetail = document.getMainDetail();
            if (mainDetail != null)
                document.getMainDetail().modified();
        }

        updateTable();
        selectRowByDetail(null);
        fireActionChange(EVENT_CHANGE);
    }

}
