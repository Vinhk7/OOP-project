package gui.model;

public class TableInfo {
    private final int id;
    private final String name;
    private String status; // "Available" or "Occupied"

    public TableInfo(int id, String name, String status) {
        this.id = id; this.name = name; this.status = status;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isAvailable() { return "Available".equals(status); }
}
