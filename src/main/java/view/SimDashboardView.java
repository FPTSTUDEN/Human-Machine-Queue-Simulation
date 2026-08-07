package view;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SimDashboardView {

    @FXML
    private Pane simulationPane;

    private Circle customer;

    @FXML
    public void initialize() {
        drawStation(60, 100, "Queue");
        drawStation(250, 50, "Human");
        drawStation(250, 170, "Machine");
        drawStation(500, 100, "Exit");
    }

    private void drawStation(double x, double y, String name) {
        Rectangle box = new Rectangle(x, y, 100, 50);
        box.setFill(Color.LIGHTGRAY);
        box.setStroke(Color.BLACK);

        Text text = new Text(x + 25, y + 30, name);

        simulationPane.getChildren().addAll(box, text);
    }

    @FXML
    private void startSimulation() {

        if (customer != null) {
            simulationPane.getChildren().remove(customer);
        }

        customer = new Circle(10, Color.DODGERBLUE);
        customer.setCenterX(20);
        customer.setCenterY(125);

        simulationPane.getChildren().add(customer);

        TranslateTransition t1 = new TranslateTransition(Duration.seconds(1), customer);
        t1.setToX(90);
        t1.setToY(0);

        TranslateTransition t2 = new TranslateTransition(Duration.seconds(1), customer);
        t2.setToX(280);
        t2.setToY(-50);

        TranslateTransition t3 = new TranslateTransition(Duration.seconds(1), customer);
        t3.setToX(520);
        t3.setToY(0);

        SequentialTransition animation = new SequentialTransition(t1, t2, t3);
        animation.play();
    }

    @FXML
    private void resetSimulation() {

        simulationPane.getChildren().clear();

        customer = null;

        initialize();
    }
}