package gui.model;

/** status: "Active" or "Inactive". role: "Manager" or "Staff". */
public record Employee(int id, String username, String fullName, String role, String phone, String status) { }
