package data.controlles;

import data.DataManager;
import data.entities.CategoryIcon;

import java.util.List;

import static data.entities.CategoryIcon.*;

public class CategoryIconController extends EntityController<CategoryIcon> {

    public CategoryIconController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_NAME + " VARCHAR(64) NOT NULL UNIQUE",
                COLUMN_DATA + " BLOB NOT NULL"
        });
    }

    public void loadIconsList(boolean loadData) throws Exception {
        items.clear();

        try {
            var sql = "SELECT "
                    + COLUMN_NAME
                    + (loadData ? "," + COLUMN_DATA : "")
                    + " FROM " + TABLE_NAME
                    + " ORDER BY " + COLUMN_NAME;
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var row : buffer) {
                CategoryIcon categoryIcon = new CategoryIcon();
                categoryIcon.setName((String) row[0]);
                if (loadData)
                    categoryIcon.setDataArray((byte[]) row[1]);
                items.add(categoryIcon);
            }
        } catch (Exception e) {
            throw new Exception("Can't load categories icons\n" + e.getMessage());
        }
    }

    public CategoryIcon loadIconByName(String name) throws Exception {
        return loadItem(COLUMN_NAME + "='" + name + "'");
    }

    public void addIcon(String name, byte[] dataArray) throws Exception {
        addItem(
                new String[] {COLUMN_NAME, COLUMN_DATA },
                new Object[] { name, dataArray });
    }

    public boolean updateIcon(String name, byte[] dataArray) throws Exception {
        return updateItem(COLUMN_NAME + "='" + name + "'", new String[] { COLUMN_DATA }, new Object[] { dataArray });
    }

    public boolean deleteIcon(String name) throws Exception {
        return deleteItem(COLUMN_NAME + "='" + name + "'");
    }

    public boolean iconExists(String name) throws Exception {
        return itemExists(COLUMN_NAME + "='" + name + "'");
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT "
                + COLUMN_NAME + ","
                + COLUMN_DATA
                + " FROM " + TABLE_NAME
                + (condition != null && !condition.isEmpty() ? " WHERE " + condition : "")
                + (orderBy != null && !orderBy.isEmpty() ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected CategoryIcon bufferToItem(Object[] row) {
        CategoryIcon item;
        item = new CategoryIcon();

        item.setName((String) row[0]);
        item.setDataArray((byte[]) row[1]);

        return item;
    }

}
