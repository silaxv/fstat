package data.controlles;

import data.DataManager;
import data.entities.*;
import utils.DateUtils;
import utils.NumericUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocumentController extends EntityController<Document> {

    private static final String TABLE_NAME = "documents";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "doc_date";
    private static final String COLUMN_TRANS_GROUP = "trans_group";
    private static final String COLUMN_ACT = "act";
    private static final String COLUMN_ACCOUNT = "f_accounts";
    private static final String COLUMN_NOTES = "notes";

    private static final String TABLE_DT_NAME = "documents_details";
    private static final String COLUMN_DT_ID = "id";
    private static final String COLUMN_DT_DOCUMENT = "f_documents";
    private static final String COLUMN_DT_PRODUCT = "f_products";
    private static final String COLUMN_DT_USER = "f_users";
    private static final String COLUMN_DT_QUANTITY = "quantity";
    private static final String COLUMN_DT_PRICE = "price";
    private static final String COLUMN_DT_SUBTOTAL = "subtotal";
    private static final String COLUMN_DT_NOTES = "notes";

    private CategoryController categories;
    private AccountController accounts;
    private ProductController products;
    private UserController users;

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
                COLUMN_NOTES + " VARCHAR(1024) NOT NULL"
        });

        dbController.createIndex(TABLE_NAME, 1, new String[] { COLUMN_DATE });

        dbController.createTable(TABLE_DT_NAME, new String[] {
                COLUMN_DT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
                COLUMN_DT_DOCUMENT + " INTEGER NOT NULL",
                COLUMN_DT_PRODUCT + " INTEGER NOT NULL",
                COLUMN_DT_USER + " INTEGER",
                COLUMN_DT_QUANTITY + " DECIMAL(15,3) NOT NULL",
                COLUMN_DT_PRICE + " MONEY NOT NULL",
                COLUMN_DT_SUBTOTAL + " MONEY NOT NULL",
                COLUMN_DT_NOTES + " VARCHAR(1024) NOT NULL"
        });

        dbController.createIndex(TABLE_DT_NAME, 1, new String[] { COLUMN_DT_DOCUMENT });

        //todo: create indexes
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

    public void loadDocumentsList(Date date0, Date date1) throws Exception {
        if (date0 == null || date1 == null)
            throw new Exception("Dates shouldn't be empty!");

        var condition = COLUMN_DATE + " BETWEEN " + DateUtils.dateToInt(date0) + " AND " + DateUtils.dateToInt(date1);
        loadList(condition, COLUMN_DATE + ", d." + COLUMN_ID);
    }

    public Document loadDocumentById(int id) throws Exception {
        Document document = loadItem("d." + COLUMN_ID + "=" + id);

        //Load details
        try {
            var sql = "SELECT " + COLUMN_DT_ID + ", "
                    + COLUMN_DT_DOCUMENT + ", "
                    + COLUMN_DT_PRODUCT + ", "
                    + COLUMN_DT_USER + ", "
                    + COLUMN_DT_QUANTITY + ", "
                    + COLUMN_DT_PRICE + ", "
                    + COLUMN_DT_SUBTOTAL + ", "
                    + COLUMN_DT_NOTES
                    + " FROM " + TABLE_DT_NAME
                    + " WHERE " + COLUMN_DT_DOCUMENT + "=" + id;
            List<Object[]> buffer = dbController.execQuery(sql);
            var documentDetails = new ArrayList<DocumentDetail>();
            for (var item : buffer) {
                DocumentDetail documentDetail = new DocumentDetail(
                        (int) item[0],
                        document,
                        products.getProductById((int) item[2]),
                        item[3] != null ? users.getUserById((int) item[3]) : null,
                        NumericUtils.objectToBigDecimal(item[4]),
                        NumericUtils.objectToBigDecimal(item[5]),
                        NumericUtils.objectToBigDecimal(item[6]),
                        (String) item[7]
                );
                documentDetails.add(documentDetail);
            }
            document.setDetails(documentDetails);
        } catch (Exception e) {
            throw new Exception("Can't load document details (id = " + id + ")\n" + e.getMessage());
        }

        return document;
    }

    public int addDocument(Date date, TransGroup transGroup, boolean act, Account account, String notes, List<DocumentDetail> details) throws Exception {
        checkParameters(date, transGroup, account, notes);
        int itemId;

        dbController.transactionBegin();
        try {
            itemId = addItem(
                    new String[]{COLUMN_DATE, COLUMN_TRANS_GROUP, COLUMN_ACT, COLUMN_ACCOUNT, COLUMN_NOTES},
                    new Object[]{date, transGroup.getId(), act, account.getId(), notes});
            //Add details
            if (details != null)
                addDocumentDetails(itemId, details);
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while adding document\n" + e.getMessage());
        } finally {
            dbController.transactionCommit();
        }

        return itemId;
    }

    public boolean updateDocument(int id, Date date, TransGroup transGroup, boolean act, Account account, String notes, List<DocumentDetail> details) throws Exception {
        checkParameters(date, transGroup, account, notes);
        boolean result;

        dbController.transactionBegin();
        try {
            result = updateItem(COLUMN_ID + "=" + id,
                    new String[] { COLUMN_DATE, COLUMN_TRANS_GROUP, COLUMN_ACT, COLUMN_ACCOUNT, COLUMN_NOTES },
                    new Object[] { date, transGroup.getId(), act, account.getId(), notes });
            //Update details
            if (result)
                updateDocumentDetails(id, details);
        } catch (Exception e) {
            dbController.transactionRollback();
            throw new Exception("Error while updating document\n" + e.getMessage());
        } finally {
            dbController.transactionCommit();
        }

        return result;
    }

    public boolean deleteDocument(long id) throws Exception {
        boolean result;

        dbController.transactionBegin();
        try {
            result = deleteItem(COLUMN_ID + "=" + id);
            if (result) {
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
        } finally {
            dbController.transactionCommit();
        }

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
            var condition = COLUMN_DT_DOCUMENT + "=" + documentId
                    + " AND " + COLUMN_DT_ID + " IN (" + sqlIdsDelete + ")";
            try {
                dbController.deleteData(TABLE_DT_NAME, condition);
            } catch (Exception e) {
                throw new Exception("Can't delete details items (" + condition + ")\n" + e.getMessage());
            }
        }

        //Update existing records
        try {
            var columnsNames = new String[] {
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
                if (detail.getProduct() == null)
                    throw new Exception("Product can't be null (detail id = " + detail.getId() + ")");
                var values = new Object[] {
                        detail.getProduct().getId(),
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
            }
        } catch (Exception e) {
            throw new Exception("Can't create a new item\n" + e.getMessage());
        }

        //Add new details records
        addDocumentDetails(documentId, details);
    }

    private void addDocumentDetails(int documentId, List<DocumentDetail> details) throws Exception {
        try {
            var columnsNames = new String[] {
                    COLUMN_DT_DOCUMENT,
                    COLUMN_DT_PRODUCT,
                    COLUMN_DT_USER,
                    COLUMN_DT_QUANTITY,
                    COLUMN_DT_PRICE,
                    COLUMN_DT_SUBTOTAL,
                    COLUMN_DT_NOTES
            };
            var columnsValues = new ArrayList<Object[]>();
            for (var detail : details) {
                if (detail == null || detail.getId() != null)
                    continue;
                if (detail.getProduct() == null)
                    throw new Exception("Product can't be null!");
                var row = new Object[] {
                        documentId,
                        detail.getProduct().getId(),
                        detail.getUser() != null ? detail.getUser().getId() : null,
                        detail.getQuantity(),
                        detail.getPrice(),
                        detail.getSubtotal(),
                        detail.getNotes()
                };
                columnsValues.add(row);
            }
            if (columnsValues.size() > 0)
                dbController.insertDataBatch(TABLE_DT_NAME, columnsNames, columnsValues);
        } catch (Exception e) {
            throw new Exception("Can't create a new detail record\n" + details.toString() + "\n" + e.getMessage());
        }
    }

    protected List<Object[]> selectItems(String condition, String orderBy) throws Exception {
        List<Object[]> buffer;

        var sql = "SELECT d." + COLUMN_ID + ", "
                + "d." + COLUMN_DATE + ", "
                + "d." + COLUMN_TRANS_GROUP + ", "
                + "d." + COLUMN_ACT + ", "
                + "d." + COLUMN_ACCOUNT + ", "
                + "d." + COLUMN_NOTES + ", "
                + "COUNT(dd." + COLUMN_DT_ID + "), "
                + "SUM(" + COLUMN_DT_SUBTOTAL  + ")"
                + " FROM " + TABLE_NAME + " AS d"
                + " LEFT JOIN " + TABLE_DT_NAME + " AS dd"
                + " ON d." + COLUMN_ID + " = dd." + COLUMN_DT_DOCUMENT
                + (condition != null && condition.length() > 0 ? " WHERE " + condition : "")
                + " GROUP BY d." + COLUMN_ID
                + (orderBy != null && orderBy.length() > 0 ? " ORDER BY " + orderBy : "");
        buffer = dbController.execQuery(sql);

        return buffer;
    }

    protected Document bufferToItem(Object[] row) throws Exception {
        Document item;
        item = new Document();

        item.setId((int) row[0]);
        item.setDocDate(DateUtils.intToDate((Integer) row[1]));
        item.setTransGroup(TransGroup.getValueById((int) row[2]));
        item.setAct((Integer) row[3] == 1);
        int accountId = (int) row[4];
        Account account = accounts.getAccountById(accountId);
        if (account == null)
            throw new Exception("Account with id = " + accountId + " is not found!");
        item.setAccount(account);
        item.setNotes((String) row[5]);
        item.setCount((Integer) row[6]);
        item.setTotal(NumericUtils.objectToBigDecimal(row[7]));

        return item;
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
