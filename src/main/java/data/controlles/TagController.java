package data.controlles;

import data.DataManager;
import data.entities.Tag;

import java.util.List;

import static data.entities.Tag.*;

public class TagController extends EntityController<Tag> {

    public TagController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        //TABLE tags
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_NAME + " VARCHAR(64) NOT NULL"
        });

        //TABLE tags_refs
        dbController.createTable(TABLE_R_NAME, new String[] {
                COLUMN_DOCUMENT_DETAIL + " INTEGER NOT NULL",
                COLUMN_TAG + " INT NOT NULL",
                "UNIQUE(" + COLUMN_DOCUMENT_DETAIL + ", " + COLUMN_TAG + ")"
        });
    }

    public void dropTable() throws Exception {
        super.dropTable();
        if (dbController.tableExists(TABLE_R_NAME))
            dbController.dropTable(TABLE_R_NAME);
    }

    public void loadTagsList() throws Exception {
        loadList(null, COLUMN_NAME);
    }

    public Tag loadTagById(int id) throws Exception {
        return loadItem("id=" + id);
    }

    public int addTag(String name) throws Exception {
        return (int) addItem(new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean updateTag(int id, String name) throws Exception {
        return updateItem(COLUMN_ID + "=" + id, new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean deleteTag(int id) throws Exception {
        return deleteItem(COLUMN_ID + "=" + id);
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + " FROM " + TABLE_NAME
                + (condition != null && !condition.isEmpty() ? " WHERE " + condition : "")
                + (orderBy != null && !orderBy.isEmpty() ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected Tag bufferToItem(Object[] row) {
        Tag item;
        item = new Tag();

        item.setId((int) row[0]);
        item.setName((String) row[1]);

        return item;
    }

}
