package data.entities;

/*
    TABLE documents
        id INT
        doc_date DATE
        trans_group TINYINT
        act BOOLEAN
        exclude BOOLEAN
        account_id INT
        notes VARCHAR(1024)
*/

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Document {

    public static final String TABLE_NAME = "documents";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "doc_date";
    public static final String COLUMN_TRANS_GROUP = "trans_group";
    public static final String COLUMN_ACT = "act";
    public static final String COLUMN_EXCLUDE = "exclude";
    public static final String COLUMN_ACCOUNT = "account_id";
    public static final String COLUMN_NOTES = "notes";

    private int id;
    private Date docDate;
    private TransGroup transGroup;
    private boolean act;
    private boolean exclude;
    private Account account;
    private String notes;
    private List<DocumentDetail> details;
    private Integer count;
    private BigDecimal total;

    public Document() {
        this.details = new ArrayList<>();
        this.exclude = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }

    public TransGroup getTransGroup() {
        return transGroup;
    }

    public void setTransGroup(TransGroup transGroup) {
        this.transGroup = transGroup;
    }

    public boolean isAct() {
        return act;
    }

    public void setAct(boolean act) {
        this.act = act;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<DocumentDetail> getDetails() {
        return details;
    }

    public DocumentDetail getMainDetail() {
        for (var detail : details) {
            if (detail.isMain())
                return detail;
        }

        return null;
    }

    public void setDetails(List<DocumentDetail> details) {
        this.details = details;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
