package data.entities;

/*
    TABLE categories
        id INT
        f_categories INT
        trans_group TINYINT
        name VARCHAR(128)
        f_categories_icons VARCHAR(64)
 */

public class Category {

    private int id;
    private int parentId;
    private Category parent;
    private TransGroup transGroup;
    private String name;
    private String categoryIcon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
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

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }
}
