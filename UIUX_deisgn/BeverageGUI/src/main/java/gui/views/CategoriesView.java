package gui.views;

import gui.Ui;
import gui.model.Category;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Optional;

public class CategoriesView extends VBox {
    private final AppService service;
    private final TableView<Category> table = new TableView<>();

    @SuppressWarnings("unchecked")
    public CategoriesView(AppService service) {
        super(12);
        this.service = service;

        Button add = new Button("+ Add category");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> ask("Add category", "").ifPresent(n -> { service.addCategory(n); load(); }));
        Button rename = new Button("Rename");
        rename.setOnAction(e -> {
            Category c = table.getSelectionModel().getSelectedItem();
            if (c == null) { Ui.info("Please select a category first."); return; }
            ask("Rename category", c.name()).ifPresent(n -> { service.renameCategory(c.id(), n); load(); });
        });
        Button del = new Button("Delete");
        del.getStyleClass().add("danger");
        del.setOnAction(e -> {
            Category c = table.getSelectionModel().getSelectedItem();
            if (c == null) { Ui.info("Please select a category first."); return; }
            if (!Ui.confirm("Delete category \"" + c.name() + "\"?")) return;
            if (!service.deleteCategory(c.id())) Ui.error("This category still has products. Move or delete them first.");
            load();
        });

        table.getColumns().addAll(
                Ui.<Category>col("ID", c -> String.valueOf(c.id())),
                Ui.<Category>col("Name", Category::name));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No categories yet. Click \"+ Add category\" to create one."));
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(new HBox(10, add, rename, del), table);
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(service.getCategories())); }

    private Optional<String> ask(String title, String initial) {
        TextInputDialog d = new TextInputDialog(initial);
        d.setTitle(title);
        d.setHeaderText(null);
        d.setContentText("Name:");
        return d.showAndWait().map(String::trim).filter(s -> !s.isEmpty());
    }
}
