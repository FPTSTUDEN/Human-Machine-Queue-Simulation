import model.SimulationConfig;
import model.SimulationResult;
import model.SupermarketModel;

public class TestingModel {

    public static void main(String[] args) {

        // Create the simulation model
        SupermarketModel model = new SupermarketModel();

        // Create simulation configuration
        SimulationConfig config = new SimulationConfig(
                2,      // Human cashiers
                1,      // Self-checkout kiosks
                20,     // Arrival rate (customers/hour)
                3.0,    // Average human service time (minutes)
                4.0,    // Average machine service time (minutes)
                60.0,   // Simulation duration (minutes)
                5.0     // Maximum waiting time (minutes)
        );

        // Run the simulation (currently returns placeholder values)
        SimulationResult result = model.runSimulation(config);

        // Print basic information
        System.out.println("========== MODEL TEST ==========");
        System.out.println("Number of checkout stations: "
                + model.getCheckoutStations().size());

        System.out.println();

        System.out.println("========== SIMULATION RESULT ==========");
        System.out.println("Customers served: "
                + result.getServedCustomers());

        System.out.println("Customers abandoned: "
                + result.getAbandonedCustomers());

        System.out.println("Average waiting time: "
                + result.getAverageWaitingTime());

        System.out.println("Average service time: "
                + result.getAverageServiceTime());

        System.out.println("Average queue length: "
                + result.getAverageQueueLength());

        System.out.println("Maximum queue length: "
                + result.getMaximumQueueLength());

        System.out.println("Human utilization: "
                + result.getHumanUtilization());

        System.out.println("Machine utilization: "
                + result.getMachineUtilization());

        System.out.println("Throughput: "
                + result.getThroughput());
    }
}