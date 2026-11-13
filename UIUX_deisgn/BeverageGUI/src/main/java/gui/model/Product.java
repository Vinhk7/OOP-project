package gui.model;

public record Product(int id, String name, int categoryId, long price, boolean available, String imagePath) {
    public Product(int id, String name, int categoryId, long price, boolean available) {
        this(id, name, categoryId, price, available, null);
    }
}