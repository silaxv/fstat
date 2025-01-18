package data.controlles;

import data.DataManager;
import data.entities.*;
import utils.DateUtils;
import utils.NumericUtils;

import java.util.*;

import static data.entities.Document.*;
import static data.entities.DocumentDetail.*;

public class DocumentController extends EntityController<Document> {

    private CategoryController categories;
    private AccountController accounts;
    private ProductController products;
    private UserController users;
    private TagController tags;

    public DocumentController(DataManager dataManager) {
        super(dataManager, TABLE_NAME);
    }

    public void createTable() throws Exception {
        dbController.createTable(TABLE_NAME, new String[] {
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_DATE + " DATE NOT NULL",
                COLUMN_TRANS_GROUP + " TINYINT NOT NULL",
                COLUMN_ACCOUNT + " INTEGER NOT NULL",
                COLUMN_ACT + " BOOLEAN NOT NULL",
                COLUMN_EXCLUDE + " BOOLEAN NOT NULL",
                COLUMN_NOTES + " VARCHAR(1024) NOT NULL"
        });

        dbController.createIndex(TABLE_NAME, 1, new String[] { COLUMN_DATE });

        dbController.createTable(TABLE_DT_NAME, new String[] {
                COLUMN_DT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_DT_DOCUMENT + " INTEGER NOT NULL",
                COLUMN_DT_MAIN + " BOOLEAN NOT NULL",
                COLUMN_DT_CATEGORY + " INTEGER",
                COLUMN_DT_PRODUCT + " INTEGER",
                COLUMN_DT_USER + " INTEGER",
                COLUMN_DT_QUANTITY + " DECIMAL(15,3) NOT NULL",
                COLUMN_DT_PRICE + " MONEY NOT NULL",
                COLUMN_DT_SUBTOTAL + " MONEY NOT NULL",
                COLUMN_DT_NOTES + " VARCHAR(1024) NOT NULL",
                "FOREIGN KEY(\"" + COLUMN_DT_DOCUMENT + "\") REFERENCES \"" + TABLE_NAME + "\"(\"" + COLUMN_ID + "\")"
        });

        dbController.createIndex(TABLE_DT_NAME, 1, new String[] { COLUMN_DT_DOCUMENT });
    }

    @Override
    public void dropTable() throws Exception {
        super.dropTable();
        if (dbController.tableExists(TABLE_DT_NAME))
            dbController.dropTable(TABLE_DT_NAME);
    }

    public void setCategories(CategoryController categories) {
        this.categories = categories;
    }

    public void setAccounts(AccountController accounts) {
        this.accounts = accounts;
    }

    public void setProducts(ProductController products) {
        this.products = products;
    }

    public void setUsers(UserController users) {
        this.users = users;
    }

    public void setTags(TagController tags) {
        this.tags = tags;
    }

    public Document getDocumentById(int id) {
        for (var i = 0; i < size(); i++) {
            Document document = get(i);
            if (document.getId() == id) {
                return document;
            }
        }

        return null;
    }

    public void loadDocumentsList(Date date0, Date date1) throws Exception {
        if (date0 == null || date1 == null)
            throw new Exception("Dates shouldn't be empty!");

        var condition = COLUMN_DATE + " BETWEEN " + DateUtils.dateToInt(date0) + " AND " + DateUtils.dateToInt(date1);
        loadList(condition, COLUMN_DATE + ", d." + COLUMN_ID);
    }

    public Document loadDocumentById(int id) throws Exception {
        Document document = loadItem("d." + COLUMN_ID + "=" + id);

        var documentDetails = new ArrayList<DocumentDetail>();

        //Load details
        try {
            var sql = "SELECT " + COLUMN_DT_ID + ", "
                    + COLUMN_DT_DOCUMENT + ", "
                    + COLUMN_DT_MAIN + ", "
                    + COLUMN_DT_CATEGORY + ", "
                    + COLUMN_DT_PRODUCT + ", "
                    + COLUMN_DT_USER + ", "
                    + COLUMN_DT_QUANTITY + ", "
                    + COLUMN_DT_PRICE + ", "
                    + COLUMN_DT_SUBTOTAL + ", "
                    + COLUMN_DT_NOTES
                    + " FROM " + TABLE_DT_NAME
                    + " WHERE " + COLUMN_DT_DOCUMENT + "=" + id;
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var item : buffer) {
                DocumentDetail documentDetail = new DocumentDetail(
                        (int) item[0],
                        document,
                        (Integer) item[2] == 1,
                        item[3] != null ? categories.getCategoryById((int) item[3]): null,
                        item[4] != null ? products.getProductById((int) item[4]): null,
                        item[5] != null ? users.getUserById((int) item[5]) : null,
                        NumericUtils.objectToBigDecimal(item[6]),
                        NumericUtils.objectToBigDecimal(item[7]),
                        NumericUtils.objectToBigDecimal(item[8]),
                        (String) item[9]
                );
                documentDetails.add(documentDetail);
            }
        } catch (Exception e) {
            throw new Exception("Can't load document details (id = " + id + ")\n" + e.getMessage());
        }

