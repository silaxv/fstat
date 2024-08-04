package data.entities;

public enum TransGroup {
    INCOME (1),
    EXPENSES (2),
    TRANSFER (3);

    private final int id;

    TransGroup(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TransGroup getValueById(int id) {
        for (TransGroup item : TransGroup.values())
            if (item.getId() == id)
                return item;

        return null;
    }

}
