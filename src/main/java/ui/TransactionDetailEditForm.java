package ui;

import data.controlles.CategoryController;
import data.controlles.ProductController;
import data.controlles.TagController;
import data.controlles.UserController;
import data.entities.DocumentDetail;
import ui.controls.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TransactionDetailEditForm extends JPanel {

    private DocumentDetail documentDetail;
    private boolean excludedDocument;

    private final CategoryController categories;
    private final ProductController products;
    private final UserController users;
    private final TagController tags;

    private final List<ActionListener> listeners = new ArrayList<>();

    public static final String EVENT_TOTAL_CHANGE = "total_change";

    private final JDialog dialog;

    private TransactionDetailsTable detailsTable;
    private JButton buttonCategory;
    private ProductComboBox productField;
    private DecimalTextField priceField;
    private DecimalTextField quantityField;
    private DecimalTextField subtotalField;
    private UserComboBox userField;
    private TagsField tagsField;
    private NotesTextField notesField;

    public TransactionDetailEditForm(JDialog dialog, CategoryController categories, ProductController products, UserController users, TagController tags) {
        this.dialog = dialog;
        this.categories = categories;
        this.products = products;
        this.users = users;
        this.tags = tags;

        initPositionForm();
    }

    public void setDetailsTable(TransactionDetailsTable detailsTable) {
        this.detailsTable = detailsTable;
    }

    public void setExcludedDocument(boolean excludedDocument) {
        this.excludedDocument = excludedDocument;
    }

    public void updateForm(DocumentDetail documentDetail) {
        this.documentDetail = documentDetail;
        updateForm();
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    private void initPositionForm() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        //Category Product row
        JPanel productRow = new JPanel();
        add(productRow);

        //Category
        buttonCategory = new JButton("<>");
        productRow.add(buttonCategory);
        buttonCategory.addActionListener(e -> actionSelectCategory());
        updateCategoryButton();

        //Product
        productField = new ProductComboBox(products);
        productField.addActionListener(this::actionSelectProduct);
        productRow.add(productField);

        //Price row
        JPanel priceRow = new JPanel();
        add(priceRow);

        //Price
        priceRow.add(new JLabel("Price:"));
        priceField = new DecimalTextField(11, 2, 12);
        priceField.addActionListener(this::actionChangePrice);
        priceRow.add(priceField);

        //Quantity
        priceRow.add(new JLabel("Quantity:"));
        quantityField = new DecimalTextField(9, 3, 12);
        quantityField.addActionListener(this::actionChangeQuantity);
        priceRow.add(quantityField);

        //Subtotal
        priceRow.add(new JLabel("Subtotal:"));
        subtotalField = new DecimalTextField(11, 2, 12);
        subtotalField.addActionListener(this::actionChangeSubtotal);
        priceRow.add(subtotalField);

        //Person Tags row
        JPanel userRow = new JPanel();
        add(userRow);

        //Person
        JLabel userLabel = new JLabel("Person:");
        userRow.add(userLabel);
        userField = new UserComboBox(users);
        userRow.add(userField);
        userField.addActionListener(e -> actionChangeUser());
        userField.setList();

        //Tags
        JLabel tagsLabel = new JLabel("Tags:");
        userRow.add(tagsLabel);
        tagsField = new TagsField(dialog, tags);
        tagsField.addActionListener(this::actionChangeTags);
        userRow.add(tagsField);

        //Notes panel
        JPanel notesRow = new JPanel();
        add(notesRow);
        JLabel notesLabel = new JLabel("Notes:");
        notesRow.add(notesLabel);
        notesField = new NotesTextField(30);
        notesRow.add(notesField);
        notesField.addActionListener(this::actionChangeNotes);

        SwingUtilities.invokeLater(() -> buttonCategory.requestFocus());
    }

    private void updateForm() {
        boolean enabled = (documentDetail != null);

        buttonCategory.setEnabled(enabled);
        productField.setEnabled(enabled);
        priceField.setEnabled(enabled && (!excludedDocument || !documentDetail.isMain()));
        quantityField.setEnabled(enabled && (!excludedDocument || !documentDetail.isMain()));
        subtotalField.setEnabled(enabled);
        userField.setEnabled(enabled);
        tagsField.setEnabled(enabled);
        notesField.setEnabled(enabled);

        if (!enabled) {
            updateCategoryButton();
            productField.setValue("");
            priceField.setValue(null);
            quantityField.setValue(null);
            subtotalField.setValue(null);
            userField.setValue(null);
            tagsField.clear();
            notesField.setText("");
            return;
        }

        //Category
        updateCategoryButton();

        //Product
        productField.setList(documentDetail.getCategory());
        if (documentDetail.getProduct() != null)
            productField.setValue(documentDetail.getProduct().getName());

        //Price
        priceField.setValue(documentDetail.getPrice());

        //Quantity
        quantityField.setValue(documentDetail.getQuantity());

        //Subtotal
        subtotalField.setValue(documentDetail.getSubtotal());

        //Person
        userField.setValue(documentDetail.getUser());

        //Tags
        var detailTags = documentDetail.getTags();
        if (detailTags != null) {
            tagsField.setValues(detailTags);
        }

        //Notes
        notesField.setText(documentDetail.getNotes());
    }

    private void actionSelectCategory() {
        if (documentDetail == null)
            return;

        CategorySelectForm categorySelectForm;
        try {
            categorySelectForm = new CategorySelectForm(dialog, categories);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        categorySelectForm.setSelectedCategory(documentDetail.getCategory());
        categorySelectForm.showDialog();

        var selectedCategory = categorySelectForm.getSelectedCategory();
        if (selectedCategory != null) {
            documentDetail.setCategory(selectedCategory);
            documentDetail.setProduct(null);
            documentDetail.modified();
            updateCategoryButton();
            updateProductFieldList();
            updateDetailsRow();
            if (productField.listSize() == 1) {
                documentDetail.setProduct(productField.getListItem(0));
                if (documentDetail.getProduct() != null)
                    productField.setValue(documentDetail.getProduct().getName());
                if (priceField.isEnabled())
                    priceField.requestFocus();
                else
                    subtotalField.requestFocus();
            } else {
                productField.requestFocus();
            }
        }

    }

    private void actionSelectProduct(ActionEvent actionEvent) {
        if (documentDetail == null || productField == null)
            return;

        documentDetail.setProduct(productField.getSelectedProduct());
        documentDetail.modified();
        updateDetailsRow();
    }

    private void actionChangePrice(ActionEvent actionEvent) {
        if (documentDetail == null)
            return;

        documentDetail.setPrice(priceField.getValue());
        documentDetail.modified();
        System.out.println("Price: " + priceField.getValue());
        recalculatePriceSubtotal(true);
        updateDetailsRow();
    }

    private void actionChangeQuantity(ActionEvent actionEvent) {
        if (documentDetail == null)
            return;

        documentDetail.setQuantity(quantityField.getValue());
        documentDetail.modified();
        recalculatePriceSubtotal(true);
        updateDetailsRow();
    }

    private void actionChangeSubtotal(ActionEvent actionEvent) {
        if (documentDetail == null)
            return;

        documentDetail.setSubtotal(subtotalField.getValue());
        documentDetail.modified();
        recalculatePriceSubtotal(false);
        updateDetailsRow();
    }

    private void actionChangeUser() {
        if (documentDetail == null || userField == null)
            return;

        documentDetail.setUser(userField.getSelectedUser());
        documentDetail.modified();
        updateDetailsRow();
    }

    private void actionChangeTags(ActionEvent actionEvent) {
        if (documentDetail == null || tagsField == null)
            return;

        documentDetail.setTags(tagsField.getValues());
        documentDetail.modified();
        updateDetailsRow();
    }

    private void actionChangeNotes(ActionEvent actionEvent) {
        documentDetail.setNotes(notesField.getText());
        documentDetail.modified();
        updateDetailsRow();
    }

    private void updateDetailsRow() {
        if (documentDetail != null && detailsTable != null)
            detailsTable.updateRow(documentDetail);
    }

    private void updateCategoryButton() {
        buttonCategory.setText(documentDetail != null && documentDetail.getCategory() != null ? documentDetail.getCategory().getName() : "<Category>");
    }

    private void updateProductFieldList() {
        if (documentDetail == null)
            return;

        productField.setList(documentDetail.getCategory());
    }

    private void recalculatePriceSubtotal(boolean recalculateSubtotal) {
        //Recalculate values
        if (recalculateSubtotal) {
            documentDetail.setSubtotal(documentDetail.getPrice().multiply(documentDetail.getQuantity()).setScale(2, RoundingMode.HALF_UP));
            subtotalField.setValue(documentDetail.getSubtotal());
        } else {
            documentDetail.setPrice(!documentDetail.getQuantity().equals(BigDecimal.ZERO) ? documentDetail.getSubtotal().divide(documentDetail.getQuantity(), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            priceField.setValue(documentDetail.getPrice());
        }

        fireActionChange();
    }

    private void fireActionChange() {
        ActionEvent event = new ActionEvent(this, 0, EVENT_TOTAL_CHANGE);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
