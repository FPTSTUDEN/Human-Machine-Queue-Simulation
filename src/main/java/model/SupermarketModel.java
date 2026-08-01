package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main Model class of the MVC architecture.
 * Manages customers, checkout stations, and simulation execution.
 * Uses three-phase simulation approach (A, B, C phases).
 */
public class SupermarketModel {

    private final List<CheckoutStation> checkoutStations;
    private int nextCustomerId;
    private Random random;

    // Simulation state
    private double currentTime;
    private SimulationConfig config;
    private double nextArrivalTime;
    
    // Statistics
    private int totalServed;
    private int totalAbandoned;
    private double totalWaitingTime;
    private double totalServiceTime;
    private double totalQueueLength;
    private int totalQueueMeasurements;
    private int maxQueueLength;
    
    // Track total customers for debugging
    private int totalCustomersCreated;

    // Constants
    private static final int MAX_QUEUE_LENGTH = 10;

    public SupermarketModel() {
        checkoutStations = new ArrayList<>();
        nextCustomerId = 1;
        random = new Random();
    }

    /**
     * Creates the required number of human cashier
     * and self-checkout stations.
     */
    public void createCheckoutStations(
            int humanCashierCount,
            int selfCheckoutCount) {

        checkoutStations.clear();

        int stationId = 1;

        for (int i = 0; i < humanCashierCount; i++) {
            checkoutStations.add(
                    new CheckoutStation(
                            stationId,
                            CheckoutType.HUMAN_CASHIER
                    )
            );
            stationId++;
        }

        for (int i = 0; i < selfCheckoutCount; i++) {
            checkoutStations.add(
                    new CheckoutStation(
                            stationId,
                            CheckoutType.SELF_CHECKOUT
                    )
            );
            stationId++;
        }
    }

    /**
     * Creates a new customer entering the simulation.
     */
    public Customer createCustomer(double arrivalTime, double serviceTime) {
        Customer customer =
                new Customer(nextCustomerId, arrivalTime, serviceTime);
        nextCustomerId++;
        totalCustomersCreated++;
        return customer;
    }

    /**
     * Selects the shortest queue among stations of the given type.
     */
    private CheckoutStation selectShortestQueue(CheckoutType type) {
        CheckoutStation selected = null;
        int minQueueLength = Integer.MAX_VALUE;

        for (CheckoutStation station : checkoutStations) {
            if (station.getType() == type) {
                int queueLength = station.getTotalCustomersIncludingCurrent();
                if (queueLength < minQueueLength) {
                    minQueueLength = queueLength;
                    selected = station;
                }
            }
        }

        return selected;
    }

    /**
     * Selects a service time from the given array.
     */
    private double selectServiceTime(double[] serviceTimes) {
        if (serviceTimes == null || serviceTimes.length == 0) {
            return 1.0;
        }
        int index = random.nextInt(serviceTimes.length);
        return serviceTimes[index];
    }

    /**
     * Phase A: Scan for and process all events that are due to occur
     * at the current simulation time.
     */
    private void phaseA() {
        // Process arrivals at current time
        while (Math.abs(nextArrivalTime - currentTime) < 1e-9) {
            processArrival();
            nextArrivalTime = currentTime + generateInterarrivalTime(config.getArrivalRate());
        }

        // Process service completions at current time
        for (CheckoutStation station : checkoutStations) {
            if (station.isBusy()) {
                double completionTime = station.getCurrentCompletionTime();
                if (Math.abs(completionTime - currentTime) < 1e-9) {
                    processServiceCompletion(station);
                }
            }
        }
    }

