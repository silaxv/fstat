package ui;

import javax.swing.*;
import java.text.SimpleDateFormat;

public class WorkPanel extends JPanel {

    private JButton buttonTest;

    public WorkPanel() {
        init();
        initListeners();
    }

    private void init() {
        JLabel emptyLabel = new JLabel("Test");
        add(emptyLabel);
        buttonTest = new JButton("Test");
        add(buttonTest);
    }

    private void initListeners() {
        buttonTest.addActionListener(e -> {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        });
    }

}
