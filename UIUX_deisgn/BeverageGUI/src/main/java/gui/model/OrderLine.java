package gui.model;

public class OrderLine {
    private final Product product;
    private int quantity;

    public OrderLine(Product product, int quantity) { this.product = product; this.quantity = quantity; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }
    public long getSubtotal() { return product.price() * quantity; }
}
