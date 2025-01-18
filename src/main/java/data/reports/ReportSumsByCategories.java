package data.reports;

import data.DataManager;
import data.DatabaseController;
import data.controlles.CategoryController;
import data.entities.Category;
import utils.DateUtils;
import utils.NumericUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportSumsByCategories implements ReportGenerator {

    protected final DatabaseController dbController;

    private CategoryController categories;

    private List<ReportTableByCategories> items;

    private static class ReportTableByCategories {

        public Category category;
        public BigDecimal sum;

        public ReportTableByCategories(Category category, BigDecimal sum) {
            this.category = category;
            this.sum = sum;
        }
    }

    public ReportSumsByCategories() {
        DataManager dataManager = DataManager.getInstance();
        this.dbController = dataManager.getDatabaseController();
    }

    public void setCategories(CategoryController categories) {
        this.categories = categories;
    }

    @Override
    public void initReport(Date date0, Date date1) throws Exception {
        items = new ArrayList<>();

        try {
            if (categories == null)
                throw new Exception("No categories passed to the report");

            var sql = "SELECT\n" +
                    "c.id,\n" +
                    "c.parent_id,\n" +
                    "SUM(dd.subtotal) AS summ\n" +
                    "FROM categories AS c\n" +
                    "INNER JOIN documents_details AS dd\n" +
                    "ON dd.category_id = c.id\n" +
                    "LEFT JOIN documents AS d\n" +
                    "ON d.id = dd.document_id\n" +
                    "WHERE d.doc_date BETWEEN " + DateUtils.dateToInt(date0) + " AND " + DateUtils.dateToInt(date1) + "\n" +
                    "AND d.act = 1\n" +
                    "GROUP BY c.id, c.parent_id, c.name\n" +
                    "ORDER BY summ DESC";
            List<Object[]> buffer = dbController.execQuery(sql);
            for (var row : buffer) {
                Integer categoryId = (Integer) row[0];
                items.add(new ReportTableByCategories(categoryId != null ? categories.getCategoryById(categoryId) : null, NumericUtils.objectToBigDecimal(row[2])));
            }
        } catch (Exception e) {
            throw new Exception("Can't exec query\n" + e.getMessage());
        }

    }

    @Override
    public String getReportText() {
        StringBuilder repText = new StringBuilder();

        for (var item : items) {
            repText.append(item.category.getName()).append(" ").append(NumericUtils.numberToCurrencyString(item.sum)).append(System.lineSeparator());
        }

        return "Report Sums by categories" + System.lineSeparator() + "========" + System.lineSeparator() + repText;
    }

}
