package gui.views;

import gui.Ui;
import gui.Fmt;
import gui.model.Topping;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ToppingsView extends VBox {
    private final AppService service;
    private final TableView<Topping> table = new TableView<>();

    public ToppingsView(AppService service) {
        super(12);
        this.service = service;
        Button add = new Button("Add topping");
        Button edit = new Button("Edit selected");
        Button delete = new Button("Delete selected");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> edit(null));
        edit.setOnAction(e -> { if (table.getSelectionModel().getSelectedItem() != null) edit(table.getSelectionModel().getSelectedItem()); });
        delete.setOnAction(e -> {
            Topping t = table.getSelectionModel().getSelectedItem();
            if (t == null) return;
            if (Ui.confirm("Delete " + t.name() + "?")) {
                if (!service.deleteTopping(t.id())) Ui.error("This topping is already used in an order.");
                load();
            }
        });
        table.getColumns().addAll(
                Ui.<Topping>col("Name", Topping::name),
                Ui.<Topping>col("Price", t -> Fmt.money(t.price())),
                Ui.<Topping>col("Status", t -> t.active() ? "Available" : "Inactive"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(new HBox(10, add, edit, delete), table);
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(service.getToppings())); }

    private void edit(Topping original) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Add topping" : "Edit topping");
        TextField name = new TextField(original == null ? "" : original.name());
        TextField price = new TextField(original == null ? "5000" : String.valueOf(original.price()));
        CheckBox active = new CheckBox("Available");
        active.setSelected(original == null || original.active());
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(12));
        form.addRow(0, new Label("Name"), name);
        form.addRow(1, new Label("Price"), price);
        form.add(active, 1, 2);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try {
                long value = Long.parseLong(price.getText().trim());
                if (name.getText().isBlank() || value < 0) throw new IllegalArgumentException();
                if (original == null) service.addTopping(name.getText().trim(), value);
                else service.updateTopping(original.id(), name.getText().trim(), value, active.isSelected());
                load();
            } catch (IllegalArgumentException ex) { Ui.error("Enter a name and a valid price."); }
        });
    }
}
