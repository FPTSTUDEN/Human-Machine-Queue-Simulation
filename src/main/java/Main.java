// Main.java
// package main;

import controller.Controller;
import model.SimulationConfig;
import model.SimulationResults;

public class Main {
    
    public static void main(String[] args) {
        // Configuration parameters
        int humanCashierCount = 2;
        int selfCheckoutCount = 3;
        double arrivalRate = 10.0; // customers per hour
        double[] humanServiceTimes = {2.0, 3.0, 4.0, 5.0}; // minutes
        double[] machineServiceTimes = {1.0, 1.5, 2.0, 2.5, 3.0}; // minutes
        double simulationDuration = 480.0; // 8 hours in minutes
        double maximumWaitingTime = 5.0; // 5 minutes
        
        // Create configuration
        SimulationConfig config = new SimulationConfig(
            humanCashierCount,
            selfCheckoutCount,
            arrivalRate / 60.0, // Convert to customers per minute
            humanServiceTimes,
            machineServiceTimes,
            simulationDuration,
            maximumWaitingTime
        );
        
        // Create and run simulation
        System.out.println("Starting Supermarket Checkout Simulation...");
        System.out.println("===========================================");
        System.out.println("Configuration:");
        System.out.printf("  Human Cashiers: %d\n", humanCashierCount);
        System.out.printf("  Self-Checkouts: %d\n", selfCheckoutCount);
        System.out.printf("  Arrival Rate: %.1f customers/hour\n", arrivalRate);
        System.out.printf("  Max Waiting Time: %.1f minutes\n", maximumWaitingTime);
        System.out.printf("  Simulation Duration: %.1f minutes (%.1f hours)\n", 
                simulationDuration, simulationDuration / 60);
        System.out.println("===========================================\n");
        
        Controller controller = new Controller(config);
        SimulationResults results = controller.runSimulation();
        
        // Print results
        System.out.println(results);
    }
    
}