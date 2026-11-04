package gui.views;

import gui.Ui;
import gui.model.TableInfo;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Manager: add / delete tables. (Staff change status from the Tables screen.) */
public class ManageTablesView extends VBox {
    private final AppService service;
    private final TableView<TableInfo> table = new TableView<>();

    @SuppressWarnings("unchecked")
    public ManageTablesView(AppService service) {
        super(12);
        this.service = service;

        Button add = new Button("+ Add table");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog("Table " + (service.getTables().size() + 1));
            d.setTitle("Add table");
            d.setHeaderText(null);
            d.setContentText("Name:");
            d.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(n -> { service.addTable(n); load(); });
        });
        Button free = new Button("Set Available");
        free.setOnAction(e -> setStatus("Available"));
        Button occ = new Button("Set Occupied");
        occ.setOnAction(e -> setStatus("Occupied"));
        Button del = new Button("Delete");
        del.getStyleClass().add("danger");
        del.setOnAction(e -> {
            TableInfo t = table.getSelectionModel().getSelectedItem();
            if (t == null) { Ui.info("Please select a table first."); return; }
            if (!Ui.confirm("Delete " + t.getName() + "?")) return;
            if (!service.deleteTable(t.getId())) Ui.error("An occupied table cannot be deleted.");
            load();
        });

        table.getColumns().addAll(
                Ui.<TableInfo>col("ID", t -> String.valueOf(t.getId())),
                Ui.<TableInfo>col("Name", TableInfo::getName),
                Ui.<TableInfo>col("Status", TableInfo::getStatus));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No tables yet. Click \"+ Add table\" to create one."));
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(new HBox(10, add, free, occ, del), table);
        load();
    }

    private void setStatus(String s) {
        TableInfo t = table.getSelectionModel().getSelectedItem();
        if (t == null) { Ui.info("Please select a table first."); return; }
        service.setTableStatus(t.getId(), s);
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(service.getTables())); }
}
