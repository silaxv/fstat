package data.controlles;

import data.DataManager;
import data.entities.Category;
import data.entities.Product;
import data.entities.TransGroup;

import java.util.List;

public class ProductController extends EntityController<Product> {

    private static final String TABLE_NAME = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "f_categories";
    private static final String COLUMN_TRANS_GROUP = "trans_group";
    private static final String COLUMN_NAME = "name";

    private CategoryController categories;

    public ProductController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_CATEGORY + " INTEGER",
                COLUMN_TRANS_GROUP + " TINYINT NOT NULL",
                COLUMN_NAME + " VARCHAR(128) NOT NULL"
        });
    }

    public Product getProductById(int id) {
        for (var i = 0; i < size(); i++) {
            Product product = get(i);
            if (product.getId() == id) {
                return product;
            }
        }

        return null;
    }

    public void setCategories(CategoryController categories) {
        this.categories = categories;
    }

    public void loadProductsList(TransGroup transGroup, Category category) throws Exception {
        checkTransGroup(transGroup);
        checkCategory(category);

        loadList(COLUMN_TRANS_GROUP + "=" + transGroup.getId() + (category != null ? " AND " + COLUMN_CATEGORY + "=" + category.getId() : ""), COLUMN_NAME);
    }

    public void loadProductsList() throws Exception {
        loadList(null, COLUMN_TRANS_GROUP + ", " + COLUMN_NAME);
    }

    public Product loadProductById(int id) throws Exception {
        return loadItem(COLUMN_ID + "=" + id);
    }

    public int addProduct(Category category, TransGroup transGroup, String name) throws Exception {
        checkTransGroup(transGroup);
        checkCategory(category);

        return (int) addItem(
                new String[] { COLUMN_CATEGORY, COLUMN_TRANS_GROUP, COLUMN_NAME },
                new Object[] { category != null ? category.getId() : null, transGroup.getId(), name });
    }

    public boolean updateProduct(int id, Category category, TransGroup transGroup, String name) throws Exception {
        checkTransGroup(transGroup);
        checkCategory(category);

        return updateItem(COLUMN_ID + "=" + id,
                new String[] { COLUMN_CATEGORY, COLUMN_TRANS_GROUP, COLUMN_NAME },
                new Object[] { category != null ? category.getId() : null, transGroup.getId(), name });
    }

    public boolean deleteProduct(int id) throws Exception {
        return deleteItem(COLUMN_ID + "=" + id);
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        return selectItems(condition, orderBy,
                new String[] { COLUMN_ID, COLUMN_CATEGORY, COLUMN_TRANS_GROUP, COLUMN_NAME });
    }

    protected Product bufferToItem(Object[] row) throws Exception {
        Product item;
        item = new Product();

        item.setId((int) row[0]);
        if (row[1] != null) {
            int categoryId = (int) row[1];
            Category category = categories.getCategoryById(categoryId);
            if (category == null)
                throw new Exception("Category with id = " + categoryId + " is not found!");
            item.setCategory(category);
        }
        item.setTransGroup(TransGroup.getValueById((int) row[2]));
        item.setName((String) row[3]);

        return item;
    }

    private void checkCategory(Category category) throws Exception {
        if (category != null && !categories.categoryExistsById(category.getId()))
            throw new Exception("A category with id = " + category.getId() + " doesn't exist!");
    }

    private void checkTransGroup(TransGroup transGroup) throws Exception {
        if (transGroup == null)
            throw new Exception("Trans group can't be empty!");
    }

}
