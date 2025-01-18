package ui;

import data.DataManager;
import data.controlles.*;
import data.entities.*;
import ui.controls.NotesTextField;
import utils.NumericUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TransactionEditForm {

    private final JDialog dialog;

    private final DataManager dataManager = DataManager.getInstance();

    private final DocumentController documents = dataManager.getDocuments();
    private final AccountController accounts = dataManager.getAccounts();
    private final CategoryController categories = dataManager.getCategories();
    private final ProductController products = dataManager.getProducts();
    private final UserController users = dataManager.getUsers();
    private final TagController tags = dataManager.getTags();

    private Document document;
    private final boolean newDocument;
    private boolean saveResult;
    private final List<Product> newProducts;

    private JComboBox<String> accountField;
    private JFormattedTextField dateField;
    private JCheckBox activeCheckBox;
    private TransactionDetailsTable detailsTable;
    private TransactionDetailEditForm detailEditForm;
    private NotesTextField notesField;
    private JTextField totalField;
    private JLabel errorMessageField;

    public TransactionEditForm(final Window parentWindow, final Account account, final Date documentDate, final TransGroup transGroup, final Integer documentId) throws Exception {
        newDocument = (documentId == null);
        dialog = new JDialog(parentWindow, "Transaction" + (newDocument ? " (new)" : ""), Dialog.ModalityType.DOCUMENT_MODAL);
        saveResult = false;
        newProducts = new ArrayList<>();

        if (newDocument) {
            initNewDocument(account, documentDate, transGroup);
            loadTables(transGroup);
        } else {
            loadTables(transGroup);
            loadDocument(documentId);
            setupExcludedDocument();
        }

        //Init dialog window
        initDialog();

        //Update control's data
        updateData();

        dialog.setLocationRelativeTo(parentWindow);
    }

    public boolean show() {
        if (document != null)
            dialog.setVisible(true);

        return saveResult;
    }

    public Document getDocument() {
        return document;
    }

    private void initNewDocument(final Account account, final Date documentDate, final TransGroup transGroup) {
        if (account == null)
            throw new IllegalArgumentException("Account is null!");
        if (documentDate == null)
            throw new IllegalArgumentException("Document's date is null!");
        if (transGroup == null)
            throw new IllegalArgumentException("Trans group is null!");

        document = new Document();
        document.setAct(true);
        document.setAccount(account);
        document.setDocDate(documentDate);
        document.setTransGroup(transGroup);
        document.setNotes("");

        //Details
        var documentDetails = document.getDetails();
        DocumentDetail detail = new DocumentDetail(document);
        documentDetails.add(detail);
        detail.setMain(true);
        document.setDetails(documentDetails);
    }

    private void loadDocument(final Integer documentId) throws Exception {
        if (documentId == null)
            throw new IllegalArgumentException("Document's id is null!");

        document = documents.loadDocumentById(documentId);
    }

    private void setupExcludedDocument() {
        if (document == null || !document.isExclude() || document.getDetails() == null || document.getDetails().isEmpty())
            return;

        DocumentDetail mainDetail = document.getMainDetail();
        if (mainDetail == null)
            return;

        BigDecimal total = BigDecimal.ZERO;
        for (var detail : document.getDetails()) {
            total = total.add(detail.getSubtotal());
        }
        mainDetail.setSubtotal(total);
    }

    private void saveDocument() throws Exception {
        if (document == null)
            throw new IllegalArgumentException("Document is null!");

        //Create or update document
        if (newDocument) {
            int documentId = documents.addDocument(document.getDocDate(), document.getTransGroup(), document.isAct(), document.isExclude(), document.getAccount(), document.getNotes(), document.getDetails(), newProducts);
            document.setId(documentId);
        } else {
            documents.updateDocument(document.getId(), document.getDocDate(), document.getTransGroup(), document.isAct(), document.isExclude(), document.getAccount(), document.getNotes(), document.getDetails(), newProducts);
        }
        System.out.println("Document saved (id = " + document.getId() + ")");
    }

    private void loadTables(TransGroup transGroup) throws Exception {
        categories.loadCategoriesListByTransGroup(transGroup);
        products.loadProductsList(transGroup, null);
        users.loadUsersList();
    }

    private void initDialog() {
        if (document == null)
            return;

        dialog.setBounds(0, 0, 800, 500);
        Container dialogContainer = dialog.getContentPane();
        dialogContainer.setLayout(new BorderLayout());

        //1. Root panel
        JPanel rootPanel = new JPanel();
        dialogContainer.add(rootPanel, BorderLayout.CENTER);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setFocusCycleRoot(true);
        //rootPanel.setBorder(BorderFactory.createLineBorder(Color.RED));

        //1.1. Document panel
        JPanel documentPanel = new JPanel();
        rootPanel.add(documentPanel, BorderLayout.NORTH);
        documentPanel.setLayout(new BoxLayout(documentPanel, BoxLayout.Y_AXIS));
        //documentPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));

        //1.1.1. Document form (document controls)
        documentPanel.add(initDocumentForm());

        //1.1.2. Detail form (for a selected document's detail)
        detailEditForm = new TransactionDetailEditForm(dialog, categories, products, users, tags);
        detailEditForm.addActionListener(this::actionDetailEditFormChange);
        documentPanel.add(detailEditForm);

        //1.2. Details table
        detailsTable = new TransactionDetailsTable();
        rootPanel.add(detailsTable, BorderLayout.CENTER);
        detailEditForm.setDetailsTable(detailsTable);
        detailsTable.addActionListener(this::actionDetailsTableChange);

        //1.3. Document bottom form
        rootPanel.add(initDocumentBottomForm(), BorderLayout.SOUTH);

        //2. Bottom panel
        dialogContainer.add(initBottomPanel(), BorderLayout.SOUTH);

        initDialogKeys(rootPanel);
    }

    private JPanel initDocumentForm() {
        JPanel documentForm = new JPanel();
        documentForm.setLayout(new BoxLayout(documentForm, BoxLayout.Y_AXIS));

        //Account / Date / Active panel
        JPanel accountRow = new JPanel();
        documentForm.add(accountRow);

        JLabel accountLabel = new JLabel("Account:");
        accountRow.add(accountLabel);
        accountField = new JComboBox<>();
        accountRow.add(accountField);
        accountField.setEditable(false);
        accountLabel.setLabelFor(accountField);
        accountField.setEditable(false);
        accountSetList();
        if (document.getAccount() != null)
            accountSetValue(document.getAccount());

        JLabel dateLabel = new JLabel("Date:");
        accountRow.add(dateLabel);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateField = new JFormattedTextField(dateFormat);
        accountRow.add(dateField);
        dateLabel.setLabelFor(dateField);
        dateField.setName("Date");
        dateField.setColumns(10);
        dateField.setEditable(true);
        if (document.getDocDate() != null) {
            dateField.setValue(document.getDocDate());
        }

        activeCheckBox = new JCheckBox();
        accountRow.add(activeCheckBox);
        activeCheckBox.setText("Active");
        activeCheckBox.setSelected(document.isAct());

        return documentForm;
    }

    private JPanel initDocumentBottomForm() {
        JPanel documentBottomForm = new JPanel();
        documentBottomForm.setLayout(new BorderLayout());

        //Transaction notes
        JLabel notesLabel = new JLabel("Transaction notes:");
        documentBottomForm.add(notesLabel, BorderLayout.WEST);
        notesField = new NotesTextField(0);
        notesLabel.setLabelFor(notesField);
        documentBottomForm.add(notesField, BorderLayout.CENTER);
        if (document.getNotes() != null)
            notesField.setText(document.getNotes());

        //Total
        JPanel totalPanel = new JPanel();
        documentBottomForm.add(totalPanel, BorderLayout.EAST);
        totalPanel.add(new JLabel("Total:"));
        totalField = new JTextField();
        totalField.setColumns(12);
        totalField.setHorizontalAlignment(SwingConstants.RIGHT);
        totalField.setEditable(false);
        totalPanel.add(totalField);
        recalculateTotal();

        return documentBottomForm;
    }

    private void accountSetList() {
        accountField.removeAllItems();
        for (var i = 0; i < accounts.size(); i++) {
            Account curAccount = accounts.get(i);
            if (curAccount != null && curAccount.getName() != null)
                accountField.addItem(curAccount.getName());
        }
    }

    private void accountSetValue(Account account) {
        if (account == null) {
            accountField.setSelectedIndex(-1);
            return;
        }

        int selectedIndex = -1;
        for (var i = 0; i < accounts.size(); i++) {
            Account curAccount = accounts.get(i);
            if (curAccount != null && curAccount.getId() == account.getId()) {
                selectedIndex = i;
                break;
            }
        }
        accountField.setSelectedIndex(selectedIndex);
    }

    private Account accountGetValue() {
        Account account = null;

        int selectedAccountIndex = accountField.getSelectedIndex();
        if (selectedAccountIndex >= 0 && selectedAccountIndex < accounts.size())
            account = accounts.get(selectedAccountIndex);

        return account;
    }

    private void recalculateTotal() {
        if (document == null || document.getDetails() == null)
            return;

        BigDecimal total = BigDecimal.ZERO;
        if (!document.isExclude()) { //Normal positions
            for (var detail : document.getDetails()) {
                if (detail.getSubtotal() != null)
                    total = total.add(detail.getSubtotal());
            }
        } else { //Contains excluded positions
            DocumentDetail mainDetail = document.getMainDetail();
            total = mainDetail != null ? mainDetail.getSubtotal() : BigDecimal.ZERO;
            BigDecimal mainDetailSubtotal = total;
            for (var detail : document.getDetails()) {
                if (!detail.isMain() && detail.getSubtotal() != null)
                    mainDetailSubtotal = mainDetailSubtotal.subtract(detail.getSubtotal());
            }
            //Set calculated subtotal for the main detail that will be displayed in the item table
            detailsTable.setMainDetailSubtotal(mainDetailSubtotal);
            detailsTable.updateRow(mainDetail);
        }
        totalField.setText(NumericUtils.numberToCurrencyString(total));
    }

    private JPanel initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        //Error message
        errorMessageField = new JLabel();
        bottomPanel.add(errorMessageField, BorderLayout.CENTER);
        errorMessageField.setForeground(Color.RED);
        errorMessageField.setVisible(false);

        //Buttons panel
        JPanel buttonsPanel = new JPanel();
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JButton buttonOk = new JButton("Ok");
        buttonOk.addActionListener(e -> actionCloseDialog(true));
        buttonsPanel.add(buttonOk);
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(e -> actionCloseDialog(false));
        buttonsPanel.add(buttonCancel);

        dialog.getRootPane().registerKeyboardAction(e -> actionCloseDialog(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.getRootPane().registerKeyboardAction(e -> actionCloseDialog(false), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        return bottomPanel;
    }

    private void initDialogKeys(JPanel rootPanel) {
        //Add KeyEvent.VK_ENTER key to the focus traversal keys of the panel
        Set<AWTKeyStroke> forwardKeys = rootPanel.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet<>(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        rootPanel.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);
    }

    private void updateData() {
        if (document == null)
            return;

        updateDetailsTable();
    }

    private void updateDetailsTable() {
        detailsTable.setDocument(document);
        detailsTable.updateTable();
        detailsTable.setSelectedRow(0);
        DocumentDetail detail = detailsTable.getSelectedDetail();
        updateCurrentDetail(detail);
    }

    private void updateCurrentDetail(DocumentDetail detail) {
        if (document != null)
            detailEditForm.setExcludedDocument(document.isExclude());
        detailEditForm.updateForm(detail);
    }

    private void showErrorMessage(String message) {
        errorMessageField.setText(message);
        errorMessageField.setVisible(true);
    }

    private void actionDetailEditFormChange(ActionEvent actionEvent) {
        recalculateTotal();
    }

    private void actionDetailsTableChange(ActionEvent actionEvent) {
        updateCurrentDetail(detailsTable.getSelectedDetail());
        if (Objects.equals(actionEvent.getActionCommand(), TransactionDetailsTable.EVENT_CHANGE))
            recalculateTotal();
    }

    private void actionCloseDialog(boolean saveResult) {
        accountField.requestFocus();
        try {
            if (saveResult) {
                //Validate & prepare (search main) & save document
                if (!prepareDocument())
                    return;
                saveDocument();
                this.saveResult = true;
            }
            dialog.setVisible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean prepareDocument() {
        if (document == null || document.getDetails() == null || document.getDetails().isEmpty())
            return false;

        DocumentDetail mainDetail = document.getMainDetail();
        if (document.isExclude() && mainDetail == null)
            return false;

        //Active
        document.setAct(activeCheckBox.isSelected());

        //Account
        document.setAccount(accountGetValue());
        if (document.getAccount() == null) {
            showErrorMessage("The account field can not be empty");
            return false;
        }

        //Date
        document.setDocDate((Date) dateField.getValue());
        if (document.getDocDate() == null) {
            showErrorMessage("The date field can not be empty");
            return false;
        }

        //Notes
        document.setNotes(notesField.getText());

        //Search main detail for a normal positions list
        if (!document.isExclude()) {
            DocumentDetail currentMainDetail = null;
            for (var documentDetail : document.getDetails()) {
                if (documentDetail.getSubtotal() == null)
                    return false;
                if (currentMainDetail == null || documentDetail.getSubtotal().compareTo(currentMainDetail.getSubtotal()) > 0)
                    currentMainDetail = documentDetail;
            }
            if (currentMainDetail == null)
                return false;
            if (!currentMainDetail.isEqualTo(mainDetail)) {
                if (mainDetail != null) {
                    mainDetail.setMain(false);
                    mainDetail.modified();
                }
                currentMainDetail.setMain(true);
                currentMainDetail.modified();
            }
        }

        //Set calculated subtotal for the main position
        if (document.isExclude()) {
            assert mainDetail != null;
            BigDecimal mainDetailSubtotal = mainDetail.getSubtotal();
            boolean isModified = false;
            for (var detail : document.getDetails()) {
                if (!detail.isMain() && detail.getSubtotal() != null)
                    mainDetailSubtotal = mainDetailSubtotal.subtract(detail.getSubtotal());
                if (detail.isModified())
                    isModified = true;
            }
            if (mainDetailSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
                showErrorMessage("The subitems' total cannot be less than amount of the primary item");
                return false;
            }
            if (isModified) {
                mainDetail.setPrice(mainDetailSubtotal);
                mainDetail.setQuantity(BigDecimal.ONE);
                mainDetail.setSubtotal(mainDetailSubtotal);
                mainDetail.modified();
            }
        }

        //Search empty details
        for (var documentDetail : document.getDetails()) {
            boolean dataPresence = documentDetail.getCategory() != null;
            if (documentDetail.getProduct() != null)
                dataPresence = true;
            if (documentDetail.getSubtotal() != null && documentDetail.getSubtotal().compareTo(BigDecimal.ZERO) != 0)
                dataPresence = true;
            if (documentDetail.getNotes() != null && !documentDetail.getNotes().isEmpty())
                dataPresence = true;
            //Show error message that a position is empty
            if (!dataPresence) {
                showErrorMessage("No field is filled in");
                return false;
            }
        }

        //Search new products
        newProducts.clear();
        for (var documentDetail : document.getDetails()) {
            if (documentDetail.getProduct() != null && documentDetail.getProduct().isTemporary())
                newProducts.add(documentDetail.getProduct());
        }

        return true;
    }

}
