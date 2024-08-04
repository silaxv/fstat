import data.DataManager;
import ui.MainForm;

public class MainApp {

    public static String PROPERTIES_FILE_NAME = "fstat.properties"; //todo: correct prop file

    public static void main(String[] args) {
        try {
            initApp();
        } catch (Exception e) {
            System.out.println(e.getMessage()); //todo: msg dialog
        }

        MainForm mainForm = new MainForm();
    }

    private static void initApp() throws Exception {
        DataManager dataManager = DataManager.getInstance();
        dataManager.init();
    }

}
