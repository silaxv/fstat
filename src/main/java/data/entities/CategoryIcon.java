package data.entities;

/*
    TABLE categories_icons
        name VARCHAR(64)
        data_array
*/

public class CategoryIcon {

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
