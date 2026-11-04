package gui.views;

import gui.model.TableInfo;
import gui.service.AppService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

/**
 * Grid of tables. Green = Available, red = Occupied.
 * Click a table to take an order. Right-click a table to change its status (Staff and Manager).
 */
public class TablesView extends VBox {
    private final AppService service;
    private final Consumer<TableInfo> onOpen;
    private final FlowPane grid = new FlowPane(14, 14);

    public TablesView(AppService service, Consumer<TableInfo> onOpen) {
        super(14);
        this.service = service;
        this.onOpen = onOpen;
        Label legend = new Label("Green = Available     Red = Occupied     •     Click a table to take an order     •     Right-click to change its status");
        legend.getStyleClass().add("muted");
        setPadding(new Insets(4));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(grid, legend);
        load();
    }

    private void load() {
        grid.getChildren().clear();
        for (TableInfo t : service.getTables()) {
            Button b = new Button(t.getName() + "\n" + t.getStatus());
            b.setPrefSize(140, 100);
            b.getStyleClass().addAll("table-tile", t.isAvailable() ? "available" : "occupied");
            b.setOnAction(e -> onOpen.accept(t));

            MenuItem free = new MenuItem("Set Available");
            free.setDisable(t.isAvailable());
            free.setOnAction(e -> { service.setTableStatus(t.getId(), "Available"); load(); });
            MenuItem occ = new MenuItem("Set Occupied");
            occ.setDisable(!t.isAvailable());
            occ.setOnAction(e -> { service.setTableStatus(t.getId(), "Occupied"); load(); });
            b.setContextMenu(new ContextMenu(free, occ));

            grid.getChildren().add(b);
        }
        if (grid.getChildren().isEmpty()) {
            Label none = new Label("No tables yet. A manager can add tables in Manage Tables.");
            none.getStyleClass().add("muted");
            grid.getChildren().add(none);
        }
    }
}
