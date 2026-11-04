package gui.model;

/** Logged-in account. Replace with the team's User entity later. */
public record User(int id, String username, String fullName, String role) {
    public boolean isManager() { return "Manager".equals(role); }
}
