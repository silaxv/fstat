package data.entities;

/*
    TABLE accounts
        id INT
        name VARCHAR(128)
*/

public class Account {

    public static final String TABLE_NAME = "accounts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