        //Load tags
        List<TagRef> tagRefList = tags.loadDocumentTags(document, documentDetails);
        for (var documentDetail : documentDetails) {
            List<Tag> tags = new ArrayList<>();
            for (var tagRef : tagRefList) {
                if (tagRef.getDocumentDetail() == documentDetail)
                    tags.add(tagRef.getTag());
            }
            documentDetail.setTags(tags);
        }

        document.setDetails(documentDetails);

        return document;
    }

    public static DocumentDetail getDocumentDetail(Integer documentDetailId, List<DocumentDetail> documentDetails) throws Exception {
        int documentDetailIdInt = documentDetailId != null ? documentDetailId : -1;
        DocumentDetail documentDetail = null;

        for (var documentDetailItem : documentDetails) {
            if (documentDetailItem.getId() == documentDetailIdInt) {
                documentDetail = documentDetailItem;
                break;
            }
        }
        if (documentDetail == null)
            throw new Exception("A document detail with id = " + documentDetailIdInt + " not found");

        return documentDetail;
    }

    public int addDocument(Date date, TransGroup transGroup, boolean act, boolean exclude, Account account, String notes, List<DocumentDetail> details, List<Product> newProducts) throws Exception {
        checkParameters(date, transGroup, account, notes);
        int itemId;

        dbController.transactionBegin();
        //Create new products
        createNewProducts(newProducts);
        //Create a document
        try {
            itemId = addItem(
                    new String[]{COLUMN_DATE, COLUMN_TRANS_GROUP, COLUMN_ACT, COLUMN_EXCLUDE, COLUMN_ACCOUNT, COLUMN_NOTES},
                    new Object[]{date, transGroup.getId(), act, exclude, account.getId(), notes});
            //Add details
            if (details != null)
                addDocumentDetails(itemId, details);
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while adding document\n" + e.getMessage());
        }
        dbController.transactionCommit();

        return itemId;
    }

    public boolean updateDocument(int id, Date date, TransGroup transGroup, boolean act, boolean exclude, Account account, String notes, List<DocumentDetail> details, List<Product> newProducts) throws Exception {
        checkParameters(date, transGroup, account, notes);
        boolean result;

        dbController.transactionBegin();
        //Create new products
        createNewProducts(newProducts);
        try {
            result = updateItem(COLUMN_ID + "=" + id,
                    new String[] { COLUMN_DATE, COLUMN_TRANS_GROUP, COLUMN_ACT, COLUMN_EXCLUDE, COLUMN_ACCOUNT, COLUMN_NOTES },
                    new Object[] { date, transGroup.getId(), act, exclude, account.getId(), notes });
            //Update details
            if (result)
                updateDocumentDetails(id, details);
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while updating document\n" + e.getMessage());
        }
        dbController.transactionCommit();

        return result;
    }

    public boolean deleteDocument(int id) throws Exception {
        boolean result;

        dbController.transactionBegin();
        try {
            result = deleteItem(COLUMN_ID + "=" + id);
            if (result) {
                //Delete tags refs
                tags.deleteTagsRefsByDocumentId(id);
                //Delete details
                var condition = COLUMN_DT_DOCUMENT + "=" + id;
                try {
                    dbController.deleteData(TABLE_DT_NAME, condition);
                } catch (Exception e) {
                    throw new Exception("Can't delete details items (" + condition + ")\n" + e.getMessage());
                }
            }
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while deleting document\n" + e.getMessage());
        }
        dbController.transactionCommit();

        return result;
    }

    private void updateDocumentDetails(int documentId, List<DocumentDetail> details) throws Exception {
        //Get ids
        List<Integer> detailsIds = new ArrayList<>();
        try {
            var sql = "SELECT " + COLUMN_DT_ID + " FROM " + TABLE_DT_NAME + " WHERE " + COLUMN_DT_DOCUMENT + "=" + documentId;
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var item : buffer)
                detailsIds.add((Integer) item[0]);
        } catch (Exception e) {
            throw new Exception("Can't load details ids (documentId = " + documentId + ")\n" + e.getMessage());
        }

        //Delete records
        StringBuilder sqlIdsDelete = new StringBuilder();
        for (var item : detailsIds) {
            boolean recordFound = false;
            for (var detail : details) {
                if (item.equals(detail.getId())) {
                    recordFound = true;
                    break;
                }
            }
            if (!recordFound)
                sqlIdsDelete.append(sqlIdsDelete.length() > 0 ? ", " : "").append(item);
        }
        if (sqlIdsDelete.length() > 0) {
            //Delete tags refs
            tags.deleteTagsRefs(TagRef.COLUMN_DOCUMENT_DETAIL + " IN (" + sqlIdsDelete + ")");
            //Delete details
            var condition = COLUMN_DT_DOCUMENT + "=" + documentId
                    + " AND " + COLUMN_DT_ID + " IN (" + sqlIdsDelete + ")";
            try {
                dbController.deleteData(TABLE_DT_NAME, condition);
            } catch (Exception e) {
                throw new Exception("Can't delete details items (" + condition + ")\n" + e.getMessage());
            }
            System.out.println("DT DELETE: Ids: " + sqlIdsDelete);
        }

        //Update existing records
        try {
            var columnsNames = new String[] {
                    COLUMN_DT_MAIN,
                    COLUMN_DT_CATEGORY,
                    COLUMN_DT_PRODUCT,
                    COLUMN_DT_USER,
                    COLUMN_DT_QUANTITY,
                    COLUMN_DT_PRICE,
                    COLUMN_DT_SUBTOTAL,
                    COLUMN_DT_NOTES
            };
            for (var detail : details) {
                if (detail == null || detail.getId() == null || !detail.isModified())
                    continue;
                //Update tags
                tags.updateTags(detail, detail.getTags());
                //Update detail
                var values = new Object[] {
                        detail.isMain(),
                        detail.getCategory() != null ? detail.getCategory().getId() : null,
                        detail.getProduct() != null ? detail.getProduct().getId() : null,
                        detail.getUser() != null ? detail.getUser().getId() : null,
                        detail.getQuantity(),
                        detail.getPrice(),
                        detail.getSubtotal(),
                        detail.getNotes()
                };
                var condition = COLUMN_DT_DOCUMENT + "=" + documentId
                        + " AND " + COLUMN_DT_ID + "=" + detail.getId();
                int res = dbController.updateData(TABLE_DT_NAME, condition, columnsNames, values);
                if (res == 0)
                    throw new Exception("The detail with an id = " + detail.getId() + " wasn't updated!");
                System.out.println("DT UPDATE: " + detail);
            }
        } catch (Exception e) {
            throw new Exception("Can't create a new item\n" + e.getMessage());
        }

        //Add new details records
        addDocumentDetails(documentId, details);
    }

    private void createNewProducts(List<Product> newProducts) throws Exception {
        if (newProducts == null || newProducts.isEmpty())
            return;

        try {
            for (var product : newProducts) {
                int productId = products.addProduct(product.getCategory(), product.getTransGroup(), product.getName());
                product.setId(productId);
                product.setTemporary(false);
            }
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while adding new products\n" + e.getMessage());
        }
    }

    private void addDocumentDetails(int documentId, List<DocumentDetail> details) throws Exception {
        try {
            var columnsNames = new String[] {
                    COLUMN_DT_DOCUMENT,
                    COLUMN_DT_MAIN,
                    COLUMN_DT_CATEGORY,
                    COLUMN_DT_PRODUCT,
                    COLUMN_DT_USER,
                    COLUMN_DT_QUANTITY,
                    COLUMN_DT_PRICE,
                    COLUMN_DT_SUBTOTAL,
                    COLUMN_DT_NOTES
            };
            var columnsValues = new ArrayList<Object[]>();
            var tagsRefs = new ArrayList<TagRef>();
            for (var detail : details) {
                if (detail == null || detail.getId() != null)
                    continue;
                var row = new Object[] {
                        documentId,
                        detail.isMain(),
                        detail.getCategory() != null ? detail.getCategory().getId() : null,
                        detail.getProduct() != null ? detail.getProduct().getId() : null,
                        detail.getUser() != null ? detail.getUser().getId() : null,
                        detail.getQuantity(),
                        detail.getPrice(),
                        detail.getSubtotal(),
                        detail.getNotes()
                };
                columnsValues.add(row);
                //Collect tags refs into the list
                for (var tag : detail.getTags()) {
                    tagsRefs.add(new TagRef(tag, detail));
                }
                System.out.println("DT ADD: " + detail);
            }
            //Insert records
            if (!columnsValues.isEmpty()) {
                int[] keys = dbController.insertDataBatch(TABLE_DT_NAME, columnsNames, columnsValues);
                int keyIndex = 0;
                //Assign ids for new items
                for (var detail : details) {
                    if (detail != null && detail.getId() == null)
                        detail.setId(keys[keyIndex++]);
                }
                //Add tags refs
                tags.addTags(tagsRefs);
            }
        } catch (Exception e) {
            throw new Exception("Can't create a new detail record\n" + details.toString() + "\n" + e.getMessage());
        }
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT d." + COLUMN_ID + ", " //0
                + "d." + COLUMN_DATE + ", " //1
                + "d." + COLUMN_TRANS_GROUP + ", " //2
                + "d." + COLUMN_ACT + ", " //3
                + "d." + COLUMN_EXCLUDE + ", " //4
                + "d." + COLUMN_ACCOUNT + ", " //5
                + "d." + COLUMN_NOTES + ", " //6
                + "COUNT(dd." + COLUMN_DT_ID + "), " //7
                + "SUM(" + COLUMN_DT_SUBTOTAL  + "), " //8
                + "MAX(CASE WHEN dd." + COLUMN_DT_MAIN + " = 1 THEN dd." + COLUMN_DT_CATEGORY + " END), " //9
                + "MAX(CASE WHEN dd." + COLUMN_DT_MAIN + " = 1 THEN dd." + COLUMN_DT_PRODUCT + " END)," //10
                + "MAX(CASE WHEN dd." + COLUMN_DT_MAIN + " = 1 THEN dd." + COLUMN_DT_USER + " END)" //11
                + " FROM " + TABLE_NAME + " AS d"
                + " LEFT JOIN " + TABLE_DT_NAME + " AS dd"
                + " ON d." + COLUMN_ID + " = dd." + COLUMN_DT_DOCUMENT
                + (condition != null && !condition.isEmpty() ? " WHERE " + condition : "")
                + " GROUP BY d." + COLUMN_ID
                + (orderBy != null && !orderBy.isEmpty() ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected Document bufferToItem(Object[] row) throws Exception {
        var document = new Document();

        //Fill document's values
        document.setId((int) row[0]);
        document.setDocDate(DateUtils.intToDate((Integer) row[1]));
        document.setTransGroup(TransGroup.getValueById((int) row[2]));
        document.setAct((Integer) row[3] == 1);
        document.setExclude((Integer) row[4] == 1);
        int accountId = (int) row[5];
        Account account = accounts.getAccountById(accountId);
        if (account == null)
            throw new Exception("Account with id = " + accountId + " is not found!");
        document.setAccount(account);
        document.setNotes((String) row[6]);
        document.setCount((Integer) row[7]);
        document.setTotal(NumericUtils.objectToBigDecimal(row[8]));

        //Create fake main detail for the list
        var documentDetail = new DocumentDetail(document);
        Integer categoryId = (Integer) row[9];
        documentDetail.setCategory(categoryId != null ? categories.getCategoryById(categoryId) : null);
        Integer productId = (Integer) row[10];
        documentDetail.setProduct(productId != null ? products.getProductById(productId) : null);
        Integer userId = (Integer) row[11];
        documentDetail.setUser(userId != null ? users.getUserById(userId) : null);
        documentDetail.setMain(true);
        document.getDetails().add(documentDetail);

        return document;
    }

    private void checkParameters(Date date, TransGroup transGroup, Account account, String notes) throws Exception {
        if (date == null)
            throw new Exception("Date shouldn't be empty!");
        if (transGroup == null)
            throw new Exception("Trans group shouldn't be empty!");
        if (account == null)
            throw new Exception("Account shouldn't be empty!");
        if (notes == null)
            throw new Exception("Notes shouldn't be empty!");
    }


}
