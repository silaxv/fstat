package ui.controls;

public class DataTableColumn {

    private String name;
    private final Class<?> columnClass;
    private int width;

    public DataTableColumn(String name, Class<?> aClass, int width) {
        this.name = name;
        this.columnClass = aClass;
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
