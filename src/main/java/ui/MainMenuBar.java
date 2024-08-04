package ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuBar extends JMenuBar implements ActionListener {

    private JMenu fileMenu;
    private JMenu helpMenu;

    public MainMenuBar() {
        createMenuBar();
    }

    private void createMenuBar() {
        makeFileMenu();
        makeHelpMenu();
        this.add(fileMenu);
        this.add(helpMenu);
    }

    private void makeFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        fileMenu.addSeparator();
        fileMenu.add(makeMenuItem("Exit", MainMenuActions.EXIT, 'x'));
    }

    private void makeHelpMenu() {
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        helpMenu.add(makeMenuItem("About...", MainMenuActions.ABOUT, 'C'));
    }

    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case MainMenuActions.EXIT:
                ActionExit();
                break;
            case MainMenuActions.ABOUT:
                ActionAbout();
                break;
        }
    }

    private void ActionExit() {
        System.exit(0);
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
