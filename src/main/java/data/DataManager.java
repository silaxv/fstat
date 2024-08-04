package data;

import data.controlles.*;

public class DataManager {

    static private final DataManager instance = new DataManager();

    private final DatabaseController databaseController;

    private final AccountController accounts;
    private final CategoryController categories;
    private final CategoryIconController categoriesIcons;
    private final TagController tags;
    private final UserController users;
    private final ProductController products;
    private final DocumentController documents;

    public DataManager() {
        databaseController = new DatabaseController();

        accounts = new AccountController(this);

        categories = new CategoryController(this);

        categoriesIcons = new CategoryIconController(this);

        tags = new TagController(this);

        users = new UserController(this);

        products = new ProductController(this);
        products.setCategories(categories);

        documents = new DocumentController(this);
        documents.setAccounts(accounts);
        documents.setCategories(categories);
        documents.setProducts(products);
        documents.setUsers(users);
    }

    public static DataManager getInstance() {
        return DataManager.instance;
    }

    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    public void init(boolean resetDatabase) throws Exception {
        databaseController.connect(Constants.DB_FILE_NAME);
        if (databaseController.isNewDatabase()) {
            createDatabaseTables();
        } else if (resetDatabase) {
            databaseController.reconnect();
            dropDatabaseTables();
            createDatabaseTables();
        }
    }

    public void init() throws Exception {
        init(false);
    }

    private void createDatabaseTables() throws Exception {
        accounts.createTable();
        categories.createTable();
        categoriesIcons.createTable();
        tags.createTable();
        users.createTable();
        products.createTable();
        documents.createTable();
    }

    private void dropDatabaseTables() throws Exception {
        accounts.dropTable();
        categories.dropTable();
        categoriesIcons.dropTable();
        tags.dropTable();
        users.dropTable();
        products.dropTable();
        documents.dropTable();
    }

    public AccountController getAccounts() {
        return accounts;
    }

    public CategoryController getCategories() {
        return categories;
    }

    public CategoryIconController getCategoriesIcons() {
        return categoriesIcons;
    }

    public TagController getTags() {
        return tags;
    }

    public UserController getUsers() {
        return users;
    }

    public ProductController getProducts() {
        return products;
    }

    public DocumentController getDocuments() {
        return documents;
    }
}
