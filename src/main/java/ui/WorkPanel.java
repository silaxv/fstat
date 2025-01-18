package ui;

import data.DataManager;
import data.controlles.AccountController;
import utils.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class WorkPanel extends JPanel {

    DataManager dataManager = DataManager.getInstance();
    AccountController accounts = dataManager.getAccounts();

    final static String PAGE_TRANSACTIONS = "TRANS";
    final static String PAGE_REPORTS = "REPORTS";

    private Date periodBegin;
    private Date periodEnd;

    private TransactionsPage pageTransactions;
    private ReportsPage pageReports;

    private final Window parentWindow;

    private JTextField periodField;
    private JPanel pages;
    private String currentPage;

    public WorkPanel(final Window parentWindow) {
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout());
        init();
        initListeners();

        //Default period
        Date currentDate = DateUtils.getCurrentDate();
        setPeriod(DateUtils.dateMonthFirstDay(currentDate), DateUtils.dateMonthLastDay(currentDate));
    }

    public void switchPage(String pageName) {
        if (pageName.equals(currentPage))
            return;

        CardLayout cardLayout = (CardLayout) pages.getLayout();
        cardLayout.show(pages, pageName);
        currentPage = pageName;
        updatePages();

        System.out.println("Page: " + currentPage);
    }

    public void setPeriod(Date periodBegin, Date periodEnd) {
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
        updatePeriodField();
        updatePages();

        System.out.println("Period: " + DateUtils.dateToStringISO(periodBegin));
    }

    public void insertTransactionAction() {
        if (PAGE_TRANSACTIONS.equals(currentPage))
            pageTransactions.createTransaction();
    }

    public void deleteTransactionAction() {
        if (PAGE_TRANSACTIONS.equals(currentPage))
            pageTransactions.deleteTransaction();
    }

    public Window getParentWindow() {
        return parentWindow;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    private void init() {
        JPanel toolBarPanel = new JPanel();
        add(toolBarPanel, BorderLayout.NORTH);

        initPeriodPanel(toolBarPanel);

        //Pages
        pages = new JPanel(new CardLayout());
        add(pages, BorderLayout.CENTER);

        //Transactions page
        pageTransactions = new TransactionsPage(this);
        pages.add(pageTransactions, PAGE_TRANSACTIONS);
        pageTransactions.updatePage();

        //Reports page
        pageReports = new ReportsPage(this);
        pages.add(pageReports, PAGE_REPORTS);
        pageReports.updatePage();

        //Default page
        switchPage(PAGE_TRANSACTIONS);
    }

    private void initPeriodPanel(JPanel toolBarPanel) {
        //Period panel
        JLabel labelPeriod = new JLabel("Period");
        toolBarPanel.add(labelPeriod);

        JButton buttonPeriodPrev = new JButton("<");
        toolBarPanel.add(buttonPeriodPrev);
        buttonPeriodPrev.addActionListener(e -> switchPeriod(-1));

        periodField = new JTextField();
        toolBarPanel.add(periodField);
        periodField.setEditable(false);

        JButton buttonPeriodNext = new JButton(">");
        toolBarPanel.add(buttonPeriodNext);
        buttonPeriodNext.addActionListener(e -> switchPeriod(1));

        JButton buttonPeriod = new JButton("...");
        toolBarPanel.add(buttonPeriod);
        buttonPeriod.addActionListener(e -> changePeriod());
    }

    private void initListeners() {
    }

    private void updatePeriodField() {
        periodField.setText(DateUtils.dateToStringISO(periodBegin) + " - " + DateUtils.dateToStringISO(periodEnd));
    }

    private void updatePages() {
        if (PAGE_TRANSACTIONS.equals(currentPage))
            pageTransactions.updatePage();
        if (PAGE_REPORTS.equals(currentPage))
            pageReports.updatePage();
    }

    private void changePeriod() {
        Date newPeriodBegin = periodBegin;
        Date newPeriodEnd = periodEnd;

        //Temp solution
        if (newPeriodEnd.compareTo(newPeriodBegin) != 0) {
            newPeriodEnd = newPeriodBegin;
        } else {
            newPeriodBegin = DateUtils.dateMonthFirstDay(newPeriodBegin);
            newPeriodEnd = DateUtils.dateMonthLastDay(newPeriodBegin);
        }

        setPeriod(newPeriodBegin, newPeriodEnd);
    }

    private void switchPeriod(int offs) {
        Date newPeriodBegin = periodBegin;
        Date newPeriodEnd = periodEnd;

        if (newPeriodEnd.compareTo(newPeriodBegin) != 0) {
            Date firstDayOfPeriod = DateUtils.dateMonthFirstDay(newPeriodBegin);
            if (offs == -1)
                firstDayOfPeriod = DateUtils.dateMonthFirstDay(DateUtils.dateAddDays(firstDayOfPeriod, -1));
            else if (offs == 1)
                firstDayOfPeriod = DateUtils.dateMonthFirstDay(DateUtils.dateAddDays(DateUtils.dateMonthLastDay(firstDayOfPeriod), 1));
            newPeriodBegin = firstDayOfPeriod;
            newPeriodEnd = DateUtils.dateMonthLastDay(firstDayOfPeriod);
        } else {
            newPeriodBegin = DateUtils.dateAddDays(newPeriodBegin, offs);
            newPeriodEnd = newPeriodBegin;
        }

        setPeriod(newPeriodBegin, newPeriodEnd);
    }

}
