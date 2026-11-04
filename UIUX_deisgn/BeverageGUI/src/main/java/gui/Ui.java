package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import java.util.function.Function;

/** Small helpers shared by all screens. */
public final class Ui {
    private Ui() { }

    public static <T> TableColumn<T, String> col(String title, Function<T, String> getter) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(getter.apply(cd.getValue())));
        return c;
    }
    public static boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg).showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
    public static void info(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    public static void error(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
