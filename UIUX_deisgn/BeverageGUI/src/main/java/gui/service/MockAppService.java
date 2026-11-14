package gui.service;

import gui.model.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Sample data for testing the JavaFX application.
 * All changes are kept in memory and reset when the app restarts.
 */
public class MockAppService implements AppService {
    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Topping> toppings = new ArrayList<>();
    private final Map<String, String> passwords = new HashMap<>();
    private final Map<Integer, List<OrderLine>> orderLines = new HashMap<>();
    // Stop adding orders when the demo reaches its memory limit.
    private static final int MAX_MOCK_ORDERS = 10000;
    private int nextCategoryId = 4, nextProductId = 16, nextOrderId = 1;
    private int nextEmployeeId = 3, nextCustomerId = 3, nextToppingId = 10;

    public MockAppService() {
        categories.add(new Category(1, "Traditional Vietnamese Coffee"));
        categories.add(new Category(2, "Espresso-Based Coffee"));
        categories.add(new Category(3, "Phindi"));

        // Product IDs and prices follow Cafeshop_db.docx.
        products.add(new Product(1, "Black Coffee", 1, 15000, true, "/images/black-coffee.jpg"));
        products.add(new Product(2, "Vietnamese Milk Coffee", 1, 15000, true, "/images/vietnamese-milk-coffee.jpg"));
        products.add(new Product(3, "Salted Coffee", 1, 20000, true, "/images/salted-coffee.jpg"));
        products.add(new Product(4, "Egg Coffee", 1, 20000, true, "/images/egg-coffee.jpg"));
        products.add(new Product(5, "Coconut Coffee", 1, 20000, true, "/images/coconut-coffee.jpg"));
        products.add(new Product(6, "Vietnamese White Coffee", 1, 15000, true, "/images/vietnamese-white-coffee.jpg"));
        products.add(new Product(7, "Espresso", 2, 35000, true, "/images/espresso.jpg"));
        products.add(new Product(8, "Americano", 2, 35000, true, "/images/americano.jpg"));
        products.add(new Product(9, "Cappuccino", 2, 45000, true, "/images/cappuccino.jpg"));
        products.add(new Product(10, "Latte", 2, 45000, true, "/images/latte.jpg"));
        products.add(new Product(11, "Caramel Macchiato", 2, 55000, true, "/images/caramel-macchiato.jpg"));
        products.add(new Product(12, "Mocha", 2, 55000, true, "/images/mocha.jpg"));
        products.add(new Product(13, "Phin Coffee with Milk Cream", 3, 40000, true, "/images/phin-coffee-with-milk-cream.jpg"));
        products.add(new Product(14, "Almond Phin Coffee", 3, 40000, true, "/images/almond-phin-coffee.jpg"));
        products.add(new Product(15, "Chocolate Phin Coffee", 3, 40000, true, "/images/chocolate-phin-coffee.jpg"));

        // Default accounts for manager and staff
        employees.add(new Employee(1, "manager01", "Nguyen Van An", "ADMIN", "0901234567", "Active", 15000000));
        employees.add(new Employee(2, "staff01", "Tran Minh Anh", "STAFF", "0912345678", "Active", 6000000));
        passwords.put("manager01", "123456");
        passwords.put("staff01", "123456");

        customers.add(new Customer(1, "104240047", "Nguyễn Phúc Vinh", 1000));
        customers.add(new Customer(2, "123456789", "Alexander đệ tam", 0));
        String[] names = {"Black Pearl", "Salted Cream", "Cream", "White Pearl",
                "Coffee Jelly", "Flan", "Toasted Coconut", "Whipped Cream", "Cocoa Powder"};
        for (int i = 0; i < names.length; i++) {
            toppings.add(new Topping(i + 1, names[i], i == 4 || i == 5 || i == 6 || i == 7 ? 7000 : 5000, true));
        }
        // Orders start empty because the document has no sample invoices.
    }

    @Override public User login(String username, String password) {
        if (username == null || password == null) return null;
        for (Employee e : employees) {
            if (e.username().equals(username) && e.status().equals("Active")
                    && password.equals(passwords.get(username))) {
                return new User(e.id(), e.username(), e.fullName(), e.role());
            }
        }
        return null;
    }

