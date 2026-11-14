package gui.model;

import java.util.ArrayList;
import java.util.List;

public class OrderLine {
    private final Product product;
    private int quantity;
    private final String size;
    private final int sugarLevel;
    private final int iceLevel;
    private final List<OrderDetailTopping> toppings;
    private final long priceAtPurchase;

    public OrderLine(Product product, int quantity) {
        this(product, quantity, "S", 100, 100, List.of());
    }

    public OrderLine(Product product, int quantity, String size,
                     int sugarLevel, int iceLevel, List<OrderDetailTopping> toppings) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        this.product = product;
        this.quantity = quantity;
        this.size = size;
        this.sugarLevel = sugarLevel;
        this.iceLevel = iceLevel;
        this.toppings = List.copyOf(toppings);
        // The DOCX does not specify extra prices for M and L.
        this.priceAtPurchase = product.price();
    }

    public OrderLine(OrderLine other) {
        this.product = other.product;
        this.quantity = other.quantity;
        this.size = other.size;
        this.sugarLevel = other.sugarLevel;
        this.iceLevel = other.iceLevel;
        this.toppings = other.toppings;
        this.priceAtPurchase = other.priceAtPurchase;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) {
        if (q < 1) throw new IllegalArgumentException("Quantity must be positive");
        quantity = q;
    }
    public String getSize() { return size; }
    public int getSugarLevel() { return sugarLevel; }
    public int getIceLevel() { return iceLevel; }
    public List<OrderDetailTopping> getToppings() { return toppings; }
    public long getPriceAtPurchase() { return priceAtPurchase; }
    public long getUnitPrice() {
        long total = priceAtPurchase;
        for (OrderDetailTopping t : toppings) total += t.price();
        return total;
    }
    public long getSubtotal() { return getUnitPrice() * quantity; }
    public OrderDetail toDetail() {
        return new OrderDetail(product.id(), product.name(), size, sugarLevel,
                iceLevel, quantity, priceAtPurchase, toppings);
    }
    public boolean sameOptions(OrderLine other) {
        return product.id() == other.product.id() && size.equals(other.size)
                && sugarLevel == other.sugarLevel && iceLevel == other.iceLevel
                && toppings.equals(other.toppings);
    }
}
