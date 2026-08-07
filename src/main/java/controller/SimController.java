package controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import model.SimulationConfig;
import model.SimulationResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimController {

    @FXML
    private TextField humanCashierField;

    @FXML
    private TextField selfCheckoutField;

    @FXML
    private TextField arrivalRateField;

    @FXML
    private TextField maximumWaitingTimeField;

    @FXML
    private TextField simulationDurationField;

    @FXML
    private Button startButton;

    @FXML
    private Pane simulationPane;

    @FXML
    private HBox humanCashierContainer;

    @FXML
    private HBox machineCashierContainer;

    @FXML
    private TextArea resultArea;


    private final Random random = new Random();

    private final List<VBox> humanCashierViews = new ArrayList<>();
    private final List<VBox> machineCashierViews = new ArrayList<>();

    private final Map<VBox, Integer> busyCounters = new HashMap<>();

    private Timeline customerGenerator;


    // =========================================================
    // START SIMULATION
    // =========================================================

    @FXML
    private void startSimulation() {

        try {

            int humanCashiers =
                    Integer.parseInt(humanCashierField.getText().trim());

            int selfCheckouts =
                    Integer.parseInt(selfCheckoutField.getText().trim());

            double arrivalRate =
                    Double.parseDouble(arrivalRateField.getText().trim());

            double maximumWaitingTime =
                    Double.parseDouble(maximumWaitingTimeField.getText().trim());

            double simulationDuration =
                    Double.parseDouble(simulationDurationField.getText().trim());


            if (humanCashiers < 0 ||
                    selfCheckouts < 0 ||
                    arrivalRate <= 0 ||
                    maximumWaitingTime < 0 ||
                    simulationDuration <= 0) {

                resultArea.setText(
                        "Invalid input.\n" +
                                "Please enter positive values."
                );

                return;
            }


            if (humanCashiers == 0 && selfCheckouts == 0) {

                resultArea.setText(
                        "Invalid configuration.\n" +
                                "At least one checkout station is required."
                );

                return;
            }


            stopCurrentAnimation();

            startButton.setDisable(true);


            // =================================================
            // CREATE LIVE UI
            // =================================================

            createHumanCashiers(humanCashiers);
            createMachineCashiers(selfCheckouts);
            resetSimulationPane();


            // =================================================
            // CREATE SIMULATION CONFIG
            // =================================================

            double[] humanServiceTimes = {
                    2.0,
                    3.0,
                    4.0,
                    5.0
            };


            double[] machineServiceTimes = {
                    1.0,
                    1.5,
                    2.0,
                    2.5,
                    3.0
            };


            SimulationConfig config =
                    new SimulationConfig(
                            humanCashiers,
                            selfCheckouts,
                            arrivalRate / 60.0,
                            humanServiceTimes,
                            machineServiceTimes,
                            simulationDuration,
                            maximumWaitingTime
                    );


            // =================================================
            // RUN REAL SIMULATION MODEL
            // =================================================

            Controller controller =
                    new Controller(config);


            SimulationResults results =
                    controller.runSimulation();


            // =================================================
            // SHOW REAL RESULTS
            // =================================================

            resultArea.setText(
                    "=== SIMULATION RESULTS ===\n\n" +
                            results
            );


            // =================================================
            // START VISUAL ANIMATION
            // =================================================

            startCustomerAnimation(
                    humanCashiers,
                    selfCheckouts,
                    arrivalRate,
                    simulationDuration
            );


        } catch (NumberFormatException e) {

            startButton.setDisable(false);

            resultArea.setText(
                    "Invalid input.\n" +
                            "Please enter numeric values in all fields."
            );


        } catch (Exception e) {

            startButton.setDisable(false);

            resultArea.setText(
                    "Simulation error:\n" +
                            e.getMessage()
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // RESET LIVE SIMULATION AREA
    // =========================================================

    private void resetSimulationPane() {

        simulationPane.getChildren().clear();


        Label entranceLabel =
                new Label("CUSTOMER ARRIVAL");

        entranceLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #555555;"
        );

        entranceLabel.layoutXProperty().bind(
                simulationPane.widthProperty()
                        .divide(2)
                        .subtract(65)
        );

        entranceLabel.setLayoutY(15);


        Label humanArea =
                new Label("Human Checkout");

        humanArea.setStyle(
                "-fx-text-fill: #2196F3;" +
                        "-fx-font-weight: bold;"
        );

        humanArea.layoutXProperty().bind(
                simulationPane.widthProperty()
                        .multiply(0.20)
                        .subtract(45)
        );

        humanArea.layoutYProperty().bind(
                simulationPane.heightProperty()
                        .multiply(0.52)
        );


        Label machineArea =
                new Label("Self-Checkout");

        machineArea.setStyle(
                "-fx-text-fill: #9C27B0;" +
                        "-fx-font-weight: bold;"
        );

        machineArea.layoutXProperty().bind(
                simulationPane.widthProperty()
                        .multiply(0.72)
                        .subtract(45)
        );

        machineArea.layoutYProperty().bind(
                simulationPane.heightProperty()
                        .multiply(0.52)
        );


        Label exitLabel =
                new Label("EXIT");

        exitLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #555555;"
        );

        exitLabel.layoutXProperty().bind(
                simulationPane.widthProperty()
                        .divide(2)
                        .subtract(15)
        );

        exitLabel.layoutYProperty().bind(
                simulationPane.heightProperty()
                        .subtract(30)
        );


        simulationPane.getChildren().addAll(
                entranceLabel,
                humanArea,
                machineArea,
                exitLabel
        );
    }


    // =========================================================
    // CUSTOMER GENERATOR
    // =========================================================

    private void startCustomerAnimation(
            int humanCashiers,
            int selfCheckouts,
            double arrivalRate,
            double simulationDuration
    ) {

        double intervalSeconds =
                60.0 / arrivalRate;


        // The real rate could be too slow/fast for a classroom demo.
        // Keep the visual animation observable.
        intervalSeconds =
                Math.max(
                        0.7,
                        Math.min(intervalSeconds, 3.0)
                );


        int estimatedCustomers =
                (int) Math.round(
                        arrivalRate *
                                simulationDuration /
                                60.0
                );


        int visualCustomerCount =
                Math.max(
                        8,
                        Math.min(
                                estimatedCustomers,
                                30
                        )
                );


        customerGenerator =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(intervalSeconds),
                                event ->
                                        createAnimatedCustomer(
                                                humanCashiers,
                                                selfCheckouts
                                        )
                        )
                );


        customerGenerator.setCycleCount(
                visualCustomerCount
        );


        customerGenerator.setOnFinished(
                event -> startButton.setDisable(false)
        );


        // Show first customer immediately
        createAnimatedCustomer(
                humanCashiers,
                selfCheckouts
        );


        customerGenerator.play();
    }


    // =========================================================
    // CREATE ONE CUSTOMER
    // =========================================================

    private void createAnimatedCustomer(
            int humanCashiers,
            int selfCheckouts
    ) {

        double width =
                simulationPane.getWidth();

        double height =
                simulationPane.getHeight();


        if (width <= 0) {
            width = 600;
        }

        if (height <= 0) {
            height = 350;
        }


        Circle customer =
                new Circle(9);


        customer.setFill(
                Color.web("#4CAF50")
        );

        customer.setStroke(
                Color.web("#2E7D32")
        );

        customer.setStrokeWidth(2);


        double startX =
                width / 2.0;

        double startY =
                50;


        customer.setLayoutX(startX);
        customer.setLayoutY(startY);


        simulationPane
                .getChildren()
                .add(customer);


        // =====================================================
        // SELECT HUMAN OR MACHINE
        // =====================================================

        boolean useHuman;


        if (humanCashiers == 0) {

            useHuman = false;

        } else if (selfCheckouts == 0) {

            useHuman = true;

        } else {

            /*
             * Customers randomly choose between
             * human and machine checkout.
             *
             * This selection is only for visualisation.
             */
            useHuman = random.nextBoolean();
        }


        VBox stationView;


        double targetX;


        if (useHuman) {

            stationView =
                    humanCashierViews.get(
                            random.nextInt(
                                    humanCashierViews.size()
                            )
                    );


            targetX =
                    width * 0.25;

        } else {

            stationView =
                    machineCashierViews.get(
                            random.nextInt(
                                    machineCashierViews.size()
                            )
                    );


            targetX =
                    width * 0.75;
        }


        double targetY =
                height * 0.58;


        // =====================================================
        // MOVE CUSTOMER TO CHECKOUT
        // =====================================================

        TranslateTransition moveToCheckout =
                new TranslateTransition(
                        Duration.seconds(1.4),
                        customer
                );


        moveToCheckout.setToX(
                targetX - startX
        );


        moveToCheckout.setToY(
                targetY - startY
        );


        moveToCheckout.setOnFinished(
                event ->
                        setStationBusy(
                                stationView,
                                true
                        )
        );


        // =====================================================
        // SERVICE TIME VISUALISATION
        // =====================================================

        double visualServiceTime =
                0.8 +
                        random.nextDouble() * 1.5;


        PauseTransition servicePause =
                new PauseTransition(
                        Duration.seconds(
                                visualServiceTime
                        )
                );


        // =====================================================
        // MOVE CUSTOMER TO EXIT
        // =====================================================

        TranslateTransition moveToExit =
                new TranslateTransition(
                        Duration.seconds(1.2),
                        customer
                );


        moveToExit.setToX(
                width / 2.0 - startX
        );


        moveToExit.setToY(
                height - 70 - startY
        );


        moveToExit.setOnFinished(
                event -> {

                    setStationBusy(
                            stationView,
                            false
                    );


                    simulationPane
                            .getChildren()
                            .remove(customer);
                }
        );


        SequentialTransition customerJourney =
                new SequentialTransition(
                        moveToCheckout,
                        servicePause,
                        moveToExit
                );


        customerJourney.play();
    }


    // =========================================================
    // STATION STATUS
    // =========================================================

    private void setStationBusy(
            VBox station,
            boolean busy
    ) {

        if (station == null ||
                station.getChildren().size() < 3) {

            return;
        }


        int currentBusyCount =
                busyCounters.getOrDefault(
                        station,
                        0
                );


        if (busy) {

            currentBusyCount++;

        } else {

            currentBusyCount =
                    Math.max(
                            0,
                            currentBusyCount - 1
                    );
        }


        busyCounters.put(
                station,
                currentBusyCount
        );


        Label status =
                (Label) station
                        .getChildren()
                        .get(2);


        if (currentBusyCount > 0) {

            status.setText("Serving");

            status.setStyle(
                    "-fx-text-fill: #E65100;" +
                            "-fx-font-weight: bold;"
            );

        } else {

            status.setText("Idle");

            status.setStyle(
                    "-fx-text-fill: green;"
            );
        }
    }


    // =========================================================
    // STOP PREVIOUS ANIMATION
    // =========================================================

    private void stopCurrentAnimation() {

        if (customerGenerator != null) {

            customerGenerator.stop();
            customerGenerator = null;
        }


        busyCounters.clear();
    }


    // =========================================================
    // HUMAN CASHIER VISUALS
    // =========================================================

    private void createHumanCashiers(
            int count
    ) {

        humanCashierContainer
                .getChildren()
                .clear();


        humanCashierViews.clear();


        for (int i = 1; i <= count; i++) {

            VBox cashier =
                    new VBox(5);


            cashier.setAlignment(
                    Pos.CENTER
            );


            cashier.setPrefWidth(90);
            cashier.setPrefHeight(100);


            cashier.setStyle(
                    "-fx-background-color: #E3F2FD;" +
                            "-fx-border-color: #2196F3;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 7;" +
                            "-fx-background-radius: 7;" +
                            "-fx-padding: 10;"
            );


            Label icon =
                    new Label("H");


            icon.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );


            Label name =
                    new Label(
                            "Cashier " + i
                    );


            Label status =
                    new Label("Idle");


            status.setStyle(
                    "-fx-text-fill: green;"
            );


            cashier
                    .getChildren()
                    .addAll(
                            icon,
                            name,
                            status
                    );


            humanCashierContainer
                    .getChildren()
                    .add(cashier);


            humanCashierViews
                    .add(cashier);


            busyCounters.put(
                    cashier,
                    0
            );
        }
    }


    // =========================================================
    // MACHINE CASHIER VISUALS
    // =========================================================

    private void createMachineCashiers(
            int count
    ) {

        machineCashierContainer
                .getChildren()
                .clear();


        machineCashierViews.clear();


        for (int i = 1; i <= count; i++) {

            VBox machine =
                    new VBox(5);


            machine.setAlignment(
                    Pos.CENTER
            );


            machine.setPrefWidth(90);
            machine.setPrefHeight(100);


            machine.setStyle(
                    "-fx-background-color: #F3E5F5;" +
                            "-fx-border-color: #9C27B0;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 7;" +
                            "-fx-background-radius: 7;" +
                            "-fx-padding: 10;"
            );


            Label icon =
                    new Label("M");


            icon.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );


            Label name =
                    new Label(
                            "Machine " + i
                    );


            Label status =
                    new Label("Idle");


            status.setStyle(
                    "-fx-text-fill: green;"
            );


            machine
                    .getChildren()
                    .addAll(
                            icon,
                            name,
                            status
                    );


            machineCashierContainer
                    .getChildren()
                    .add(machine);


            machineCashierViews
                    .add(machine);


            busyCounters.put(
                    machine,
                    0
            );
        }
    }
}