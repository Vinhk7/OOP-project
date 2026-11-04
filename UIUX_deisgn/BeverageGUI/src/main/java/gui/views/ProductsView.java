package gui.views;

import gui.Fmt;
import gui.model.*;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Product table with search. editable=true (Manager) also shows Add / Delete. */
public class ProductsView extends VBox {
    private final AppService service;
    private final TableView<Product> table = new TableView<>();
    private FilteredList<Product> filtered;

    public ProductsView(AppService service, boolean editable) {
        super(12);
        this.service = service;

        TextField search = new TextField();
        search.setPromptText("Search products…");
        HBox bar = new HBox(10, search);
        HBox.setHgrow(search, Priority.ALWAYS);

        TableColumn<Product, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name()));
        TableColumn<Product, String> cat = new TableColumn<>("Category");
        cat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(categoryName(c.getValue().categoryId())));
        TableColumn<Product, String> price = new TableColumn<>("Price");
        price.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Fmt.money(c.getValue().price())));
        TableColumn<Product, String> avail = new TableColumn<>("Available");
        avail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().available() ? "Yes" : "No"));
        table.getColumns().addAll(name, cat, price, avail);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No products found."));
        VBox.setVgrow(table, Priority.ALWAYS);
        load();
        search.textProperty().addListener((o, a, b) -> {
            String q = b.toLowerCase().trim();
            filtered.setPredicate(p -> q.isEmpty() || p.name().toLowerCase().contains(q));
        });

        if (editable) {
            Button add = new Button("+ Add product");
            add.getStyleClass().add("primary");
            add.setOnAction(e -> openDialog(null));
            Button edit = new Button("Edit selected");
            edit.setOnAction(e -> editSelected());
            table.setRowFactory(tv -> {
                TableRow<Product> row = new TableRow<>();
                row.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2 && !row.isEmpty()) openDialog(row.getItem()); });
                return row;
            });
            Button del = new Button("Delete selected");
            del.getStyleClass().add("danger");
            del.setOnAction(e -> {
                Product p = table.getSelectionModel().getSelectedItem();
                if (p == null) return;
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + p.name() + "\"?");
                a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    service.deleteProduct(p.id());
                    load();
                });
            });
            bar.getChildren().addAll(add, edit, del);
        }
        getChildren().addAll(bar, table);
    }

    private void load() {
        filtered = new FilteredList<>(FXCollections.observableArrayList(service.getProducts()));
        table.setItems(filtered);
    }

    private String categoryName(int id) {
        return service.getCategories().stream().filter(c -> c.id() == id)
                .map(Category::name).findFirst().orElse("?");
    }

    private void editSelected() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) { new Alert(Alert.AlertType.INFORMATION, "Please select a product first.").showAndWait(); return; }
        openDialog(p);
    }

    /** One dialog for both Add (existing == null) and Edit. */
    private void openDialog(Product existing) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(existing == null ? "Add product" : "Edit product");
        TextField name = new TextField(existing == null ? "" : existing.name());
        ComboBox<Category> cat = new ComboBox<>();
        cat.getItems().addAll(service.getCategories());
        if (existing == null) cat.getSelectionModel().selectFirst();
        else cat.getItems().stream().filter(c -> c.id() == existing.categoryId()).findFirst().ifPresent(cat::setValue);
        TextField price = new TextField(existing == null ? "" : String.valueOf(existing.price()));
        price.setPromptText("e.g. 35000");
        CheckBox avail = new CheckBox("Available");
        avail.setSelected(existing == null || existing.available());

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        g.addRow(0, new Label("Name"), name);
        g.addRow(1, new Label("Category"), cat);
        g.addRow(2, new Label("Price (₫)"), price);
        g.add(avail, 1, 3);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                long p = Long.parseLong(price.getText().trim());
                if (name.getText().isBlank() || p <= 0 || cat.getValue() == null) throw new NumberFormatException();
                if (existing == null)
                    service.addProduct(name.getText().trim(), cat.getValue().id(), p, avail.isSelected());
                else
                    service.updateProduct(existing.id(), name.getText().trim(), cat.getValue().id(), p, avail.isSelected());
                load();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Please enter a name, a category and a valid price.").showAndWait();
            }
        });
    }
}
