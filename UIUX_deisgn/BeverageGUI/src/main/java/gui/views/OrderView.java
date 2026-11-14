package gui.views;

import gui.Fmt;
import gui.model.*;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Left: menu cards (search + category filter). Right: current order with running total. */
public class OrderView extends HBox {
    private final AppService service;
    private final ObservableList<OrderLine> lines = FXCollections.observableArrayList();
    private final ListView<OrderLine> list = new ListView<>(lines);
    private final Label total = new Label();
    private final FlowPane cards = new FlowPane(12, 12);
    private final TextField search = new TextField();
    private final ComboBox<Category> filter = new ComboBox<>();
    private static final Category ALL = new Category(0, "All");
    private double currentCardWidth = 138;
    private final Map<String, Image> imageCache = new HashMap<>();

    public OrderView(AppService service, User user, Runnable onDone) {
        super(20);
        this.service = service;

        // ----- Left: Menu -----
        search.setPromptText("Search drinks…");
        filter.getItems().add(ALL);
        filter.getItems().addAll(service.getCategories());
        filter.setValue(ALL);
        search.textProperty().addListener((o, a, b) -> refreshCards());
        filter.valueProperty().addListener((o, a, b) -> refreshCards());

        HBox tools = new HBox(10, search, filter);
        HBox.setHgrow(search, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(cards);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("clear-scroll");
        scroll.viewportBoundsProperty().addListener((obs, oldB, newB) -> {
            double available = newB.getWidth() - 4;
            if (available <= 0) return;
            double minColWidth = 130;
            int cols = Math.max(2, (int) ((available + 12) / (minColWidth + 12)));
            double newCardW = Math.floor((available - 12 * (cols - 1)) / cols);
            if (Math.abs(newCardW - currentCardWidth) >= 1) {
                currentCardWidth = newCardW;
                refreshCards();
            }
        });

        VBox left = new VBox(12, tools, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        // ----- Right: Current Order -----
        Label head = new Label("Current Order");
        head.getStyleClass().add("subtitle");

        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderLine l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label name = new Label(l.getProduct().name());
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2B2118;");
                    Label unitPrice = new Label(l.getSize() + ", " + l.getSugarLevel() + "% sugar, "
                            + l.getIceLevel() + "% ice | " + Fmt.money(l.getUnitPrice())
                            + " x " + l.getQuantity());
                    unitPrice.getStyleClass().add("muted");
                    VBox info = new VBox(2, name, unitPrice);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label sub = new Label(Fmt.money(l.getSubtotal()));
                    sub.setStyle("-fx-font-weight: bold; -fx-text-fill: #6F4E37;");

                    HBox row = new HBox(8, info, spacer, sub);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(4, 2, 4, 2));
                    setText(null);
                    setGraphic(row);
                }
            }
        });
        list.setPlaceholder(new Label("No drinks yet.\nClick a drink on the left to add."));

        Button plus = new Button("+");
        Button minus = new Button("–");
        Button remove = new Button("Remove");
        plus.setOnAction(e -> changeQty(1));
        minus.setOnAction(e -> changeQty(-1));
        remove.setOnAction(e -> {
            lines.remove(list.getSelectionModel().getSelectedItem());
            refreshTotal();
        });
        Region qtySpacer = new Region();
        HBox.setHgrow(qtySpacer, Priority.ALWAYS);
        HBox qtyBar = new HBox(8, plus, minus, qtySpacer, remove);

        total.getStyleClass().add("total");
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            lines.clear();
            refreshTotal();
        });
        clearBtn.setTooltip(new Tooltip("Esc"));

        ComboBox<String> payment = new ComboBox<>(FXCollections.observableArrayList("CASH", "QR"));
        payment.setValue("CASH");
        TextField customerPhone = new TextField();
        customerPhone.setPromptText("Customer phone (optional)");
        TextField usePoints = new TextField("0");
        usePoints.setPromptText("Points to redeem");
        Button addCustomer = new Button("+ Customer");
        addCustomer.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Add customer");
            nameDialog.setHeaderText("Enter customer name");
            nameDialog.showAndWait().ifPresent(name -> {
                if (!service.addCustomer(name.trim(), customerPhone.getText().trim())) {
                    new Alert(Alert.AlertType.ERROR, "Enter a new phone number and a customer name.").showAndWait();
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Customer added.").showAndWait();
                }
            });
        });
        HBox paymentBar = new HBox(8, payment, customerPhone, addCustomer);
        HBox.setHgrow(customerPhone, Priority.ALWAYS);

        Button confirm = new Button("Confirm Order");
        confirm.getStyleClass().add("primary");
        confirm.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(confirm, Priority.ALWAYS);
        confirm.setTooltip(new Tooltip("Ctrl+Enter"));

        confirm.setOnAction(e -> {
            if (lines.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Please add at least one drink.").showAndWait();
                return;
            }

            // An empty points field means the customer does not use any points.
            String pointsText = usePoints.getText().trim();
            int points = 0;

            if (!pointsText.isEmpty()) {
                try {
                    points = Integer.parseInt(pointsText);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Please enter a valid number of points.").showAndWait();
                    return;
                }
            }

            if (points < 0) {
                new Alert(Alert.AlertType.ERROR, "Points cannot be negative.").showAndWait();
                return;
            }

            if (payment.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Please select a payment method.").showAndWait();
                return;
            }

            try {
                service.createOrder(user.id(), new ArrayList<>(lines), payment.getValue(),
                        customerPhone.getText().trim(), points);
                new Alert(Alert.AlertType.INFORMATION, "Order created successfully!").showAndWait();
                onDone.run();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                String message = ex.getMessage();
                if (message == null || message.isBlank()) message = "Invalid order information.";
                new Alert(Alert.AlertType.ERROR, message).showAndWait();
            } catch (ArithmeticException ex) {
                new Alert(Alert.AlertType.ERROR, "The order total is too large.").showAndWait();
            }
        });
        HBox actions = new HBox(10, clearBtn, confirm);

        VBox right = new VBox(10, head, list, qtyBar, new Separator(), total,
                new Label("Payment and customer"), paymentBar, usePoints, actions);
        right.setPrefWidth(360);
        right.setPadding(new Insets(18));
        right.getStyleClass().add("card");
        VBox.setVgrow(list, Priority.ALWAYS);

        getChildren().addAll(left, right);
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                lines.clear();
                e.consume();
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER && e.isControlDown()) {
                confirm.fire();
                e.consume();
            }
        });

        refreshCards();
        refreshTotal();
        lines.addListener((javafx.collections.ListChangeListener<OrderLine>) c -> refreshTotal());
    }

    private void refreshCards() {
        cards.getChildren().clear();
        String q = search.getText() == null ? "" : search.getText().toLowerCase().trim();
        Category cat = filter.getValue();

        double cardW = currentCardWidth;
        double imgW = Math.max(90, cardW - 22);
        double imgH = Math.round(imgW * 0.65);
        double cardH = imgH + 78;

        for (Product p : service.getProducts()) {
            if (!p.available()) continue;
            if (!q.isEmpty() && !p.name().toLowerCase().contains(q)) continue;
            if (cat != null && cat.id() != 0 && p.categoryId() != cat.id()) continue;

            Node thumb = createProductImage(p, imgW, imgH);

            Label name = new Label(p.name());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            name.setWrapText(true);
            name.setMaxWidth(imgW);
            name.setAlignment(Pos.CENTER);
            name.setTextAlignment(TextAlignment.CENTER);

            Label price = new Label(Fmt.money(p.price()));
            price.setStyle("-fx-text-fill: #6F4E37; -fx-font-weight: 600;");

            VBox box = new VBox(6, thumb, name, price);
            box.setAlignment(Pos.CENTER);
            box.setMaxWidth(cardW - 10);

            Button b = new Button();
            b.setGraphic(box);
            b.setPrefSize(cardW, cardH);
            b.setMinWidth(cardW);
            b.setMaxWidth(cardW);
            b.getStyleClass().add("product-card");
            b.setDisable(!p.available());
            b.setOnAction(e -> addProduct(p));
            cards.getChildren().add(b);
        }
        if (cards.getChildren().isEmpty()) {
            Label none = new Label("No drinks match your search.");
            none.getStyleClass().add("muted");
            cards.getChildren().add(none);
        }
    }

    private Node createProductImage(Product p, double targetW, double targetH) {
        String path = p.imagePath();
        if (path != null && !path.isBlank()) {
            Image img = loadImageSmart(path);
            if (img != null && !img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                double targetRatio = targetW / targetH;
                double imgW = img.getWidth();
                double imgH = img.getHeight();
                double imgRatio = imgW / imgH;

                double cropW, cropH, cropX, cropY;
                if (imgRatio > targetRatio) {
                    cropH = imgH;
                    cropW = imgH * targetRatio;
                    cropX = (imgW - cropW) / 2.0;
                    cropY = 0;
                } else {
                    cropW = imgW;
                    cropH = imgW / targetRatio;
                    cropX = 0;
                    cropY = (imgH - cropH) / 2.0;
                }

                ImageView iv = new ImageView(img);
                iv.setViewport(new Rectangle2D(cropX, cropY, cropW, cropH));
                iv.setFitWidth(targetW);
                iv.setFitHeight(targetH);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);

                Rectangle clip = new Rectangle(targetW, targetH);
                clip.setArcWidth(14);
                clip.setArcHeight(14);
                iv.setClip(clip);
                return iv;
            } else {
                System.out.println("Could not load image for " + p.name() + " -> " + path);
            }
        }

        Label placeholder = new Label("☕");
        placeholder.setPrefSize(targetW, targetH);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-background-color: #F4EFEA; -fx-background-radius: 10; -fx-font-size: 24px;");
        return placeholder;
    }

    private Image loadImageSmart(String rawPath) {
        if (imageCache.containsKey(rawPath)) return imageCache.get(rawPath);
        Image found = findImage(rawPath);
        imageCache.put(rawPath, found);
        return found;
    }

    private Image findImage(String rawPath) {
        String base = rawPath.replaceFirst("\\.[^.]+$", "");
        String[] prefixes = rawPath.startsWith("/") ? new String[]{""} : new String[]{"/images/", "/gui/images/"};
        String[] exts = {"", ".png", ".jpg", ".jpeg"};

        for (String prefix : prefixes) {
            for (String ext : exts) {
                String candidate = ext.isEmpty() ? prefix + rawPath : prefix + base + ext;
                if (!candidate.startsWith("/")) candidate = "/" + candidate;

                // 1. Check classpath (target/classes)
                for (String cp : new String[]{candidate, "/gui" + candidate}) {
                    InputStream is = getClass().getResourceAsStream(cp);
                    if (is != null) {
                        try (InputStream stream = is) {
                            Image img = new Image(stream);
                            if (!img.isError()) return img;
                        } catch (java.io.IOException ex) {
                            return null;
                        }
                    }
                }

                // 2. Check source folder directly on disk
                for (String root : new String[]{"src/main/resources", "BeverageGUI/src/main/resources"}) {
                    for (String sub : new String[]{candidate, "/gui" + candidate}) {
                        File f = new File(root + sub);
                        if (f.exists()) {
                            Image img = new Image(f.toURI().toString());
                            if (!img.isError()) return img;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void addProduct(Product p) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Customize " + p.name());
        ComboBox<String> size = new ComboBox<>(FXCollections.observableArrayList("S", "M", "L"));
        size.setValue("S");
        ComboBox<Integer> sugar = new ComboBox<>(FXCollections.observableArrayList(0, 30, 50, 70, 100));
        sugar.setValue(100);
        ComboBox<Integer> ice = new ComboBox<>(FXCollections.observableArrayList(0, 30, 50, 70, 100));
        ice.setValue(100);
        VBox toppingsBox = new VBox(5);
        List<CheckBox> checks = new ArrayList<>();
        Map<CheckBox, Topping> choices = new HashMap<>();
        for (Topping t : service.getToppings()) {
            if (!t.active()) continue;
            CheckBox cb = new CheckBox(t.name() + " (+" + Fmt.money(t.price()) + ")");
            checks.add(cb);
            choices.put(cb, t);
            toppingsBox.getChildren().add(cb);
        }
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10); form.setPadding(new Insets(14));
        form.addRow(0, new Label("Size"), size);
        form.addRow(1, new Label("Sugar %"), sugar);
        form.addRow(2, new Label("Ice %"), ice);
        VBox content = new VBox(10, form, new Label("Toppings"), toppingsBox);
        dialog.getDialogPane().setContent(new ScrollPane(content));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            List<OrderDetailTopping> selected = new ArrayList<>();
            for (CheckBox cb : checks) {
                if (cb.isSelected()) {
                    Topping t = choices.get(cb);
                    selected.add(new OrderDetailTopping(t.id(), t.name(), t.price()));
                }
            }
            OrderLine added = new OrderLine(p, 1, size.getValue(), sugar.getValue(), ice.getValue(), selected);
            for (OrderLine line : lines) {
                if (line.sameOptions(added)) {
                    line.setQuantity(line.getQuantity() + 1);
                    list.refresh();
                    refreshTotal();
                    return;
                }
            }
            lines.add(added);
            refreshTotal();
        });
    }

    private void changeQty(int delta) {
        OrderLine l = list.getSelectionModel().getSelectedItem();
        if (l == null) return;
        int q = l.getQuantity() + delta;
        if (q <= 0) lines.remove(l);
        else l.setQuantity(q);
        list.refresh();
        refreshTotal();
    }

    private void refreshTotal() {
        long sum = lines.stream().mapToLong(OrderLine::getSubtotal).sum();
        total.setText("Total: " + Fmt.money(sum));
    }
}
