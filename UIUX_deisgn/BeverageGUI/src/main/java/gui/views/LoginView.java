package gui.views;

import gui.model.User;
import gui.service.AppService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public class LoginView {
    private final VBox root = new VBox(14);

    public LoginView(AppService service, Consumer<User> onSuccess) {
        Label title = new Label("☕ Beverage Management");
        title.getStyleClass().add("title");
        Label sub = new Label("Please log in to continue");
        sub.getStyleClass().add("muted");

        TextField user = new TextField();
        user.setPromptText("Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        Label error = new Label();
        error.getStyleClass().add("error");

        Button login = new Button("Log in");
        login.getStyleClass().add("primary");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);
        login.setOnAction(e -> {
            User u = service.login(user.getText().trim(), pass.getText());
            if (u == null) error.setText("Wrong username or password, or the account is inactive.");
            else onSuccess.accept(u);
        });

        VBox card = new VBox(12, title, sub, user, pass, error, login);
        card.getStyleClass().add("card");
        card.setMaxWidth(320);
        card.setPadding(new Insets(28));

        root.getChildren().add(card);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("app-bg");
    }

    public Parent getRoot() { return root; }
}
