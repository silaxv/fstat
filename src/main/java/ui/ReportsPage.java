package ui;

import data.DataManager;
import data.controlles.CategoryController;
import data.reports.ReportGenerator;
import data.reports.ReportSumsByCategories;

import javax.swing.*;
import java.awt.*;

public class ReportsPage extends JPanel {

    private final DataManager dataManager = DataManager.getInstance();

    private final CategoryController categories = dataManager.getCategories();

    private final WorkPanel parentPanel;

    private ReportGenerator reportGenerator1;

    private JTextArea reportTextArea;

    public ReportsPage(final WorkPanel parentPanel) {
        this.parentPanel = parentPanel;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        add(reportTextArea, BorderLayout.CENTER);
    }

    private void showReport1() {
        reportGenerator1 = new ReportSumsByCategories();
        ((ReportSumsByCategories)reportGenerator1).setCategories(categories);
        try {
            generateReport(reportGenerator1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Rep1");
    }

    private void generateReport(ReportGenerator reportGenerator) throws Exception {
        reportGenerator.initReport(parentPanel.getPeriodBegin(), parentPanel.getPeriodEnd());
        String reportText = reportGenerator.getReportText();
        reportTextArea.setText(reportText);
    }

    public void updatePage() {
        showReport1();
    }

}
