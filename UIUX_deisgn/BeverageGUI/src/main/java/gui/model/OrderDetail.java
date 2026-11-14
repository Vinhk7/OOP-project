package gui.model;

import java.util.List;

// A drink with its size, sugar, ice and selected toppings.
public record OrderDetail(int productId, String productName, String size,
                          int sugarLevel, int iceLevel, int quantity,
                          long priceAtPurchase, List<OrderDetailTopping> toppings) {
    public OrderDetail {
        toppings = List.copyOf(toppings);
    }
}
