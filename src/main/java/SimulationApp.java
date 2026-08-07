import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/org/example/view/SimDashboardView.fxml"
                        )
                );


        Scene scene =
                new Scene(
                        loader.load(),
                        1200,
                        850
                );


        stage.setTitle(
                "Human - Machine Queue Simulation"
        );

        stage.setScene(scene);

        stage.setMinWidth(1050);
        stage.setMinHeight(750);

        stage.show();
    }


    public static void main(String[] args) {

        launch(args);
    }
}