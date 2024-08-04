package data.controlles;

import data.DataManager;
import data.entities.Account;
import data.entities.User;

import java.util.List;

public class UserController extends EntityController<User> {

    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";

    public UserController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_NAME + " VARCHAR(64) NOT NULL"
        });
    }

    public User getUserById(int id) {
        for (var i = 0; i < size(); i++) {
            User user = get(i);
            if (user.getId() == id) {
                return user;
            }
        }

        return null;
    }

    public void loadUsersList() throws Exception {
        loadList(null, COLUMN_NAME);
    }

    public User loadUserById(int id) throws Exception {
        return loadItem(COLUMN_ID + "=" + id);
    }

    public int addUser(String name) throws Exception {
        return (int) addItem(new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean updateUser(int id, String name) throws Exception {
        return updateItem(COLUMN_ID + "=" + id, new String[] { COLUMN_NAME } , new String[] { name });
    }

    public boolean deleteUser(int id) throws Exception {
        return deleteItem(COLUMN_ID + "=" + id);
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + " FROM " + TABLE_NAME
                + (condition != null && condition.length() > 0 ? " WHERE " + condition : "")
                + (orderBy != null && orderBy.length() > 0 ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected User bufferToItem(Object[] row) {
        User item;
        item = new User();

        item.setId((int) row[0]);
        item.setName((String) row[1]);

        return item;
    }
}
