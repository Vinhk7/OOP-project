package gui.views;

import gui.Fmt;
import gui.model.*;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Left: menu cards (search + category filter). Right: current order with running total. */
public class OrderView extends HBox {
    private final AppService service;
    private final ObservableList<OrderLine> lines = FXCollections.observableArrayList();
    private final ListView<OrderLine> list = new ListView<>(lines);
    private final Label total = new Label();
    private final FlowPane cards = new FlowPane(12, 12);
    private final TextField search = new TextField();
    private final ComboBox<Category> filter = new ComboBox<>();
    private static final Category ALL = new Category(0, "All");

    public OrderView(AppService service, User user, TableInfo table, Runnable onDone) {
        super(20);
        this.service = service;

        // ----- left: menu -----
        search.setPromptText("Search drinks…");
        filter.getItems().add(ALL);
        filter.getItems().addAll(service.getCategories());
        filter.setValue(ALL);
        search.textProperty().addListener((o, a, b) -> refreshCards());
        filter.valueProperty().addListener((o, a, b) -> refreshCards());
        HBox tools = new HBox(10, search, filter);
        HBox.setHgrow(search, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(cards);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("clear-scroll");
        VBox left = new VBox(12, tools, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        // ----- right: order -----
        Label head = new Label("Current order");
        head.getStyleClass().add("subtitle");
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(OrderLine l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null
                        : l.getProduct().name() + "  x" + l.getQuantity() + "   " + Fmt.money(l.getSubtotal()));
            }
        });
        list.setPlaceholder(new Label("No drinks yet.\nClick a drink to add it."));
        Button plus = new Button("+");
        Button minus = new Button("–");
        Button remove = new Button("Remove");
        plus.setOnAction(e -> changeQty(1));
        minus.setOnAction(e -> changeQty(-1));
        remove.setOnAction(e -> lines.remove(list.getSelectionModel().getSelectedItem()));
        HBox qtyBar = new HBox(8, plus, minus, remove);

        total.getStyleClass().add("total");
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> onDone.run());
        Button confirm = new Button("Confirm order");
        confirm.getStyleClass().add("primary");
        confirm.setTooltip(new Tooltip("Ctrl+Enter"));
        cancel.setTooltip(new Tooltip("Esc"));
        confirm.setOnAction(e -> {
            if (lines.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Please add at least one drink.").showAndWait();
                return;
            }
            service.createOrder(table.getId(), user.id(), lines);
            onDone.run();
        });
        HBox actions = new HBox(10, cancel, confirm);

        VBox right = new VBox(12, head, list, qtyBar, total, actions);
        right.setPrefWidth(340);
        right.setPadding(new Insets(16));
        right.getStyleClass().add("card");
        VBox.setVgrow(list, Priority.ALWAYS);

        getChildren().addAll(left, right);
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) { onDone.run(); e.consume(); }
            else if (e.getCode() == javafx.scene.input.KeyCode.ENTER && e.isControlDown()) { confirm.fire(); e.consume(); }
        });
        refreshCards();
        refreshTotal();
        lines.addListener((javafx.collections.ListChangeListener<OrderLine>) c -> refreshTotal());
    }

    private void refreshCards() {
        cards.getChildren().clear();
        String q = search.getText() == null ? "" : search.getText().toLowerCase().trim();
        Category cat = filter.getValue();
        for (Product p : service.getProducts()) {
            if (!q.isEmpty() && !p.name().toLowerCase().contains(q)) continue;
            if (cat != null && cat.id() != 0 && p.categoryId() != cat.id()) continue;
            Button b = new Button(p.name() + "\n" + Fmt.money(p.price()));
            b.setPrefSize(150, 90);
            b.setWrapText(true);
            b.getStyleClass().add("product-card");
            b.setDisable(!p.available());
            b.setOnAction(e -> addProduct(p));
            cards.getChildren().add(b);
        }
        if (cards.getChildren().isEmpty()) {
            Label none = new Label("No drinks match your search.");
            none.getStyleClass().add("muted");
            cards.getChildren().add(none);
        }
    }

    private void addProduct(Product p) {
        for (OrderLine l : lines) {
            if (l.getProduct().id() == p.id()) {
                l.setQuantity(l.getQuantity() + 1);
                list.refresh();
                refreshTotal();
                return;
            }
        }
        lines.add(new OrderLine(p, 1));
    }

    private void changeQty(int delta) {
        OrderLine l = list.getSelectionModel().getSelectedItem();
        if (l == null) return;
        int q = l.getQuantity() + delta;
        if (q <= 0) lines.remove(l); else l.setQuantity(q);
        list.refresh();
        refreshTotal();
    }

    private void refreshTotal() {
        long sum = lines.stream().mapToLong(OrderLine::getSubtotal).sum();
        total.setText("Total: " + Fmt.money(sum));
    }
}
