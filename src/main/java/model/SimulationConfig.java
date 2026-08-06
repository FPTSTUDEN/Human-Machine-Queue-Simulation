package model;

/**
 * Stores the input parameters received from the Controller
 * before a simulation run starts.
 */
public class SimulationConfig {

    private final int humanCashierCount;
    private final int selfCheckoutCount;

    private final double arrivalRate;
    private final double[] humanServiceTimes;
    private final double[] machineServiceTimes;

    private final double simulationDuration;
    private final double maximumWaitingTime;

    public SimulationConfig(
            int humanCashierCount,
            int selfCheckoutCount,
            double arrivalRate,
            double[] humanServiceTimes,
            double[] machineServiceTimes,
            double simulationDuration,
            double maximumWaitingTime) {

        this.humanCashierCount = humanCashierCount;
        this.selfCheckoutCount = selfCheckoutCount;
        this.arrivalRate = arrivalRate;
        this.humanServiceTimes = humanServiceTimes;
        this.machineServiceTimes = machineServiceTimes;
        this.simulationDuration = simulationDuration;
        this.maximumWaitingTime = maximumWaitingTime;
    }

    public int getHumanCashierCount() {
        return humanCashierCount;
    }

    public int getSelfCheckoutCount() {
        return selfCheckoutCount;
    }

    public double getArrivalRate() {
        return arrivalRate;
    }

    public double[] getHumanServiceTimes() {
        return humanServiceTimes;
    }

    public double[] getMachineServiceTimes() {
        return machineServiceTimes;
    }

    public double getSimulationDuration() {
        return simulationDuration;
    }

    public double getMaximumWaitingTime() {
        return maximumWaitingTime;
    }
}