    /**
     * Phase B: Check for any conditions that have become true.
     */
    private void phaseB() {
        boolean conditionChanged = true;
        int maxIterations = 1000;
        int iterations = 0;
        
        while (conditionChanged && iterations < maxIterations) {
            conditionChanged = false;
            iterations++;
            
            // Check for idle stations that can start serving waiting customers
            for (CheckoutStation station : checkoutStations) {
                if (!station.isBusy() && station.hasWaitingCustomers()) {
                    Customer nextCustomer = station.peekNextCustomer();
                    if (nextCustomer != null) {
                        // Check if customer has already waited too long
                        double waitingTime = currentTime - nextCustomer.getArrivalTime();
                        if (waitingTime > config.getMaximumWaitingTime()) {
                            // Customer waited too long, remove and abandon
                            station.getNextWaitingCustomer(); // Remove from queue
                            nextCustomer.setAbandoned(true);
                            totalAbandoned++;
                            conditionChanged = true;
                        } else {
                            // Start service for this customer
                            station.startNextCustomer(currentTime);
                            conditionChanged = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Phase C: Advance time to the next event.
     */
    private double phaseC() {
        double nextEventTime = Double.MAX_VALUE;
        
        // Find next arrival time
        if (nextArrivalTime < nextEventTime) {
            nextEventTime = nextArrivalTime;
        }
        
        // Find next service completion time
        for (CheckoutStation station : checkoutStations) {
            if (station.isBusy()) {
                double completionTime = station.getCurrentCompletionTime();
                if (completionTime < nextEventTime) {
                    nextEventTime = completionTime;
                }
            }
        }
        
        if (nextEventTime == Double.MAX_VALUE) {
            return config.getSimulationDuration();
        }
        
        return Math.min(nextEventTime, config.getSimulationDuration());
    }

    /**
     * Process a customer arrival event.
     */
    private void processArrival() {
        // Randomly assign to human or self-checkout
        CheckoutType type;
        double serviceTime;
        
        if (random.nextBoolean()) {
            type = CheckoutType.HUMAN_CASHIER;
            serviceTime = selectServiceTime(config.getHumanServiceTimes());
        } else {
            type = CheckoutType.SELF_CHECKOUT;
            serviceTime = selectServiceTime(config.getMachineServiceTimes());
        }

        Customer customer = createCustomer(currentTime, serviceTime);

        // Select station with shortest queue
        CheckoutStation selectedStation = selectShortestQueue(type);

        if (selectedStation != null) {
            // Check if customer would wait too long based on queue length
            if (!selectedStation.canAcceptCustomer(MAX_QUEUE_LENGTH)) {
                customer.setAbandoned(true);
                totalAbandoned++;
                return;
            }

            selectedStation.addCustomer(customer);
            measureQueueLength();
            
            // If station is idle, start service immediately
            if (!selectedStation.isBusy()) {
                selectedStation.startNextCustomer(currentTime);
            }
        }
    }

    /**
     * Process a service completion event.
     */
    private void processServiceCompletion(CheckoutStation station) {
        // Complete current service and get the completed customer
        Customer customer = station.completeCurrentService(currentTime);
        
        if (customer != null) {
            // Record service statistics
            totalServed++;
            totalServiceTime += customer.getServiceTime();
            totalWaitingTime += customer.getWaitingTime();
        }
        
        // Try to start next customer
        boolean startedNext = station.startNextCustomer(currentTime);
        
        // If next customer started, check if they waited too long
        if (startedNext) {
            Customer nextCustomer = station.getCurrentCustomer();
            if (nextCustomer != null) {
                double waitingTime = currentTime - nextCustomer.getArrivalTime();
                if (waitingTime > config.getMaximumWaitingTime()) {
                    // Customer waited too long, abandon and try next
                    station.completeCurrentService(currentTime); // Force completion
                    nextCustomer.setAbandoned(true);
                    totalAbandoned++;
                    // Recursively process next customer
                    processServiceCompletion(station);
                }
            }
        }
    }

    /**
     * Measure current queue length across all stations.
     */
    private void measureQueueLength() {
        int currentTotalQueue = 0;
        for (CheckoutStation station : checkoutStations) {
            currentTotalQueue += station.getTotalCustomersIncludingCurrent();
        }
        totalQueueLength += currentTotalQueue;
        totalQueueMeasurements++;
        maxQueueLength = Math.max(maxQueueLength, currentTotalQueue);
    }

    /**
     * Generate interarrival time using exponential distribution.
     */
    private double generateInterarrivalTime(double rate) {
        // Rate is customers per hour, convert to customers per minute
        double ratePerMinute = rate / 60.0;
        return -Math.log(1 - random.nextDouble()) / ratePerMinute;
    }

    /**
     * Calculate average of service times.
     */
    private double getAverageServiceTime(double[] serviceTimes) {
        if (serviceTimes == null || serviceTimes.length == 0) {
            return 1.0;
        }
        double sum = 0;
        for (double time : serviceTimes) {
            sum += time;
        }
        return sum / serviceTimes.length;
    }

    /**
     * Reset all statistics.
     */
    private void resetStatistics() {
        totalServed = 0;
        totalAbandoned = 0;
        totalWaitingTime = 0;
        totalServiceTime = 0;
        totalQueueLength = 0;
        totalQueueMeasurements = 0;
        maxQueueLength = 0;
        totalCustomersCreated = 0;
    }

    /**
     * Runs the supermarket simulation using three-phase approach.
     */
    public SimulationResult runSimulation(SimulationConfig config) {
        this.config = config;
        this.currentTime = 0;
        
        createCheckoutStations(
                config.getHumanCashierCount(),
                config.getSelfCheckoutCount()
        );
        
        resetStatistics();
        nextArrivalTime = generateInterarrivalTime(config.getArrivalRate());
        
        // Main simulation loop using three-phase approach
        while (currentTime < config.getSimulationDuration()) {
            // Phase A: Process all events at current time
            phaseA();
            
            // Phase B: Process all conditional events
            phaseB();
            
            // Phase C: Advance time to next event
            double nextTime = phaseC();
            
            if (nextTime == currentTime) {
                break;
            }
            
            currentTime = nextTime;
        }
        
        // Handle remaining customers after simulation ends
        handleRemainingCustomers();
        
        // Debug output
        System.out.println("Total customers created: " + totalCustomersCreated);
        System.out.println("Total served: " + totalServed);
        System.out.println("Total abandoned: " + totalAbandoned);
        
        return calculateResults(config);
    }

    /**
     * Handle customers remaining after simulation ends.
     */
    private void handleRemainingCustomers() {
        for (CheckoutStation station : checkoutStations) {
            // Abandon all customers in queues
            while (station.hasWaitingCustomers()) {
                Customer customer = station.getNextWaitingCustomer();
                if (customer != null && !customer.isAbandoned()) {
                    customer.setAbandoned(true);
                    totalAbandoned++;
                }
            }
            
            // Complete service for current customer if they were being served
            if (station.isBusy()) {
                Customer customer = station.getCurrentCustomer();
                if (customer != null && !customer.isAbandoned()) {
                    // This customer was being served, count them as served
                    totalServed++;
                    totalServiceTime += customer.getServiceTime();
                    totalWaitingTime += customer.getWaitingTime();
                }
                station.completeCurrentService(currentTime);
            }
        }
    }

    /**
     * Calculate simulation results.
     */
    private SimulationResult calculateResults(SimulationConfig config) {
        // Calculate utilization using station's own method
        double humanUtilization = 0;
        double machineUtilization = 0;
        int humanCount = 0;
        int machineCount = 0;

        for (CheckoutStation station : checkoutStations) {
            if (station.getType() == CheckoutType.HUMAN_CASHIER) {
                humanUtilization += station.getUtilization(config.getSimulationDuration());
                humanCount++;
            } else {
                machineUtilization += station.getUtilization(config.getSimulationDuration());
                machineCount++;
            }
        }

        if (humanCount > 0) {
            humanUtilization = humanUtilization / humanCount;
        }
        if (machineCount > 0) {
            machineUtilization = machineUtilization / machineCount;
        }

        double averageWaitingTime = totalServed > 0 ? totalWaitingTime / totalServed : 0;
        double averageServiceTime = totalServed > 0 ? totalServiceTime / totalServed : 0;
        double averageQueueLength = totalQueueMeasurements > 0 ? totalQueueLength / totalQueueMeasurements : 0;
        double throughput = config.getSimulationDuration() > 0 ? totalServed / config.getSimulationDuration() : 0;

        return new SimulationResult(
                totalServed,
                totalAbandoned,
                averageWaitingTime,
                averageServiceTime,
                averageQueueLength,
                maxQueueLength,
                humanUtilization,
                machineUtilization,
                throughput
        );
    }

    public List<CheckoutStation> getCheckoutStations() {
        return checkoutStations;
    }

    /**
     * Resets the simulation model.
     */
    public void reset() {
        checkoutStations.clear();
        nextCustomerId = 1;
        random = new Random();
        resetStatistics();
        config = null;
        currentTime = 0;
        nextArrivalTime = 0;
    }
}