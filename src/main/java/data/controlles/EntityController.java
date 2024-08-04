package data.controlles;

import data.DataManager;
import data.DatabaseController;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityController<T> {

    protected final List<T> items;

    protected final DatabaseController dbController;

    private final String tableName;

    public EntityController(DataManager dataManager, String mainTableName) {
        this.items = new ArrayList<>();
        this.dbController = dataManager.getDatabaseController();
        this.tableName = mainTableName;
    }

    public int size() {
        return items.size();
    }

    public T get(int index) {
        return items.get(index);
    }

    public abstract void createTable() throws Exception;

    public void dropTable() throws Exception {
        items.clear();

        if (dbController.tableExists(tableName))
            dbController.dropTable(tableName);
    }

    protected void loadList(String condition, String orderBy) throws Exception {
        items.clear();

        try {
            List<Object[]> buffer = selectItems(condition, orderBy);
            for (var row : buffer)
                items.add(bufferToItem(row));
        } catch (Exception e) {
            throw new Exception("Can't load items" + (condition != null ? " (" + condition + ")" : "") + "\n" + e.getMessage());
        }
    }

    protected T loadItem(String condition) throws Exception {
        T item;

        checkCondition(condition);

        try {
            List<Object[]> buffer = selectItems(condition, null);
            if (buffer.size() == 0)
                return null;
            item = bufferToItem(buffer.get(0));
        } catch (Exception e) {
            throw new Exception("Can't load an item (" + condition + ")\n" + e.getMessage());
        }

        return item;
    }

    protected int addItem(String[] columnsNames, Object[] columnsValues) throws Exception {
        int itemId;

        try {
            itemId = dbController.insertData(tableName, columnsNames, columnsValues);
        } catch (Exception e) {
            throw new Exception("Can't create a new item\n" + e.getMessage());
        }

        return itemId;
    }

    protected boolean updateItem(String condition, String[] columnsNames, Object[] columnsValues) throws Exception {
        int res;

        checkCondition(condition);

        try {
            res = dbController.updateData(tableName, condition, columnsNames, columnsValues);
        } catch (Exception e) {
            throw new Exception("Can't update an item (" + condition + ")\n" + e.getMessage());
        }

        return res > 0;
    }

    protected boolean deleteItem(String condition) throws Exception {
        int res;

        checkCondition(condition);

        try {
            res = dbController.deleteData(tableName, condition);
        } catch (Exception e) {
            throw new Exception("Can't delete an item (" + condition + ")\n" + e.getMessage());
        }

        return res > 0;
    }

    protected boolean itemExists(String condition) throws Exception {
        List<Object[]> buffer;

        checkCondition(condition);

        try {
            var sql = "SELECT 1 FROM " + tableName + " WHERE " + condition;
            buffer = dbController.execQuery(sql);
        } catch (Exception e) {
            throw new Exception("Can't check an item (" + condition + ")\n" + e.getMessage());
        }

        return buffer.size() > 0;
    }

    protected List<Object[]> selectItems(String condition, String orderBy, String[] columns) throws Exception {
        List<Object[]> buffer;

        StringBuilder sqlColumns = new StringBuilder();
        for (var column : columns)
            sqlColumns.append(sqlColumns.length() > 0 ? ", " : "").append(column);
        var sql = "SELECT " + sqlColumns
                + " FROM " + tableName
                + (condition != null && condition.length() > 0 ? " WHERE " + condition : "")
                + (orderBy != null && orderBy.length() > 0 ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected abstract T bufferToItem(Object[] row) throws Exception;

    protected abstract List<Object[]> selectItems(String condition, String orderBy) throws Exception;

    private static void checkCondition(String condition) throws Exception {
        if (condition == null || condition.length() == 0)
            throw new Exception("The condition shouldn't be empty!");
    }
}
