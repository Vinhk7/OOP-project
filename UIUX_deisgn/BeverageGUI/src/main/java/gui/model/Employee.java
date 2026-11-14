package gui.model;

// Employee information used by the admin screens.
public record Employee(int id, String username, String fullName, String role,
                       String phone, String status, long salary) {
    public Employee(int id, String username, String fullName, String role,
                    String phone, String status) {
        this(id, username, fullName, role, phone, status, 0);
    }
}
