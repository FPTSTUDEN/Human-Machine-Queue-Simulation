package model;
/**
 * Stores the performance results produced by the Model
 * and returned to the Controller after a simulation run.
 */
public class SimulationResult {

    private final int servedCustomers;
    private final int abandonedCustomers;

    private final double averageWaitingTime;
    private final double averageServiceTime;
    private final double averageQueueLength;
    private final double maximumQueueLength;

    private final double humanUtilization;
    private final double machineUtilization;
    private final double throughput;

    public SimulationResult(
            int servedCustomers,
            int abandonedCustomers,
            double averageWaitingTime,
            double averageServiceTime,
            double averageQueueLength,
            double maximumQueueLength,
            double humanUtilization,
            double machineUtilization,
            double throughput) {

        this.servedCustomers = servedCustomers;
        this.abandonedCustomers = abandonedCustomers;
        this.averageWaitingTime = averageWaitingTime;
        this.averageServiceTime = averageServiceTime;
        this.averageQueueLength = averageQueueLength;
        this.maximumQueueLength = maximumQueueLength;
        this.humanUtilization = humanUtilization;
        this.machineUtilization = machineUtilization;
        this.throughput = throughput;
    }

    public int getServedCustomers() {
        return servedCustomers;
    }

    public int getAbandonedCustomers() {
        return abandonedCustomers;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageServiceTime() {
        return averageServiceTime;
    }

    public double getAverageQueueLength() {
        return averageQueueLength;
    }

    public double getMaximumQueueLength() {
        return maximumQueueLength;
    }

    public double getHumanUtilization() {
        return humanUtilization;
    }

    public double getMachineUtilization() {
        return machineUtilization;
    }

    public double getThroughput() {
        return throughput;
    }
}