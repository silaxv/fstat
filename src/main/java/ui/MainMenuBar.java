package ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuBar extends JMenuBar implements ActionListener {

    private WorkPanel workPanel;

    private JMenu fileMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;

    public MainMenuBar() {
        createMenuBar();
    }

    public void setWorkPanel(WorkPanel workPanel) {
        this.workPanel = workPanel;
    }

    private void createMenuBar() {
        makeFileMenu();
        makeViewMenu();
        makeHelpMenu();
        this.add(fileMenu);
        this.add(viewMenu);
        this.add(helpMenu);
    }

    private void makeFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        fileMenu.addSeparator();
        fileMenu.add(makeMenuItem("Exit", MainMenuActions.EXIT, 'x'));
    }

    private void makeViewMenu() {
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        viewMenu.add(makeMenuItem("Transactions", MainMenuActions.TRANSACTIONS, 'T'));
        viewMenu.add(makeMenuItem("Reports", MainMenuActions.REPORTS, 'R'));
    }

    private void makeHelpMenu() {
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        helpMenu.add(makeMenuItem("About...", MainMenuActions.ABOUT, 'A'));
    }

    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case MainMenuActions.EXIT:
                ActionExit();
                break;
            case MainMenuActions.TRANSACTIONS:
                ActionTransactions();
                break;
            case MainMenuActions.REPORTS:
                ActionReports();
                break;
            case MainMenuActions.ABOUT:
                ActionAbout();
                break;
        }
    }

    private void ActionExit() {
        System.exit(0);
    }

    private void ActionTransactions() {
        workPanel.switchPage(WorkPanel.PAGE_TRANSACTIONS);
    }

    private void ActionReports() {
        workPanel.switchPage(WorkPanel.PAGE_REPORTS);
    }

    private void ActionAbout() {

    }

    private JMenuItem makeMenuItem(String menuName, String action, int mnemonic) {
        JMenuItem menu = new JMenuItem(menuName);
        menu.setActionCommand(action);
        menu.setMnemonic(mnemonic);
        menu.addActionListener(this);

        return menu;
    }

}
