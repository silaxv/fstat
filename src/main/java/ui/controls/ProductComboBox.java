package ui.controls;

import data.controlles.ProductController;
import data.entities.Category;
import data.entities.Product;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class ProductComboBox extends JComboBox<String> {

    public static final String EVENT_CHANGE = "change";

    private final ProductController products;
    private List<Product> categoryProductsList;

    private Category category;
    private Product selectedProduct;

    private boolean listenerActive;
    private final List<ActionListener> listeners = new ArrayList<>();

    public ProductComboBox(ProductController products) {
        this.products = products;
        this.category = null;
        setEditable(true);
        listenerActive = false;
        addItemListener(this::itemListener);
    }

    public void setList(Category category) {
        this.category = category;
        listenerActive = false;
        removeAllItems();
        if (category != null) {
            categoryProductsList = products.getProductsListByCategory(category);
            for (var item : categoryProductsList)
                addItem(item.getName());
        }
        setSelectedIndex(-1);
        listenerActive = true;
    }

    public int listSize() {
        return categoryProductsList.size();
    }

    public Product getListItem(int index) {
        return categoryProductsList.get(index);
    }

    public void setValue(String productName) {
        listenerActive = false;
        getEditor().setItem(productName != null ? productName : "");
        listenerActive = true;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    private void itemListener(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() != ItemEvent.SELECTED || !listenerActive || category == null)
            return;

        String selectedItemName = (String) itemEvent.getItem();
        //Search a product by a name
        selectedProduct = null;
        if (categoryProductsList != null) {
            for (var item : categoryProductsList) {
                if (item.getName().compareToIgnoreCase(selectedItemName) == 0) {
                    selectedProduct = item;
                    break;
                }
            }
        }
        //If a product hasn't been found then creating a temp product
        if (selectedProduct == null) {
            var tempProduct = new Product();
            tempProduct.setTransGroup(category.getTransGroup());
            tempProduct.setCategory(category);
            tempProduct.setName(selectedItemName);
            tempProduct.setTemporary(true);
            selectedProduct = tempProduct;
        }

        fireActionChange();
    }

    private void fireActionChange() {
        ActionEvent event = new ActionEvent(this, 0, EVENT_CHANGE);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
