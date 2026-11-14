package gui.model;

import java.time.LocalDateTime;

// Status follows the Orders table: PENDING, COMPLETED or CANCELLED.
public record Order(int id, int tableId, String tableName, String employeeName,
                    LocalDateTime time, long total, String status,
                    Integer customerId, int pointsUsed, String paymentMethod) {
    // Keep the old constructor so existing views still work.
    public Order(int id, int tableId, String tableName, String employeeName,
                 LocalDateTime time, long total, String status) {
        this(id, tableId, tableName, employeeName, time, total, status, null, 0, "CASH");
    }
}
