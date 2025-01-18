package data.entities;

/*
    TABLE products --By the definition used in economics, products include services
        id INT
        category_id INT
        trans_group TINYINT
        name VARCHAR(128)
*/

public class Product {

    public static final String TABLE_NAME = "products";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CATEGORY = "category_id";
    public static final String COLUMN_TRANS_GROUP = "trans_group";
    public static final String COLUMN_NAME = "name";

    private int id;
    private Category category;
    private TransGroup transGroup;
    private String name;
    private boolean temporary;

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

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public String toString() {
        return name;
    }

}
