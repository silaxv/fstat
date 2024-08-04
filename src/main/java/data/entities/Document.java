package data.entities;

/*
    TABLE documents
        id INT
        doc_date DATE
        trans_group TINYINT
        act BIT
        f_accounts INT
        notes VARCHAR(1024)
*/

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Document {

    private int id;
    private Date docDate;
    private TransGroup transGroup;
    private boolean act;
    private Account account;
    private String notes;
    private List<DocumentDetail> details;
    private Integer count;
    private BigDecimal total;

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