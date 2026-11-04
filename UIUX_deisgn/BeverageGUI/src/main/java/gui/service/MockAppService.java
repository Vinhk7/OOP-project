package gui.service;

import gui.model.*;
import java.time.LocalDateTime;
import java.util.*;

/** Fake data (same samples as the report) so the GUI runs without the backend. */
public class MockAppService implements AppService {
    private final List<Category> categories = new ArrayList<>(List.of(
            new Category(1, "Coffee"), new Category(2, "Milk Tea")));
    private final List<Product> products = new ArrayList<>(List.of(
            new Product(1, "Americano", 1, 35000, true),
            new Product(2, "Brown Sugar Milk Tea", 2, 45000, true),
            new Product(3, "Latte", 1, 45000, true),
            new Product(4, "Espresso", 1, 30000, false),
            new Product(5, "Matcha Milk Tea", 2, 50000, true)));
    private final List<TableInfo> tables = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>(List.of(
            new Employee(1, "manager01", "Nguyen Van An", "Manager", "0901234567", "Active"),
            new Employee(2, "staff01", "Tran Minh Anh", "Staff", "0912345678", "Active")));
    private final Map<String, String> passwords = new HashMap<>(Map.of("manager01", "123456", "staff01", "123456"));
    private final Map<Integer, List<OrderLine>> orderLines = new HashMap<>();
    private int nextCategoryId = 3, nextProductId = 6, nextTableId = 9, nextOrderId = 3, nextEmployeeId = 3;

    public MockAppService() {
        for (int i = 1; i <= 8; i++)
            tables.add(new TableInfo(i, String.format("Table %02d", i), i == 2 || i == 5 ? "Occupied" : "Available"));
        orders.add(new Order(1, 1, "Table 01", "Tran Minh Anh", LocalDateTime.of(2026, 10, 17, 14, 30), 80000, "Completed"));
        orders.add(new Order(2, 2, "Table 02", "Tran Minh Anh", LocalDateTime.of(2026, 10, 17, 15, 10), 90000, "Preparing"));
        orderLines.put(1, List.of(new OrderLine(products.get(0), 1), new OrderLine(products.get(1), 1)));
        orderLines.put(2, List.of(new OrderLine(products.get(1), 2)));
    }

    @Override public User login(String u, String p) {
        // like the real thing: right password AND an Active account
        if (!p.equals(passwords.get(u))) return null;
        return employees.stream()
                .filter(e -> e.username().equals(u) && e.status().equals("Active"))
                .map(e -> new User(e.id(), e.username(), e.fullName(), e.role()))
                .findFirst().orElse(null);
    }

    // categories
    @Override public List<Category> getCategories() { return new ArrayList<>(categories); }
    @Override public void addCategory(String name) { categories.add(new Category(nextCategoryId++, name)); }
    @Override public void renameCategory(int id, String name) {
        for (int i = 0; i < categories.size(); i++)
            if (categories.get(i).id() == id) categories.set(i, new Category(id, name));
    }
    @Override public boolean deleteCategory(int id) {
        if (products.stream().anyMatch(p -> p.categoryId() == id)) return false;
        categories.removeIf(c -> c.id() == id);
        return true;
    }

    // products
    @Override public List<Product> getProducts() { return new ArrayList<>(products); }
    @Override public void addProduct(String name, int categoryId, long price, boolean available) {
        products.add(new Product(nextProductId++, name, categoryId, price, available));
    }
    @Override public void updateProduct(int id, String name, int categoryId, long price, boolean available) {
        for (int i = 0; i < products.size(); i++)
            if (products.get(i).id() == id) products.set(i, new Product(id, name, categoryId, price, available));
    }
    @Override public void deleteProduct(int id) { products.removeIf(p -> p.id() == id); }

    // tables
    @Override public List<TableInfo> getTables() { return new ArrayList<>(tables); }
    @Override public void setTableStatus(int id, String status) {
        tables.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> t.setStatus(status));
    }
    @Override public void addTable(String name) { tables.add(new TableInfo(nextTableId++, name, "Available")); }
    @Override public boolean deleteTable(int id) {
        if (tables.stream().anyMatch(t -> t.getId() == id && !t.isAvailable())) return false;
        tables.removeIf(t -> t.getId() == id);
        return true;
    }

    // orders
    @Override public List<Order> getOrders() {
        List<Order> copy = new ArrayList<>(orders);
        Collections.reverse(copy); // newest first
        return copy;
    }
    @Override public void createOrder(int tableId, int userId, List<OrderLine> lines) {
        long total = lines.stream().mapToLong(OrderLine::getSubtotal).sum();
        String tableName = tables.stream().filter(t -> t.getId() == tableId).map(TableInfo::getName).findFirst().orElse("?");
        String who = employees.stream().filter(e -> e.id() == userId).map(Employee::fullName).findFirst().orElse("?");
        int id = nextOrderId++;
        orders.add(new Order(id, tableId, tableName, who, LocalDateTime.now(), total, "Preparing"));
        orderLines.put(id, lines.stream().map(l -> new OrderLine(l.getProduct(), l.getQuantity())).toList());
        setTableStatus(tableId, "Occupied");
    }
    @Override public List<OrderLine> getOrderLines(int orderId) { return orderLines.getOrDefault(orderId, List.of()); }
    @Override public void updateOrderStatus(int orderId, String status) {
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            if (o.id() == orderId) {
                orders.set(i, new Order(o.id(), o.tableId(), o.tableName(), o.employeeName(), o.time(), o.total(), status));
                if (!status.equals("Preparing")) setTableStatus(o.tableId(), "Available");
            }
        }
    }

    // employees
    @Override public List<Employee> getEmployees() { return new ArrayList<>(employees); }
    @Override public boolean addEmployee(String username, String password, String fullName, String role, String phone) {
        if (employees.stream().anyMatch(e -> e.username().equalsIgnoreCase(username))) return false;
        passwords.put(username, password);
        employees.add(new Employee(nextEmployeeId++, username, fullName, role, phone, "Active"));
        return true;
    }
    @Override public void updateEmployee(int id, String fullName, String role, String phone) {
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e.id() == id) employees.set(i, new Employee(id, e.username(), fullName, role, phone, e.status()));
        }
    }
    @Override public void setEmployeeStatus(int id, String status) {
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e.id() == id) employees.set(i, new Employee(id, e.username(), e.fullName(), e.role(), e.phone(), status));
        }
    }
    @Override public void deleteEmployee(int id) { employees.removeIf(e -> e.id() == id); }
}
