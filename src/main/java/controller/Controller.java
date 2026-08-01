// Controller.java
package controller;

import model.*;
import java.util.*;

/**
 * Main controller for the supermarket checkout simulation.
 * Implements three-phase simulation: Arrivals, Service Completions, and Abandonments.
 */
public class Controller {
    
    private final SimulationConfig config;
    private final List<CheckoutStation> stations;
    private final List<Customer> allCustomers;
    private final List<Customer> abandonedCustomers;
    
    private double currentTime;
    private int customerIdCounter;
    private double nextArrivalTime;
    private boolean simulationRunning;
    
    // Statistics
    private int totalCustomersServed;
    private int totalCustomersAbandoned;
    private double totalWaitingTime;
    private double totalSystemTime;
    
    public Controller(SimulationConfig config) {
        this.config = config;
        this.stations = new ArrayList<>();
        this.allCustomers = new ArrayList<>();
        this.abandonedCustomers = new ArrayList<>();
        this.currentTime = 0;
        this.customerIdCounter = 0;
        this.simulationRunning = false;
        this.totalCustomersServed = 0;
        this.totalCustomersAbandoned = 0;
        this.totalWaitingTime = 0;
        this.totalSystemTime = 0;
        
        initializeStations();
    }
    
    /**
     * Initializes checkout stations with service time arrays
     */
    private void initializeStations() {
        int stationId = 0;
        
        // Create human cashier stations
        for (int i = 0; i < config.getHumanCashierCount(); i++) {
            stations.add(new CheckoutStation(
                stationId++,
                CheckoutType.HUMAN_CASHIER,
                config.getHumanServiceTimes()
            ));
        }
        
        // Create self-checkout stations
        for (int i = 0; i < config.getSelfCheckoutCount(); i++) {
            stations.add(new CheckoutStation(
                stationId++,
                CheckoutType.SELF_CHECKOUT,
                config.getMachineServiceTimes()
            ));
        }
    }
    
    /**
     * Runs the complete simulation
     */
    public SimulationResults runSimulation() {
        simulationRunning = true;
        currentTime = 0;
        generateFirstArrival();
        
        while (simulationRunning && currentTime < config.getSimulationDuration()) {
            // Phase 1: Process arrivals
            processArrivals();
            
            // Phase 2: Process service completions
            processServiceCompletions();
            
            // Phase 3: Process abandonments
            processAbandonments();
            
            // Advance time to next event
            advanceTime();
        }
        
        // Process remaining customers at end of simulation
        processRemainingCustomers();
        
        return generateResults();
    }
    
    /**
     * Generates the first arrival time
     */
    private void generateFirstArrival() {
        nextArrivalTime = generateExponentialInterarrival();
    }
    
    /**
     * Phase 1: Process all arrivals up to current time
     */
    private void processArrivals() {
        while (nextArrivalTime <= currentTime && currentTime < config.getSimulationDuration()) {
            // Create new customer
            Customer customer = new Customer(
                customerIdCounter++,
                nextArrivalTime,
                0 // Service time will be assigned by station
            );
            
            // Route to best station
            CheckoutStation selectedStation = selectBestStation();
            if (selectedStation != null) {
                selectedStation.addCustomer(customer);
                allCustomers.add(customer);
                
                // If station is free, start service immediately
                if (!selectedStation.isBusy()) {
                    selectedStation.startNextCustomer(currentTime);
                }
            }
            
            // Generate next arrival
            nextArrivalTime += generateExponentialInterarrival();
        }
    }
    
    /**
     * Selects the station with the shortest queue
     */
    private CheckoutStation selectBestStation() {
        if (stations.isEmpty()) {
            return null;
        }
        CheckoutStation bestStation = Collections.min(stations);
        
        return bestStation;
    }
    
    /**
     * Phase 2: Process all service completions
     */
    private void processServiceCompletions() {
        boolean hasCompletions = true;
        
        // Loop to handle simultaneous completions
        while (hasCompletions) {
            hasCompletions = false;
            
            for (CheckoutStation station : stations) {
                if (station.isBusy()) {
                    Customer currentCustomer = station.getCurrentCustomer();
                    double finishTime = currentCustomer.getServiceStartTime() + 
                                      currentCustomer.getServiceTime();
                    
                    if (finishTime <= currentTime) {
                        // Complete service
                        station.completeService(currentTime);
                        totalCustomersServed++;
                        
                        // Update statistics
                        totalWaitingTime += currentCustomer.getWaitingTime();
                        totalSystemTime += currentCustomer.getTotalTimeInSystem();
                        
                        // Start next customer immediately
                        station.startNextCustomer(currentTime);
                        
                        hasCompletions = true;
                    }
                }
            }
        }
    }
    
