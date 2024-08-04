package data.entities;

/*
    TABLE documents_details
        id INT
        f_documents INT
        f_products INT
        f_users INT
        quantity DECIMAL(15,3)
        price MONEY
        subtotal MONEY
        notes VARCHAR(1024)
*/

import java.math.BigDecimal;

public class DocumentDetail {

    private Integer id;
    private Document document;
    private Product product;
    private User user;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String notes;
    private boolean modified;

    public DocumentDetail(Integer id, Document document, Product product, User user, BigDecimal quantity, BigDecimal price, BigDecimal subtotal, String notes) {
        this.id = id;
        this.document = document;
        this.product = product;
        this.user = user;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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

    @Override
    public String toString() {
        return "[" + id + "]" +
                (document != null ? " document=" + document.getId() : "") +
                (product != null && product.getCategory() != null ? ", category=" + product.getCategory().getName() : "") +
                (product != null ? ", product=" + product.getName() : "") +
                (user != null ? ", user=" + user.getName() : "") +
                ", price=" + price;
    }
}
