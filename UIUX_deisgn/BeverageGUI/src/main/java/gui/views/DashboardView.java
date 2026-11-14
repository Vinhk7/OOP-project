package gui.views;

import gui.Fmt;
import gui.model.Order;
import gui.model.OrderLine;
import gui.service.AppService;
import javafx.collections.FXCollections;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class DashboardView extends VBox {
    public DashboardView(AppService service) {
        super(16);
        List<Order> orders = service.getOrders();
        long today = 0, month = 0;
        Map<String, Integer> sales = new HashMap<>();
        Map<String, Long> revenue = new TreeMap<>();
        LocalDate date = LocalDate.now();

        // Only completed orders count as revenue.
        for (Order o : orders) {
            if (!"COMPLETED".equals(o.status())) continue;
            LocalDate orderDate = o.time().toLocalDate();
            if (orderDate.equals(date)) today += o.total();
            if (YearMonth.from(orderDate).equals(YearMonth.from(date))) month += o.total();
            revenue.merge(orderDate.toString(), o.total(), Long::sum);
            for (OrderLine line : service.getOrderLines(o.id())) {
                sales.merge(line.getProduct().name(), line.getQuantity(), Integer::sum);
            }
        }
        Label summary = new Label("Today: " + Fmt.money(today) + "     This month: " + Fmt.money(month));
        summary.getStyleClass().add("subtitle");
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Completed order revenue by day");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> entry : revenue.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chart.getData().add(series);
        List<Map.Entry<String, Integer>> top = new ArrayList<>(sales.entrySet());
        top.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        ListView<String> best = new ListView<>();
        for (int i = 0; i < Math.min(5, top.size()); i++) {
            best.getItems().add(top.get(i).getKey() + " - " + top.get(i).getValue() + " drinks");
        }
        best.setPlaceholder(new Label("Complete an order to see best-selling drinks."));
        best.setPrefHeight(160);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getChildren().addAll(summary, chart, new Label("Top-selling drinks"), best);
    }
}