    /**
     * Phase 3: Process customer abandonments
     */
    private void processAbandonments() {
        double maxWaitingTime = config.getMaximumWaitingTime();
        
        for (CheckoutStation station : stations) {
            // Check if customers in queue are waiting too long
            while (station.hasWaitingCustomers()) {
                Customer nextCustomer = station.peekNextCustomer();
                double waitingTime = currentTime - nextCustomer.getArrivalTime();
                
                if (waitingTime > maxWaitingTime) {
                    // Customer abandons
                    Customer abandoned = station.removeNextCustomer();
                    if (abandoned != null) {
                        abandoned.setAbandoned(true);
                        abandoned.setAbandonmentTime(currentTime);
                        abandonedCustomers.add(abandoned);
                        totalCustomersAbandoned++;
                    }
                } else {
                    break; // Queue is ordered by arrival time, so break if next customer hasn't waited too long
                }
            }
        }
    }
    
    /**
     * Advances time to the next event
     */
    private void advanceTime() {
        double nextEventTime = Double.MAX_VALUE;
        
        // Check next arrival
        if (nextArrivalTime < config.getSimulationDuration()) {
            nextEventTime = Math.min(nextEventTime, nextArrivalTime);
        }
        
        // Check next service completion
        for (CheckoutStation station : stations) {
            if (station.isBusy()) {
                Customer customer = station.getCurrentCustomer();
                double finishTime = customer.getServiceStartTime() + customer.getServiceTime();
                if (finishTime <= config.getSimulationDuration()) {
                    nextEventTime = Math.min(nextEventTime, finishTime);
                }
            }
        }
        
        // Check next abandonment (optional: check every small time step)
        // For efficiency, we check abandonments at each event time
        
        // If no more events, end simulation
        if (nextEventTime == Double.MAX_VALUE) {
            simulationRunning = false;
            currentTime = config.getSimulationDuration();
        } else {
            currentTime = Math.min(nextEventTime, config.getSimulationDuration());
        }
    }
    
    /**
     * Processes any remaining customers when simulation ends
     */
    private void processRemainingCustomers() {
        // Complete all current services
        for (CheckoutStation station : stations) {
            if (station.isBusy()) {
                Customer customer = station.getCurrentCustomer();
                station.completeService(currentTime);
                totalCustomersServed++;
                totalWaitingTime += customer.getWaitingTime();
                totalSystemTime += customer.getTotalTimeInSystem();
            }
        }
        
    }
    
    /**
     * Generates exponential inter-arrival time
     */
    private double generateExponentialInterarrival() {
        Random random = new Random();
        return -Math.log(1 - random.nextDouble()) / config.getArrivalRate();
    }
    
    /**
     * Generates simulation results
     */
    private SimulationResults generateResults() {
        SimulationResults results = new SimulationResults();
        results.setTotalCustomers(allCustomers.size());
        results.setTotalCustomersServed(totalCustomersServed);
        results.setTotalCustomersAbandoned(totalCustomersAbandoned);
        results.setTotalWaitingTime(totalWaitingTime);
        results.setTotalSystemTime(totalSystemTime);
        results.setSimulationDuration(currentTime);
        
        // Calculate station statistics
        Map<Integer, StationStats> stationStats = new HashMap<>();
        for (CheckoutStation station : stations) {
            StationStats stats = new StationStats();
            stats.setStationId(station.getId());
            stats.setType(station.getType());
            stats.setServedCustomers(station.getServedCustomerCount());
            stats.setTotalBusyTime(station.getTotalBusyTime());
            stats.setUtilization(station.getTotalBusyTime() / currentTime);
            stationStats.put(station.getId(), stats);
        }
        results.setStationStats(stationStats);
        
        // Calculate average waiting time
        if (totalCustomersServed > 0) {
            results.setAverageWaitingTime(totalWaitingTime / totalCustomersServed);
            results.setAverageSystemTime(totalSystemTime / totalCustomersServed);
        }
        
        return results;
    }
    
    public List<CheckoutStation> getStations() {
        return stations;
    }
    
    public List<Customer> getAllCustomers() {
        return allCustomers;
    }
    
    public List<Customer> getAbandonedCustomers() {
        return abandonedCustomers;
    }
    
    public double getCurrentTime() {
        return currentTime;
    }
}