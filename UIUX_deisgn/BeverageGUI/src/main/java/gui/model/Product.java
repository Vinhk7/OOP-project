package gui.model;

public record Product(int id, String name, int categoryId, long price,
                      boolean available, String imagePath, String description) {
    public Product(int id, String name, int categoryId, long price, boolean available) {
        this(id, name, categoryId, price, available, null, "");
    }
    public Product(int id, String name, int categoryId, long price,
                   boolean available, String imagePath) {
        this(id, name, categoryId, price, available, imagePath, "");
    }
}
