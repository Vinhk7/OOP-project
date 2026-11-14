package gui.model;

public record User(int id, String username, String fullName, String role) {
    public boolean isManager() { return "ADMIN".equals(role); }
}
