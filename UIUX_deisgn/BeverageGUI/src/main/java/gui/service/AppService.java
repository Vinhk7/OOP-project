package gui.service;

import gui.model.*;
import java.util.List;

/** Methods used by the JavaFX screens to work with sample data. */
public interface AppService {
    User login(String username, String password);

    // Categories and products
    List<Category> getCategories();
    void addCategory(String name);
    void renameCategory(int categoryId, String newName);
    boolean deleteCategory(int categoryId);
    List<Product> getProducts();
    void addProduct(String name, int categoryId, long price, boolean available);
    void updateProduct(int productId, String name, int categoryId, long price, boolean available);
    void deleteProduct(int productId);

    // Customer loyalty and toppings
    List<Customer> getCustomers();
    Customer findCustomer(String phone);
    boolean addCustomer(String name, String phone);
    List<Topping> getToppings();
    void addTopping(String name, long price);
    void updateTopping(int id, String name, long price, boolean active);
    boolean deleteTopping(int id);

    // Orders
    List<Order> getOrders();
    void createOrder(int tableId, int userId, List<OrderLine> lines);
    void createOrder(int userId, List<OrderLine> lines, String paymentMethod,
                     String customerPhone, int pointsUsed);
    List<OrderLine> getOrderLines(int orderId);
    void updateOrderStatus(int orderId, String status);

    // Employees
    List<Employee> getEmployees();
    boolean addEmployee(String username, String password, String fullName, String role, String phone);
    void updateEmployee(int employeeId, String fullName, String role, String phone);
    void setEmployeeStatus(int employeeId, String status);
    void deleteEmployee(int employeeId);
}
