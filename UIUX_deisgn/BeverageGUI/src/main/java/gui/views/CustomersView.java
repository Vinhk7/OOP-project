package gui.views;

import gui.Ui;
import gui.model.Customer;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CustomersView extends VBox {
    private final AppService service;
    private final TableView<Customer> table = new TableView<>();

    public CustomersView(AppService service) {
        super(12);
        this.service = service;
        Button add = new Button("Add customer");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> addCustomer());
        table.getColumns().addAll(
                Ui.<Customer>col("Name", Customer::fullName),
                Ui.<Customer>col("Phone", Customer::phone),
                Ui.<Customer>col("Points", c -> String.valueOf(c.points())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(add, table);
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(service.getCustomers())); }

    private void addCustomer() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add customer");
        TextField name = new TextField();
        TextField phone = new TextField();
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(12));
        form.addRow(0, new Label("Full name"), name);
        form.addRow(1, new Label("Phone"), phone);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            if (!service.addCustomer(name.getText().trim(), phone.getText().trim())) {
                Ui.error("Enter a name and a phone number that is not registered.");
            } else load();
        });
    }
}
