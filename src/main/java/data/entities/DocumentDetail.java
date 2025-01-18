package data.entities;

/*
    TABLE documents_details
        id INT
        document_id INT
        main BOOLEAN
        category_id INT
        product_id INT
        user_id INT
        quantity DECIMAL(15,3)
        price MONEY
        subtotal MONEY
        notes VARCHAR(1024)
*/

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentDetail {

    public static final String TABLE_DT_NAME = "documents_details";
    public static final String COLUMN_DT_ID = "id";
    public static final String COLUMN_DT_DOCUMENT = "document_id";
    public static final String COLUMN_DT_MAIN = "main";
    public static final String COLUMN_DT_CATEGORY = "category_id";
    public static final String COLUMN_DT_PRODUCT = "product_id";
    public static final String COLUMN_DT_USER = "user_id";
    public static final String COLUMN_DT_QUANTITY = "quantity";
    public static final String COLUMN_DT_PRICE = "price";
    public static final String COLUMN_DT_SUBTOTAL = "subtotal";
    public static final String COLUMN_DT_NOTES = "notes";

    private Integer id;
    private Document document;
    private boolean main;
    private Category category;
    private Product product;
    private User user;
    private List<Tag> tags;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String notes;
    private boolean modified;

    public DocumentDetail(Document document) {
        this.id = null;
        this.document = document;
        this.main = false;
        this.category = null;
        this.product = null;
        this.user = null;
        this.quantity = BigDecimal.ONE;
        this.price = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.tags = new ArrayList<>();
        this.notes = "";
    }

    public DocumentDetail(Integer id, Document document, boolean main, Category category, Product product, User user, BigDecimal quantity, BigDecimal price, BigDecimal subtotal, String notes) {
        this.id = id;
        this.document = document;
        this.main = main;
        this.category = category;
        this.product = product;
        this.user = user;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
        this.tags = new ArrayList<>();
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = BigDecimal.valueOf(subtotal);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void modified() {
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isEqualTo(DocumentDetail detail) {
        if (getId() == null || detail == null || detail.getId() == null)
            return false;

        return (int) getId() == detail.getId();
    }

    @Override
    public String toString() {
        return "[" + id + "]" +
                (document != null ? " document=" + document.getId() : "") +
                (category != null ? ", category=" + category.getName() : "") +
                (product != null ? ", product=" + product.getName() : "") +
                (user != null ? ", user=" + user.getName() : "") +
                ", subtotal=" + subtotal;
    }

}
