import controller.Controller;
import model.SimulationConfig;
import model.SimulationResults;

public class TestingModel {

    public static void main(String[] args) {

        int humanCashierCount = 2;
        int selfCheckoutCount = 3;
        double arrivalRate = 20.0 / 60.0;

        double[] humanServiceTimes = {
                2.0, 3.0, 4.0, 5.0
        };

        double[] machineServiceTimes = {
                1.0, 1.5, 2.0, 2.5, 3.0
        };

        double simulationDuration = 480.0;
        double maximumWaitingTime = 5.0;

        SimulationConfig config = new SimulationConfig(
                humanCashierCount,
                selfCheckoutCount,
                arrivalRate,
                humanServiceTimes,
                machineServiceTimes,
                simulationDuration,
                maximumWaitingTime
        );

        Controller controller = new Controller(config);
        SimulationResults results = controller.runSimulation();

        System.out.println(results);
    }
}