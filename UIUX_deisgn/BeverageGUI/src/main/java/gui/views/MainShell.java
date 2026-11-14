package gui.views;

import gui.model.User;
import gui.service.AppService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

/** Sidebar + content area. Manager-only items are not even created for Staff. */
public class MainShell {
    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private final Label pageTitle = new Label();
    private final VBox sidebar = new VBox(6);
    private final List<Runnable> navActions = new ArrayList<>();
    private final AppService service;
    private final User user;
    private Button activeBtn;

    public MainShell(AppService service, User user, Runnable onLogout) {
        this.service = service;
        this.user = user;

        Label brand = new Label("☕ Coffee");
        brand.getStyleClass().add("brand");
        sidebar.getChildren().add(brand);

        Button orderBtn = addNav("New Order", this::showOrder);
        addNav("Orders", () -> show("Orders", new OrdersView(service)));
        addNav("Menu", () -> show("Menu", new ProductsView(service, false)));

        if (user.isManager()) {
            Separator sep = new Separator();
            Label mgr = new Label("ADMIN");
            mgr.getStyleClass().add("section-label");
            sidebar.getChildren().addAll(sep, mgr);
            addNav("Products", () -> show("Products", new ProductsView(service, true)));
            addNav("Categories", () -> show("Categories", new CategoriesView(service)));
            addNav("Employees", () -> show("Employees", new EmployeesView(service, user)));
            addNav("Customers", () -> show("Customers", new CustomersView(service)));
            addNav("Toppings", () -> show("Toppings", new ToppingsView(service)));
            addNav("Dashboard", () -> show("Dashboard", new DashboardView(service)));
        }

        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(210);
        sidebar.getStyleClass().add("sidebar");

        pageTitle.getStyleClass().add("title");
        Label who = new Label(user.fullName() + " (" + user.role() + ")");
        who.getStyleClass().add("muted");
        Button logout = new Button("Log out");
        logout.setOnAction(e -> onLogout.run());
        logout.setTooltip(new Tooltip("Ctrl+L"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(12, pageTitle, spacer, who, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14, 24, 14, 24));
        top.getStyleClass().add("topbar");

        content.setPadding(new Insets(20));
        root.setLeft(sidebar);
        root.setTop(top);
        root.setCenter(content);
        root.getStyleClass().add("app-bg");

        // Keyboard shortcuts: Ctrl+1..9 = sidebar items, Ctrl+L = log out
        root.sceneProperty().addListener((o, old, sc) -> {
            if (sc == null) return;
            for (int i = 0; i < Math.min(navActions.size(), 9); i++) {
                sc.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.valueOf("DIGIT" + (i + 1)), KeyCombination.CONTROL_DOWN),
                        navActions.get(i)
                );
            }
            sc.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), onLogout);
        });

        setActiveNav(orderBtn);
        showOrder(); // Màn hình mặc định sau khi đăng nhập
    }

    private Button addNav(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("nav");
        Runnable wrappedAction = () -> {
            setActiveNav(b);
            action.run();
        };
        b.setOnAction(e -> wrappedAction.run());
        navActions.add(wrappedAction);
        if (navActions.size() <= 9) b.setTooltip(new Tooltip("Ctrl+" + navActions.size()));
        sidebar.getChildren().add(b);
        return b;
    }

    private void setActiveNav(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("active");
        activeBtn = btn;
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    private void show(String title, Node view) {
        pageTitle.setText(title);
        content.getChildren().setAll(view);
    }

    private void showOrder() {
        show("New Order", new OrderView(service, user, this::showOrder));
    }

    public Parent getRoot() { return root; }
}
