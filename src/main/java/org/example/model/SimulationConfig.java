package org.example.model;
/**
 * Stores the input parameters received from the Controller
 * before a simulation run starts.
 */
public class SimulationConfig {

    private final int humanCashierCount;
    private final int selfCheckoutCount;

    private final double arrivalRate;
    private final double humanServiceTime;
    private final double machineServiceTime;

    private final double simulationDuration;
    private final double maximumWaitingTime;

    public SimulationConfig(
            int humanCashierCount,
            int selfCheckoutCount,
            double arrivalRate,
            double humanServiceTime,
            double machineServiceTime,
            double simulationDuration,
            double maximumWaitingTime) {

        this.humanCashierCount = humanCashierCount;
        this.selfCheckoutCount = selfCheckoutCount;
        this.arrivalRate = arrivalRate;
        this.humanServiceTime = humanServiceTime;
        this.machineServiceTime = machineServiceTime;
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

    public double getHumanServiceTime() {
        return humanServiceTime;
    }

    public double getMachineServiceTime() {
        return machineServiceTime;
    }

    public double getSimulationDuration() {
        return simulationDuration;
    }

    public double getMaximumWaitingTime() {
        return maximumWaitingTime;
    }
}