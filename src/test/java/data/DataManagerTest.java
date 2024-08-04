package data;

import data.controlles.*;
import data.entities.*;
import org.junit.jupiter.api.Test;
import utils.DateUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class DataManagerTest {

    private static final String TEST_RESOURCES_FOLDER = "src/test/resources/";

    DataManager dataManager = DataManager.getInstance();
    AccountController accounts = dataManager.getAccounts();
    CategoryController categories  = dataManager.getCategories();
    CategoryIconController categoriesIcons  = dataManager.getCategoriesIcons();
    TagController tags = dataManager.getTags();
    UserController users = dataManager.getUsers();
    ProductController products = dataManager.getProducts();
    DocumentController documents = dataManager.getDocuments();

    @Test
    void testWorkWithAccounts() {
        try {
            dataManager.init();

            //Add
            accounts.addAccount("Test Y");
            int lastId = accounts.addAccount("Test X");
            System.out.println("Last Id = " + lastId);

            //Update & delete
            Account accountU = accounts.loadAccountById(2);
            boolean recordUpdated = accounts.updateAccount(2, accountU.getName() + " *");
            boolean recordDeleted = accounts.deleteAccount(1);
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            Account account1 = accounts.loadAccountById(1);
            if (account1 != null)
                System.out.println("account1 = " + account1.getName());
            else
                System.out.println("account1 not found!");
            Account account2 = accounts.loadAccountById(2);
            if (account2 != null)
                System.out.println("account2 = " + account2.getName());
            else
                System.out.println("account2 not found!");

            //Load list of items
            accounts.loadAccountsList();
            System.out.println("Load list (" + accounts.size() + "):");
            for (var i = 0; i < accounts.size(); i++)
                System.out.println("*[" + accounts.get(i).getId() + "] " + accounts.get(i).getName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithCategories() throws Exception {
        try {
            dataManager.init();

            //Add
            Category category1 = categories.addCategory(null, TransGroup.EXPENSES, "Car", null);
            System.out.println("Last Id = " + category1.getId());
            categories.addCategory(category1, TransGroup.EXPENSES, "Gasoline", null);
            Category categoryRepair = categories.addCategory(category1, TransGroup.EXPENSES, "Repair", null);
            int categoryTaxesId = categories.addCategory(category1.getId(), TransGroup.EXPENSES, "Taxes", null);

            //Update & delete
            boolean recordUpdated = categories.updateCategory(categoryRepair.getId(), TransGroup.INCOME, categoryRepair.getName() + " *", "test");
            boolean recordDeleted = categories.deleteCategory(categoryTaxesId);
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            Category categoryLoaded = categories.loadCategoryById(3);
            if (categoryLoaded != null)
                System.out.println("categoryLoaded = " + categoryLoaded.getName());
            else
                System.out.println("categoryLoaded not found!");

            if (categories.changeCategoryParent(3, 1))
                System.out.println("Parent changed");

            //Load list of items
            categories.loadCategoriesList();
            categories.loadCategoriesListByTransGroup(TransGroup.EXPENSES);
            System.out.println("Load list (" + categories.size() + "):");
            for (var i = 0; i < categories.size(); i++)
                System.out.println("*[" + categories.get(i).getId() + "] " + categories.get(i).getName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithCategoriesIcons() {
        try {
            dataManager.init();

            //Add
            byte[] dataArray = new byte[10];
            dataArray[0] = 1;
            dataArray[1] = 2;
            dataArray[2] = 3;
            dataArray[3] = 6;
            dataArray[9] = 111;
            File fileArk = new File(TEST_RESOURCES_FOLDER + "ark2.png");
            byte[] fileArkContent = Files.readAllBytes(fileArk.toPath());
            File fileAlert = new File(TEST_RESOURCES_FOLDER + "alert.png");
            byte[] fileAlertContent = Files.readAllBytes(fileAlert.toPath());
            if (!categoriesIcons.iconExists("test1")) {
                categoriesIcons.addIcon("test1", dataArray);
                categoriesIcons.addIcon("test2", fileArkContent);
                categoriesIcons.addIcon("test3", dataArray);
                categoriesIcons.addIcon("test4", dataArray);
            } else {
                System.out.println("Items are already exist!");
            }

            //Update & delete
            boolean recordUpdated = categoriesIcons.updateIcon("test3", fileAlertContent);
            boolean recordDeleted = categoriesIcons.deleteIcon("test4");
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            CategoryIcon categoryIcon1 = categoriesIcons.loadIconByName("test1");
            byte[] dataArray1 = categoryIcon1.getDataArray();
            System.out.println("Loaded item <" + categoryIcon1.getName() + "> (size: " + dataArray1.length + ")");

            //Load list of items
            categoriesIcons.loadIconsList(true);
            System.out.println("Load list (" + categoriesIcons.size() + "):");
            for (var i = 0; i < categoriesIcons.size(); i++)
                System.out.println("* " + categoriesIcons.get(i).getName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithTags() {
        try {
            dataManager.init();

            //Add
            tags.addTag("Tourism");
            tags.addTag("Constructions");
            tags.addTag("Fun");
            int lastId = tags.addTag("School");
            System.out.println("Last id = " + lastId);

            //Update & delete
            boolean recordUpdated = tags.updateTag(1, "Tourism1");
            boolean recordDeleted = tags.deleteTag(3);
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            Tag tag1 = tags.loadTagById(8);
            if (tag1 != null)
                System.out.println("tag1 = " + tag1.getName());
            else
                System.out.println("tag1 not found!");

            //Load list of items
            tags.loadTagsList();
            System.out.println("Load list (" + tags.size() + "):");
            for (var i = 0; i < tags.size(); i++)
                System.out.println("*[" + tags.get(i).getId() + "] " + tags.get(i).getName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithUsers() {
        try {
            dataManager.init();

            //Add
            users.addUser("Aleksei");
            users.addUser("Tatiana");
            users.addUser("Guest");
            int lastId = users.addUser("Test user");
            System.out.println("Last id = " + lastId);

            //Update & delete
            boolean recordUpdated = users.updateUser(1, "Aleksei V Shevchenko");
            boolean recordDeleted = users.deleteUser(lastId);
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            User user1 = users.loadUserById(1);
            if (user1 != null)
                System.out.println("tag1 = " + user1.getName());
            else
                System.out.println("tag1 not found!");

            //Load list of items
            users.loadUsersList();
            System.out.println("Load list (" + users.size() + "):");
            for (var i = 0; i < users.size(); i++)
                System.out.println("*[" + users.get(i).getId() + "] " + users.get(i).getName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithProducts() {
        try {
            dataManager.init();

            TransGroup transGroup = TransGroup.EXPENSES;
            categories.loadCategoriesListByTransGroup(transGroup);
            Category category1 = categories.getCategoryById(1);
            Category category2 = categories.getCategoryById(2);
            if (category1 == null)
                throw new Exception("A category with id = 1 not found!");

            //Add
            products.addProduct(category1, transGroup, "Bread");
            int lastId = products.addProduct(category1, transGroup, "Milk");
            System.out.println("Last id = " + lastId);

            //Update & delete
            boolean recordUpdated = products.updateProduct(1, category2, transGroup, "Bread black");
            boolean recordDeleted = products.deleteProduct(2);
            System.out.println("Record updated = " + recordUpdated + "; Record deleted = " + recordDeleted);

            //Load one item
            Product product1 = products.loadProductById(1);
            System.out.println("Item: " + product1.getName() + " (" + product1.getCategory().getName() + ")");

            //Load list of items
            products.loadProductsList(transGroup, null);
            System.out.println("Load full list (" + products.size() + "):");
            for (var i = 0; i < products.size(); i++)
                System.out.println("*[" + products.get(i).getId() + "] " + products.get(i).getName());
            products.loadProductsList(transGroup, category2);
            System.out.println("Load list for category " + category2.getName() + " (" + products.size() + "):");
            for (var i = 0; i < products.size(); i++)
                System.out.println("*[" + products.get(i).getId() + "] " + products.get(i).getName());


        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testWorkWithDocuments() {
        try {
            dataManager.init(false);

            //Prepare environment
            TransGroup transGroup = TransGroup.EXPENSES;

            accounts.loadAccountsList();
            if (accounts.size() < 1) {
                accounts.addAccount("Main");
                accounts.loadAccountsList();
            }
            Account account = accounts.get(0);

            categories.loadCategoriesListByTransGroup(transGroup);
            if (categories.size() < 4) {
                categories.addCategory(-1, transGroup, "House", null);
                int categoryCarId = categories.addCategory(-1, transGroup, "Car", null);
                categories.addCategory(categoryCarId, transGroup, "Gasoline", null);
                categories.addCategory(categoryCarId, transGroup, "Repair", null);
                categories.loadCategoriesListByTransGroup(transGroup);
            }
            Category categoryGasoline = categories.getCategoryByName("Gasoline");
            Category categoryRepair = categories.getCategoryByName("Repair");

            products.loadProductsList(transGroup, null);
            if (products.size() < 4) {
                products.addProduct(categoryGasoline, categoryGasoline.getTransGroup(), "AI-92");
                products.addProduct(categoryRepair, categoryRepair.getTransGroup(), "Tires service");
                products.addProduct(categoryRepair, categoryRepair.getTransGroup(), "Car-alarm setup");
                products.addProduct(categoryRepair, categoryRepair.getTransGroup(), "Oil change");
                products.addProduct(categoryRepair, categoryRepair.getTransGroup(), "Generator repair");
                products.addProduct(categoryGasoline, categoryGasoline.getTransGroup(), "AI-95");
                products.loadProductsList(transGroup, null);
            }
            Product product1 = products.getProductById(1);
            Product product2 = products.getProductById(2);
            Product product3 = products.getProductById(3);
            Product product4 = products.getProductById(4);
            Product product5 = products.getProductById(5);

            users.loadUsersList();
            if (users.size() == 0) {
                users.addUser("Aleksei");
                users.loadUsersList();
            }
            User user1 = users.get(0);

            //Add document
            DateUtils.numbersToDate(2024, 5, 1);
            documents.addDocument(DateUtils.numbersToDate(2024, 5, 1), transGroup, true, account, "1", null);
            List<DocumentDetail> documentDetails2 = new ArrayList<>();
            documentDetails2.add(new DocumentDetail(null, null, true, product3, user1, BigDecimal.valueOf(10), BigDecimal.valueOf(55.46), BigDecimal.valueOf(554.6), "2.1"));
            documentDetails2.add(new DocumentDetail(null, null, false, product2, user1, BigDecimal.valueOf(5), BigDecimal.valueOf(800), BigDecimal.valueOf(4000), ""));
            documentDetails2.add(new DocumentDetail(null, null, false, product2, null, BigDecimal.valueOf(1), BigDecimal.valueOf(67.7), BigDecimal.valueOf(67.7), ""));
            documents.addDocument(DateUtils.numbersToDate(2024, 5, 1), transGroup, true, account, "2", documentDetails2);
            documents.addDocument(DateUtils.numbersToDate(2024, 5, 2), transGroup, true, account, "3", null);
            documents.addDocument(DateUtils.numbersToDate(2024, 6, 15), transGroup, true, account, "4", null);
            List<DocumentDetail> documentDetails5 = new ArrayList<>();
            documentDetails5.add(new DocumentDetail(null, null, true, product3, user1, BigDecimal.valueOf(2), BigDecimal.valueOf(1.5), BigDecimal.valueOf(3), "Test 5.1"));
            documentDetails5.add(new DocumentDetail(null, null, false, product2, null, BigDecimal.valueOf(1), BigDecimal.valueOf(400), BigDecimal.valueOf(400), "Test 5.2"));
            documents.addDocument(DateUtils.numbersToDate(2024, 6, 15), transGroup, true, account, "5", documentDetails5);

            //Update
            Document document5 = documents.loadDocumentById(5);
            List<DocumentDetail> documentDetailsForUpdate = document5.getDetails();
            //documentDetailsForUpdate.remove(0);
            for (var detail : documentDetailsForUpdate) {
                detail.setNotes(detail.getNotes() + " *U");
                detail.modified();
            }
            documentDetailsForUpdate.add(new DocumentDetail(null, null, false, product5, null, BigDecimal.valueOf(1), BigDecimal.valueOf(65), BigDecimal.valueOf(65), "Test 1 add 5.+"));
            documentDetailsForUpdate.add(new DocumentDetail(null, null, false, product4, null, BigDecimal.valueOf(2), BigDecimal.valueOf(5), BigDecimal.valueOf(10), "Test 2 add 5.+"));
            boolean recordUpdated = documents.updateDocument(5, DateUtils.numbersToDate(2024, 6, 15), transGroup, false, account, "5*", documentDetailsForUpdate);
            System.out.println("Record updated = " + recordUpdated);

            //Delete
            boolean recordDeleted = documents.deleteDocument(2);
            System.out.println("Record deleted = " + recordDeleted);

            //Load list
            documents.loadDocumentsList(DateUtils.numbersToDate(2024, 5, 1), DateUtils.numbersToDate(2024, 5, 31));
            System.out.println("Load for may 2024 (" + documents.size() + "):");
            for (var i = 0; i < documents.size(); i++)
                System.out.println("*[" + documents.get(i).getId() + "] " + DateUtils.dateToStringISO(documents.get(i).getDocDate()) + " " + documents.get(i).getNotes() + " (" + documents.get(i).getCount() + "/S:" + documents.get(i).getTotal() + ")");

            //Load item
            Document document1 = documents.loadDocumentById(5);
            System.out.println("Load document:\nId: " + document1.getId()
                    + "\nDate: " + DateUtils.dateToStringISO(document1.getDocDate())
                    + "\nTrans group: " + document1.getTransGroup().name()
                    + "\nNotes: " + document1.getNotes()
                    + "\nCount: " + document1.getCount()
                    + "\nTotal: " + document1.getTotal()
                    + "\nDetails:");
            List<DocumentDetail> document1Details = document1.getDetails();
            for (var detail : document1Details)
                System.out.println(detail);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}