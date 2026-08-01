import model.SimulationConfig;
import model.SimulationResult;
import model.SupermarketModel;

public class Main {

    public static void main(String[] args) {

        SupermarketModel model = new SupermarketModel();

        double[] humanServiceTimes = {2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
        double[] machineServiceTimes = {3.0, 3.5, 4.0, 4.5, 5.0, 5.5};

        SimulationConfig config =
                new SimulationConfig(
                        2,                      // human cashiers
                        1,                      // self-checkout kiosks
                        20.0,                   // arrival rate (customers per hour)
                        humanServiceTimes,      // human service times
                        machineServiceTimes,    // machine service times
                        60.0,                   // simulation duration (minutes)
                        15.0                     // maximum waiting time (minutes)
                );

        SimulationResult result =
                model.runSimulation(config);

        System.out.println(
                "Number of stations: "
                        + model.getCheckoutStations().size()
        );

        System.out.println(
                "Customers served: "
                        + result.getServedCustomers()
        );

        System.out.println(
                "Customers abandoned: "
                        + result.getAbandonedCustomers()
        );

        System.out.println(
                "Average waiting time: "
                        + String.format("%.2f", result.getAverageWaitingTime())
        );

        System.out.println(
                "Average service time: "
                        + String.format("%.2f", result.getAverageServiceTime())
        );

        System.out.println(
                "Average queue length: "
                        + String.format("%.2f", result.getAverageQueueLength())
        );

        System.out.println(
                "Maximum queue length: "
                        + result.getMaximumQueueLength()
        );

        System.out.println(
                "Human utilization: "
                        + String.format("%.2f%%", result.getHumanUtilization() * 100)
        );

        System.out.println(
                "Machine utilization: "
                        + String.format("%.2f%%", result.getMachineUtilization() * 100)
        );

        System.out.println(
                "Throughput: "
                        + String.format("%.2f", result.getThroughput())
        );
    }
}