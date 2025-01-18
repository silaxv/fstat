package data.entities;

/*
    TABLE tags
        id INT
        name VARCHAR(64)
*/

import java.util.List;

public class Tag {

    public static final String TABLE_NAME = "tags";
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

    @Override
    public String toString() {
        return name;
    }

    public static String tagsListToString(List<Tag> tagsList) {
        StringBuilder str = new StringBuilder();
        for (var tag : tagsList) {
            if (str.length() > 0)
                str.append(", ");
            str.append(tag);
        }

        return str.toString();
    }


}
