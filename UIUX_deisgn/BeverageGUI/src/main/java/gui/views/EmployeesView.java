package gui.views;

import gui.Ui;
import gui.model.Employee;
import gui.model.User;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class EmployeesView extends VBox {
    private final AppService service;
    private final User current;
    private final TableView<Employee> table = new TableView<>();

    @SuppressWarnings("unchecked")
    public EmployeesView(AppService service, User current) {
        super(12);
        this.service = service;
        this.current = current;

        Button add = new Button("+ Add employee");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> openDialog(null));
        Button edit = new Button("Edit selected");
        edit.setOnAction(e -> { Employee emp = selected(); if (emp != null) openDialog(emp); });
        table.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2 && !row.isEmpty()) openDialog(row.getItem()); });
            return row;
        });
        Button toggle = new Button("Activate / Deactivate");
        toggle.setOnAction(e -> {
            Employee emp = selected();
            if (emp == null) return;
            if (emp.id() == current.id()) { Ui.error("You cannot deactivate your own account."); return; }
            service.setEmployeeStatus(emp.id(), emp.status().equals("Active") ? "Inactive" : "Active");
            load();
        });
        Button del = new Button("Delete");
        del.getStyleClass().add("danger");
        del.setOnAction(e -> {
            Employee emp = selected();
            if (emp == null) return;
            if (emp.id() == current.id()) { Ui.error("You cannot delete your own account."); return; }
            if (Ui.confirm("Delete employee \"" + emp.fullName() + "\"?")) { service.deleteEmployee(emp.id()); load(); }
        });

        table.getColumns().addAll(
                Ui.<Employee>col("Username", Employee::username),
                Ui.<Employee>col("Full name", Employee::fullName),
                Ui.<Employee>col("Role", Employee::role),
                Ui.<Employee>col("Phone", Employee::phone),
                Ui.<Employee>col("Status", Employee::status));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No employees found."));
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(new HBox(10, add, edit, toggle, del), table);
        load();
    }

    private Employee selected() {
        Employee e = table.getSelectionModel().getSelectedItem();
        if (e == null) Ui.info("Please select an employee first.");
        return e;
    }

    private void load() { table.setItems(FXCollections.observableArrayList(service.getEmployees())); }

    /** One dialog for both Add (existing == null) and Edit. Username and password are only set when adding. */
    private void openDialog(Employee existing) {
        boolean adding = existing == null;
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(adding ? "Add employee" : "Edit employee");
        TextField username = new TextField(adding ? "" : existing.username());
        username.setDisable(!adding);
        PasswordField password = new PasswordField();
        TextField fullName = new TextField(adding ? "" : existing.fullName());
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Staff", "Manager"));
        role.setValue(adding ? "Staff" : existing.role());
        TextField phone = new TextField(adding ? "" : existing.phone());

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        g.addRow(0, new Label("Username"), username);
        int row = 1;
        if (adding) g.addRow(row++, new Label("Password"), password);
        g.addRow(row++, new Label("Full name"), fullName);
        g.addRow(row++, new Label("Role"), role);
        g.addRow(row, new Label("Phone"), phone);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (username.getText().isBlank() || fullName.getText().isBlank() || (adding && password.getText().length() < 6)) {
                Ui.error(adding ? "Username and full name are required, and the password needs at least 6 characters."
                                : "Full name is required.");
                return;
            }
            if (!phone.getText().isBlank() && !phone.getText().trim().matches("\\d{9,11}")) {
                Ui.error("Phone must be 9 to 11 digits.");
                return;
            }
            if (adding) {
                if (!service.addEmployee(username.getText().trim(), password.getText(), fullName.getText().trim(),
                        role.getValue(), phone.getText().trim())) {
                    Ui.error("That username is already taken.");
                    return;
                }
            } else {
                if (existing.id() == current.id() && !role.getValue().equals(existing.role())) {
                    Ui.error("You cannot change your own role.");
                    return;
                }
                service.updateEmployee(existing.id(), fullName.getText().trim(), role.getValue(), phone.getText().trim());
            }
            load();
        });
    }
}
