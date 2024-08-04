package data.entities;

/*
    TABLE tags_refs
        document_detail_id INT
        tag_id INT
*/

public class Tag {

    public static final String TABLE_NAME = "tags";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";

    public static final String TABLE_R_NAME = "tags_refs";
    public static final String COLUMN_DOCUMENT_DETAIL = "document_detail_id";
    public static final String COLUMN_TAG = "tag_id";

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
