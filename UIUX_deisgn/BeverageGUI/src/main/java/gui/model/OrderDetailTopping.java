package gui.model;

// Keep the topping price from the time the drink was ordered.
public record OrderDetailTopping(int toppingId, String name, long price) { }
