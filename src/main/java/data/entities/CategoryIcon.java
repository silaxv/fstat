package data.entities;

/*
    TABLE categories_icons
        name VARCHAR(64)
        data_array
*/

public class CategoryIcon {

    public static final String TABLE_NAME = "categories_icons";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATA = "data_array";

    private String name;
    private byte[] dataArray;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getDataArray() {
        return dataArray;
    }

    public void setDataArray(byte[] dataArray) {
        this.dataArray = dataArray;
    }

}
