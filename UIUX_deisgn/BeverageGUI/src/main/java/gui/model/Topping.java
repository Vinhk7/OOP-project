package gui.model;

public record Topping(int id, String name, long price, boolean active) {
    @Override public String toString() { return name + " (" + price + " VND)"; }
}