    @Override public List<Category> getCategories() { return new ArrayList<>(categories); }
    @Override public void addCategory(String name) { categories.add(new Category(nextCategoryId++, name)); }
    @Override public void renameCategory(int id, String name) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).id() == id) {
                categories.set(i, new Category(id, name));
                break;
            }
        }
    }
    @Override public boolean deleteCategory(int id) {
        for (Product p : products) if (p.categoryId() == id) return false;
        return categories.removeIf(c -> c.id() == id);
    }

    @Override public List<Product> getProducts() { return new ArrayList<>(products); }
    @Override public void addProduct(String name, int categoryId, long price, boolean available) {
        products.add(new Product(nextProductId++, name, categoryId, price, available));
    }
    @Override public void updateProduct(int id, String name, int categoryId, long price, boolean available) {
        for (int i = 0; i < products.size(); i++) {
            Product old = products.get(i);
            if (old.id() == id) {
                products.set(i, new Product(id, name, categoryId, price, available, old.imagePath(), old.description()));
                break;
            }
        }
    }
    @Override public void deleteProduct(int id) {
        // Keep old products for order history, but hide them from the menu.
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.id() == id) {
                products.set(i, new Product(p.id(), p.name(), p.categoryId(), p.price(), false, p.imagePath(), p.description()));
                break;
            }
        }
    }

    @Override public List<Customer> getCustomers() { return new ArrayList<>(customers); }
    @Override public Customer findCustomer(String phone) {
        for (Customer c : customers) if (c.phone().equals(phone)) return c;
        return null;
    }
    @Override public boolean addCustomer(String name, String phone) {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()
                || findCustomer(phone) != null) return false;
        customers.add(new Customer(nextCustomerId++, phone, name, 0));
        return true;
    }
    @Override public List<Topping> getToppings() { return new ArrayList<>(toppings); }
    @Override public void addTopping(String name, long price) {
        toppings.add(new Topping(nextToppingId++, name, price, true));
    }
    @Override public void updateTopping(int id, String name, long price, boolean active) {
        for (int i = 0; i < toppings.size(); i++) {
            if (toppings.get(i).id() == id) {
                toppings.set(i, new Topping(id, name, price, active));
                break;
            }
        }
    }
    @Override public boolean deleteTopping(int id) {
        for (Order o : orders) {
            for (OrderLine line : getOrderLines(o.id())) {
                for (OrderDetailTopping t : line.getToppings()) if (t.toppingId() == id) return false;
            }
        }
        return toppings.removeIf(t -> t.id() == id);
    }

    @Override public List<Order> getOrders() {
        List<Order> copy = new ArrayList<>(orders);
        Collections.reverse(copy);
        return copy;
    }
    @Override public void createOrder(int tableId, int userId, List<OrderLine> lines) {
        createOrder(userId, lines, "CASH", null, 0);
    }
    @Override public void createOrder(int userId, List<OrderLine> lines, String method,
                                      String phone, int pointsUsed) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Cart is empty");
        if (orders.size() >= MAX_MOCK_ORDERS) {
            throw new IllegalStateException("Mock order limit reached. Use a database to store more orders.");
        }
        if (!"CASH".equals(method) && !"QR".equals(method)) throw new IllegalArgumentException("Invalid payment method");
        String employeeName = null;
        for (Employee e : employees) {
            if (e.id() == userId && e.status().equals("Active")) employeeName = e.fullName();
        }
        if (employeeName == null) throw new IllegalArgumentException("Invalid staff account");

        long total = 0;
        List<OrderLine> details = new ArrayList<>();
        for (OrderLine line : lines) {
            if (line == null || line.getQuantity() <= 0) throw new IllegalArgumentException("Invalid item");
            total = Math.addExact(total, line.getSubtotal());
            details.add(new OrderLine(line));
        }
        Customer customer = phone == null || phone.isBlank() ? null : findCustomer(phone);
        if (phone != null && !phone.isBlank() && customer == null) throw new IllegalArgumentException("Customer not found");
        if (pointsUsed < 0 || pointsUsed > total || (customer == null && pointsUsed > 0)
                || (customer != null && pointsUsed > customer.points())) {
            throw new IllegalArgumentException("Not enough loyalty points");
        }
        // Demo rule: 1 point = 1 VND. Completed orders earn 1 point per 1000 VND.
        long payable = total - pointsUsed;
        int id = nextOrderId++;
        orders.add(new Order(id, 0, "Counter", employeeName, LocalDateTime.now(),
                payable, "PENDING", customer == null ? null : customer.id(), pointsUsed, method));
        orderLines.put(id, details);
        if (customer != null && pointsUsed > 0) changePoints(customer.id(), -pointsUsed);
    }
    private void changePoints(int id, int amount) {
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            if (c.id() == id) {
                customers.set(i, new Customer(c.id(), c.phone(), c.fullName(), c.points() + amount));
                return;
            }
        }
    }
    @Override public List<OrderLine> getOrderLines(int id) {
        List<OrderLine> copy = new ArrayList<>();
        for (OrderLine line : orderLines.getOrDefault(id, List.of())) copy.add(new OrderLine(line));
        return copy;
    }
    @Override public void updateOrderStatus(int id, String status) {
        if (!"COMPLETED".equals(status) && !"CANCELLED".equals(status)) return;
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            if (o.id() == id && "PENDING".equals(o.status())) {
                orders.set(i, new Order(o.id(), o.tableId(), o.tableName(), o.employeeName(),
                        o.time(), o.total(), status, o.customerId(), o.pointsUsed(), o.paymentMethod()));
                if (o.customerId() != null) {
                    if ("COMPLETED".equals(status)) changePoints(o.customerId(), (int)(o.total() / 1000));
                    if ("CANCELLED".equals(status)) changePoints(o.customerId(), o.pointsUsed());
                }
                break;
            }
        }
    }

    @Override public List<Employee> getEmployees() { return new ArrayList<>(employees); }
    @Override public boolean addEmployee(String username, String password, String name, String role, String phone) {
        for (Employee e : employees) if (e.username().equalsIgnoreCase(username)) return false;
        employees.add(new Employee(nextEmployeeId++, username, name, role, phone, "Active"));
        passwords.put(username, password);
        return true;
    }
    @Override public void updateEmployee(int id, String name, String role, String phone) {
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e.id() == id) {
                employees.set(i, new Employee(id, e.username(), name, role, phone, e.status(), e.salary()));
                break;
            }
        }
    }
    @Override public void setEmployeeStatus(int id, String status) {
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e.id() == id) {
                employees.set(i, new Employee(id, e.username(), e.fullName(), e.role(), e.phone(), status, e.salary()));
                break;
            }
        }
    }
    @Override public void deleteEmployee(int id) {
        for (Employee e : new ArrayList<>(employees)) {
            if (e.id() == id) {
                employees.remove(e);
                passwords.remove(e.username());
                break;
            }
        }
    }
}
