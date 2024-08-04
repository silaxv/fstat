package data.entities;

/*
    TABLE products --By the definition used in economics, products include services
        id INT
        f_categories INT
        trans_group TINYINT
        name VARCHAR(128)
*/

public class Product {

    private int id;
    private Category category;
    private TransGroup transGroup;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TransGroup getTransGroup() {
        return transGroup;
    }

    public void setTransGroup(TransGroup transGroup) {
        this.transGroup = transGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
