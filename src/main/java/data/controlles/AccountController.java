package data.controlles;

import data.DataManager;
import data.entities.Account;

import java.util.List;

import static data.entities.Account.*;

public class AccountController extends EntityController<Account> {

    public AccountController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_NAME + " VARCHAR(128) NOT NULL"
        });
    }

    public Account getAccountById(int id) {
        for (var i = 0; i < size(); i++) {
            Account account = get(i);
            if (account.getId() == id) {
                return account;
            }
        }

        return null;
    }

    public void loadAccountsList() throws Exception {
        loadList(null, COLUMN_NAME);
    }

    public Account loadAccountById(int id) throws Exception {
        return loadItem(COLUMN_ID + "=" + id);
    }

    public int addAccount(String name) throws Exception {
        return addItem(new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean updateAccount(int id, String name) throws Exception {
        return updateItem(COLUMN_ID + "=" + id, new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean deleteAccount(int id) throws Exception {
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

    protected Account bufferToItem(Object[] row) {
        Account item;
        item = new Account();

        item.setId((int) row[0]);
        item.setName((String) row[1]);

        return item;
    }

}
