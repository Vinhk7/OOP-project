package gui.service;

import gui.model.*;
import java.util.List;

/**
 * THE CONTRACT between the GUI tier and the Controller tier.
 * The GUI only calls these methods. Agree on the names with the controller teammate.
 * To connect the real backend, write a class that implements this interface
 * and swap it in MainApp (one line).
 */
public interface AppService {
    // --- account ---
    User login(String username, String password);               // null if wrong

    // --- categories ---
    List<Category> getCategories();
    void addCategory(String name);
    void renameCategory(int categoryId, String newName);
    boolean deleteCategory(int categoryId);                       // false if products still use it

    // --- products ---
    List<Product> getProducts();
    void addProduct(String name, int categoryId, long price, boolean available);
    void updateProduct(int productId, String name, int categoryId, long price, boolean available);
    void deleteProduct(int productId);

    // --- tables ---
    List<TableInfo> getTables();
    void setTableStatus(int tableId, String status);
    void addTable(String name);
    boolean deleteTable(int tableId);                             // false if table is occupied

    // --- orders ---
    List<Order> getOrders();
    void createOrder(int tableId, int userId, List<OrderLine> lines);
    List<OrderLine> getOrderLines(int orderId);                   // drinks inside one order
    void updateOrderStatus(int orderId, String status);           // Completed / Cancelled frees the table

    // --- employees ---
    List<Employee> getEmployees();
    boolean addEmployee(String username, String password, String fullName, String role, String phone); // false if username taken
    void updateEmployee(int employeeId, String fullName, String role, String phone);   // username cannot change
    void setEmployeeStatus(int employeeId, String status);        // Active / Inactive
    void deleteEmployee(int employeeId);
}
