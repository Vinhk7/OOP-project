package gui.views;

import gui.Fmt;
import gui.Ui;
import gui.model.Order;
import gui.model.OrderLine;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;

/** Order history with status filter. Preparing orders can be completed or cancelled. */
public class OrdersView extends VBox {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AppService service;
    private final TableView<Order> table = new TableView<>();
    private final ComboBox<String> filter = new ComboBox<>();

    @SuppressWarnings("unchecked")
    public OrdersView(AppService service) {
        super(12);
        this.service = service;

        filter.getItems().addAll("All", "PENDING", "COMPLETED", "CANCELLED");
        filter.setValue("All");
        filter.valueProperty().addListener((o, a, b) -> load());

        Button done = new Button("Mark completed");
        done.getStyleClass().add("primary");
        done.setOnAction(e -> change("COMPLETED"));

        Button cancel = new Button("Cancel order");
        cancel.getStyleClass().add("danger");
        cancel.setOnAction(e -> change("CANCELLED"));

        Button details = new Button("View details");
        details.setOnAction(e -> openSelected());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, new Label("Status:"), filter, spacer, details, done, cancel);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TableColumn<Order, String> statusCol = Ui.<Order>col("Status", Order::status);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    String style = switch (status) {
                        case "COMPLETED" -> "-fx-background-color: #E6F4EA; -fx-text-fill: #1E7E34;";
                        case "CANCELLED" -> "-fx-background-color: #FCE8E6; -fx-text-fill: #C5221F;";
                        default -> "-fx-background-color: #FEF7E0; -fx-text-fill: #B06000;";
                    };
                    badge.setStyle(style + " -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3 10; -fx-background-radius: 12;");
                    setText(null);
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(
                Ui.<Order>col("#", o -> String.valueOf(o.id())),
                Ui.<Order>col("Staff", Order::employeeName),
                Ui.<Order>col("Date", o -> o.time().format(FMT)),
                Ui.<Order>col("Total", o -> Fmt.money(o.total())),
                statusCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No orders match this filter."));
        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) showDetails(row.getItem());
            });
            return row;
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(bar, table);
        load();
    }

    private void load() {
        String f = filter.getValue();
        table.setItems(FXCollections.observableArrayList(service.getOrders().stream()
                .filter(o -> f == null || f.equals("All") || o.status().equals(f)).toList()));
    }

    private void change(String newStatus) {
        Order o = table.getSelectionModel().getSelectedItem();
        if (o == null) { Ui.info("Please select an order first."); return; }
        if (!o.status().equals("PENDING")) { Ui.info("Only pending orders can be updated."); return; }
        if (Ui.confirm("Set order #" + o.id() + " to " + newStatus + "?")) {
            service.updateOrderStatus(o.id(), newStatus);
            load();
        }
    }

    private void openSelected() {
        Order o = table.getSelectionModel().getSelectedItem();
        if (o == null) { Ui.info("Please select an order first."); return; }
        showDetails(o);
    }

    private void showDetails(Order o) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Order #" + o.id());
        d.getDialogPane().setContent(buildDetails(service, o));
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }

    /** The content of the order-detail popup: header, drinks table, total. */
    @SuppressWarnings("unchecked")
    static VBox buildDetails(AppService service, Order o) {
        Label head = new Label("Order #" + o.id() + "  •  " + o.employeeName() + "  •  " + o.time().format(FMT) + "  •  " + o.status());
        head.getStyleClass().add("subtitle");

        TableView<OrderLine> t = new TableView<>(FXCollections.observableArrayList(service.getOrderLines(o.id())));
        t.getColumns().addAll(
                Ui.<OrderLine>col("Drink", l -> l.getProduct().name()),
                Ui.<OrderLine>col("Options", l -> l.getSize() + " / " + l.getSugarLevel() + "% sugar / " + l.getIceLevel() + "% ice"),
                Ui.<OrderLine>col("Qty", l -> String.valueOf(l.getQuantity())),
                Ui.<OrderLine>col("Unit price", l -> Fmt.money(l.getUnitPrice())),
                Ui.<OrderLine>col("Subtotal", l -> Fmt.money(l.getSubtotal())));
        t.getColumns().get(0).setPrefWidth(240);
        t.getColumns().get(1).setPrefWidth(220);
        t.getColumns().get(2).setPrefWidth(50);
        t.getColumns().get(3).setPrefWidth(110);
        t.getColumns().get(4).setPrefWidth(110);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No drinks recorded for this order."));
        t.setPrefSize(520, 220);

        Label total = new Label("Payment: " + o.paymentMethod() + " | Points used: " + o.pointsUsed()
                + " | Total: " + Fmt.money(o.total()));
        total.getStyleClass().add("total");

        VBox box = new VBox(12, head, t, total);
        box.setPadding(new Insets(8));
        return box;
    }
}
