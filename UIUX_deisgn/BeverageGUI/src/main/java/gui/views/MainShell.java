package gui.views;

import gui.model.TableInfo;
import gui.model.User;
import gui.service.AppService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.*;

/** Sidebar + content area. Manager-only items are not even created for Staff. */
public class MainShell {
    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private final Label pageTitle = new Label();
    private final VBox sidebar = new VBox(6);
    private final List<Runnable> navActions = new ArrayList<>();
    private final AppService service;
    private final User user;

    public MainShell(AppService service, User user, Runnable onLogout) {
        this.service = service;
        this.user = user;

        Label brand = new Label("☕ Cafe");
        brand.getStyleClass().add("brand");
        sidebar.getChildren().add(brand);

        addNav("Tables", this::showTables);
        addNav("Orders", () -> show("Orders", new OrdersView(service)));
        addNav("Menu", () -> show("Menu", new ProductsView(service, false)));

        if (user.isManager()) {
            Separator sep = new Separator();
            Label mgr = new Label("MANAGER");
            mgr.getStyleClass().add("section-label");
            sidebar.getChildren().addAll(sep, mgr);
//            addNav("Products", () -> show("Products", new ProductsView(service, true)));
            addNav("Categories", () -> show("Categories", new CategoriesView(service)));
            addNav("Employees", () -> show("Employees", new EmployeesView(service, user)));
            addNav("Manage Tables", () -> show("Manage Tables", new ManageTablesView(service)));
        }
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");

        pageTitle.getStyleClass().add("title");
        Label who = new Label(user.fullName() + " (" + user.role() + ")");
        Button logout = new Button("Log out");
        logout.setOnAction(e -> onLogout.run());
        logout.setTooltip(new Tooltip("Ctrl+L"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(12, pageTitle, spacer, who, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14, 20, 14, 20));
        top.getStyleClass().add("topbar");

        content.setPadding(new Insets(20));
        root.setLeft(sidebar);
        root.setTop(top);
        root.setCenter(content);
        root.getStyleClass().add("app-bg");

        // keyboard shortcuts: Ctrl+1..9 = sidebar items in order, Ctrl+L = log out
        root.sceneProperty().addListener((o, old, sc) -> {
            if (sc == null) return;
            for (int i = 0; i < Math.min(navActions.size(), 9); i++) {
                sc.getAccelerators().put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + (i + 1)), KeyCombination.CONTROL_DOWN), navActions.get(i));
            }
            sc.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), onLogout);
        });

        showTables(); // home screen
    }

    private void addNav(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("nav");
        b.setOnAction(e -> action.run());
        navActions.add(action);
        if (navActions.size() <= 9) b.setTooltip(new Tooltip("Ctrl+" + navActions.size()));
        sidebar.getChildren().add(b);
    }

    private void show(String title, Node view) {
        pageTitle.setText(title);
        content.getChildren().setAll(view);
    }

    private void showTables() {
        show("Tables", new TablesView(service, this::showOrder));
    }

    private void showOrder(TableInfo table) {
        show("Order – " + table.getName(), new OrderView(service, user, table, this::showTables));
    }

    public Parent getRoot() { return root; }
}
