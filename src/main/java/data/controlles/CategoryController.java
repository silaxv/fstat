package data.controlles;

import data.DataManager;
import data.entities.Category;
import data.entities.TransGroup;

import java.util.List;

import static data.entities.Category.*;

public class CategoryController extends EntityController<Category> {

    public CategoryController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_PARENT + " INTEGER",
                COLUMN_TRANS_GROUP + " TINYINT NOT NULL",
                COLUMN_NAME + " VARCHAR(128) NOT NULL",
                COLUMN_ICON + " VARCHAR(64)"
        });
    }

    public Category getCategoryById(int id) {
        for (var i = 0; i < size(); i++) {
            Category category = get(i);
            if (category.getId() == id) {
                return category;
            }
        }

        return null;
    }

    public Category getCategoryByName(String name) {
        for (var i = 0; i < size(); i++) {
            Category category = get(i);
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }

        return null;
    }

    public void loadCategoriesListByTransGroup(TransGroup transGroup) throws Exception {
        checkTransGroup(transGroup);
        loadList(COLUMN_TRANS_GROUP + "=" + transGroup.getId(), COLUMN_NAME);
    }

    public void loadCategoriesList() throws Exception {
        loadList(null, COLUMN_TRANS_GROUP + ", " + COLUMN_NAME);
    }

    public Category loadCategoryById(int id) throws Exception {
        return loadItem(COLUMN_ID + "=" + id);
    }

    public Category addCategory(Category parent, TransGroup transGroup, String name, String icon) throws Exception {
        Category item = new Category();

        item.setParent(parent);
        item.setTransGroup(transGroup);
        item.setName(name);
        item.setCategoryIcon(icon);

        int itemId = addCategory(parent != null ? parent.getId() : -1, transGroup, name, icon);
        item.setId(itemId);

        return item;
    }

    public int addCategory(int parentId, TransGroup transGroup, String name, String icon) throws Exception {
        checkTransGroup(transGroup);
        checkParentCategory(parentId);

        return addItem(
                new String[] { COLUMN_PARENT, COLUMN_TRANS_GROUP, COLUMN_NAME, COLUMN_ICON },
                new Object[] { parentId != -1 ? parentId : null, transGroup.getId(), name, icon });
    }

    public boolean updateCategory(int id, TransGroup transGroup, String name, String icon) throws Exception {
        checkTransGroup(transGroup);

        return updateItem(COLUMN_ID + "=" + id,
                new String[] { COLUMN_TRANS_GROUP, COLUMN_NAME, COLUMN_ICON },
                new Object[] { transGroup.getId(), name, icon });
    }

    public boolean changeCategoryParent(int id, int parentId) throws Exception {
        int res;
        checkParentCategory(parentId);

        try {
            res = dbController.updateData(TABLE_NAME, COLUMN_ID + "=" + id,
                    new String[] { COLUMN_PARENT },
                    new Object[] { parentId != -1 ? parentId : null });
        } catch (Exception e) {
            throw new Exception("Can't change a parent of a category with id=" + id + "\n" + e.getMessage());
        }

        return res > 0;
    }

    public boolean categoryExistsById(int id) throws Exception {
        return id == -1 || itemExists(COLUMN_ID + "=" + id);
    }

    public boolean deleteCategory(int id) throws Exception {
        return deleteItem(COLUMN_ID + "=" + id);
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        return selectItems(condition, orderBy,
                new String[] { COLUMN_ID, COLUMN_PARENT, COLUMN_TRANS_GROUP, COLUMN_NAME, COLUMN_ICON });
    }

    protected Category bufferToItem(Object[] row) {
        Category item;
        item = new Category();

        item.setId((int) row[0]);
        item.setParentId((int) (row[1] != null ? row[1] : -1));
        item.setTransGroup(TransGroup.getValueById((int) row[2]));
        item.setName((String) row[3]);
        item.setCategoryIcon((String) row[4]);

        return item;
    }

    private void checkParentCategory(int parentId) throws Exception {
        if (!categoryExistsById(parentId))
            throw new Exception("A parent category with id = " + parentId + " doesn't exist!");
    }

    private void checkTransGroup(TransGroup transGroup) throws Exception {
        if (transGroup == null)
            throw new Exception("Trans group can't be empty!");
    }

}
