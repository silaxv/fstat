package ui;

import data.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainForm extends JFrame {

    // The menu bar
    private MainMenuBar menuBar;

    private WorkPanel mainPanel;

    public MainForm() {
        initUITheme();

        initForm();
        setVisible(true);

        initProperties();
    }

    private void initUITheme() {
        UIManager.put( "control", new Color(129, 129, 129) );
        UIManager.put( "info", new Color(96, 96, 96) );
        UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
        UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
        UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
        UIManager.put( "nimbusFocus", new Color(115,164,209) );
        UIManager.put( "nimbusGreen", new Color(176,179,50) );
        UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
        UIManager.put( "nimbusLightBackground", new Color( 18, 30, 49) );
        UIManager.put( "nimbusOrange", new Color(191,98,4) );
        UIManager.put( "nimbusRed", new Color(169,46,34) );
        UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
        UIManager.put( "nimbusSelectionBackground", new Color( 104, 93, 156) );
        UIManager.put( "text", new Color( 230, 230, 230) );
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (javax.swing.UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create the GUI components and layout.
    private void initForm() {
        JPanel all = new JPanel(new BorderLayout());

        menuBar = new MainMenuBar();
        setJMenuBar(menuBar);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //What happens when the frame closes?
        setTitle(Constants.DEFAULT_TITLE);

        mainPanel = new WorkPanel(this);
        menuBar.setWorkPanel(mainPanel);

        JSplitPane topAndDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        topAndDown.setOneTouchExpandable(true);
        topAndDown.setDividerLocation(0.8);
        topAndDown.setResizeWeight(.8);
        topAndDown.setContinuousLayout(true);
        topAndDown.setBorder(null);
        topAndDown.setTopComponent(mainPanel);
        all.add(topAndDown, BorderLayout.CENTER);

        getContentPane().add(all);
        setMinimumSize(new Dimension(1400, 820));
        pack();
        setLocationRelativeTo(null);

        //Keys
        getRootPane().registerKeyboardAction(e -> mainPanel.insertTransactionAction(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> mainPanel.deleteTransactionAction(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void initProperties() {
        //todo: initProperties
    }

}
