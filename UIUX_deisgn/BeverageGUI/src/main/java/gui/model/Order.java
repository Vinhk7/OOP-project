package gui.model;

import java.time.LocalDateTime;

/** status: "Preparing", "Completed" or "Cancelled" (same values as the report). */
public record Order(int id, int tableId, String tableName, String employeeName,
                    LocalDateTime time, long total, String status) { }
