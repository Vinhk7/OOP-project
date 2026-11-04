package gui;

import gui.model.User;
import gui.service.AppService;
import gui.service.MockAppService;
import gui.views.LoginView;
import gui.views.MainShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    // To connect the real backend, replace MockAppService with the controller-based implementation.
    private final AppService service = new MockAppService();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Beverage Management System");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        setRoot(new LoginView(service, this::showMain).getRoot(), 420, 480);
    }

    private void showMain(User user) {
        setRoot(new MainShell(service, user, this::showLogin).getRoot(), 1100, 700);
    }

    private void setRoot(javafx.scene.Parent root, double w, double h) {
        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(getClass().getResource("/gui/style.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) { launch(args); }
}
