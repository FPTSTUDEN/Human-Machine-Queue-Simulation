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
    private final Random random;
    
    private double currentTime;
    private int customerIdCounter;
    private double nextArrivalTime;
    
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
        this.random = new Random();
        this.currentTime = 0;
        this.customerIdCounter = 0;
        this.totalCustomersServed = 0;
        this.totalCustomersAbandoned = 0;
        this.totalWaitingTime = 0;
        this.totalSystemTime = 0;
        
        initializeStations();
    }
    
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
    
    public SimulationResults runSimulation() {
        /**
         * Run the simulation until the specified duration is reached.
         */
        currentTime = 0;
        nextArrivalTime = generateExponentialInterarrival();
        
        while (currentTime < config.getSimulationDuration()) {
            processArrivals();
            processServiceCompletions();
            processAbandonments();
            advanceTime();
        }
        
        finishRemainingServices();
        return generateResults();
    }
    
    private void processArrivals() {
        /**
         * Assign customers to stations based on their arrival times and the current state of the stations.
         */
        while (nextArrivalTime <= currentTime && currentTime < config.getSimulationDuration()) {
            Customer customer = createCustomer(nextArrivalTime);
            assignCustomerToStation(customer);
            nextArrivalTime += generateExponentialInterarrival();
        }
    }
    
    private Customer createCustomer(double arrivalTime) {
        return new Customer(customerIdCounter++, arrivalTime, 0);
    }
    
    private void assignCustomerToStation(Customer customer) {
        /** 
         * Assign the customer to the best available checkout station based on the current state of the stations.
         */
        CheckoutStation station = selectBestStation();
        if (station == null) return;
        
        station.addCustomer(customer);
        allCustomers.add(customer);
        
        if (!station.isBusy()) {
            station.startNextCustomer(currentTime);
        }
    }
    
    private CheckoutStation selectBestStation() {
        return stations.isEmpty() ? null : Collections.min(stations);
    }
    
    private void processServiceCompletions() {
        /**
         * Check each station for service completions and process them accordingly.
         * Use a loop to ensure all completions are processed before moving to the next phase.
         */
        boolean hasCompletions;
        do {
            hasCompletions = false;
            for (CheckoutStation station : stations) {
                if (station.isBusy() && isServiceComplete(station)) {
                    completeService(station);
                    hasCompletions = true;
                }
            }
        } while (hasCompletions);
    }
    
    private boolean isServiceComplete(CheckoutStation station) {
        /**
         * Check if the station service is complete.
         * Use the current time and the service start time of the customer to determine if the service duration has elapsed.
         */
        Customer customer = station.getCurrentCustomer();
        double finishTime = customer.getServiceStartTime() + customer.getServiceTime();
        return finishTime <= currentTime;
    }
    
    private void completeService(CheckoutStation station) {
        /**
         * Complete the service at the station for the current customer.
         * Update statistics for served customers and start the next customer in line if available.
         */
        Customer customer = station.getCurrentCustomer();
        station.completeService(currentTime);
        totalCustomersServed++;
        totalWaitingTime += customer.getWaitingTime();
        totalSystemTime += customer.getTotalTimeInSystem();
        station.startNextCustomer(currentTime);
    }
    
    private void processAbandonments() {
        /**
         * Mark customers as abandoned if exceeding the maximum allowed waiting time.
         */
        double maxWaitTime = config.getMaximumWaitingTime();
        
        for (CheckoutStation station : stations) {
            while (station.hasWaitingCustomers()) {
                Customer next = station.peekNextCustomer();
                if (currentTime - next.getArrivalTime() <= maxWaitTime) {
                    break;
                }
                abandonCustomer(station);
            }
        }
    }
    
    private void abandonCustomer(CheckoutStation station) {
        /**
         * Remove the next customer from the station's queue and mark them as abandoned.
         * Update statistics for abandoned customers.
         */
        Customer customer = station.removeNextCustomer();
        if (customer != null) {
            customer.setAbandoned(true);
            customer.setAbandonmentTime(currentTime);
            abandonedCustomers.add(customer);
            totalCustomersAbandoned++;
        }
    }
    
    private void advanceTime() {
        /**
         * Advance the simulation time to the next event.
         * The time is determined by the next arrival or the next service completion, whichever is sooner.
         */
        double nextEventTime = findNextEventTime();
        
        if (nextEventTime == Double.MAX_VALUE) {
            currentTime = config.getSimulationDuration();
        } else {
            currentTime = Math.min(nextEventTime, config.getSimulationDuration());
        }
    }
    
    private double findNextEventTime() {
        /**
         * Find the next event time by checking busy stations.
         * Returns the minimum of the station's next arrival time and the next service completion time.
         */
        double nextTime = Double.MAX_VALUE;
        
        // Next arrival
        if (nextArrivalTime < config.getSimulationDuration()) {
            nextTime = Math.min(nextTime, nextArrivalTime);
        }
        
        // Next service completion
        for (CheckoutStation station : stations) {
            if (station.isBusy()) {
                Customer customer = station.getCurrentCustomer();
                double finishTime = customer.getServiceStartTime() + customer.getServiceTime();
                if (finishTime <= config.getSimulationDuration()) {
                    nextTime = Math.min(nextTime, finishTime);
                }
            }
        }
        
        return nextTime;
    }
    
    private void finishRemainingServices() {
        /**
         * Complete remaining services for all busy stations.
         * Update statistics for served customers and ensure all stations are processed before finalizing the simulation.
         */
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
    
    private double generateExponentialInterarrival() {
        /**
         * Generate an exponentially distributed interarrival time based on the configured arrival rate.
         * Uses the inverse transform sampling method to generate the interarrival time.
         */
        return -Math.log(1 - random.nextDouble()) / config.getArrivalRate();
    }
    
    private SimulationResults generateResults() {
        /**
         * Generate the final simulation results, 
         * including total customers, served, abandoned, waiting time, system time, and station statistics.
         * Returns a SimulationResults object containing all relevant statistics after the simulation run.
         * This method is called after the simulation has completed to summarize the outcomes.
         */
        SimulationResults results = new SimulationResults();
        results.setTotalCustomers(allCustomers.size());
        results.setTotalCustomersServed(totalCustomersServed);
        results.setTotalCustomersAbandoned(totalCustomersAbandoned);
        results.setTotalWaitingTime(totalWaitingTime);
        results.setTotalSystemTime(totalSystemTime);
        results.setSimulationDuration(currentTime);
        
        // Station statistics
        Map<Integer, StationStats> stationStats = new HashMap<>();
        for (CheckoutStation station : stations) {
            StationStats stats = new StationStats();
            stats.setStationId(station.getId());
            stats.setType(station.getType());
            stats.setServedCustomers(station.getServedCustomerCount());
            stats.setTotalBusyTime(station.getTotalBusyTime());
            stats.setUtilization(currentTime > 0 ? station.getTotalBusyTime() / currentTime : 0);
            stationStats.put(station.getId(), stats);
        }
        results.setStationStats(stationStats);
        
        // Averages
        if (totalCustomersServed > 0) {
            results.setAverageWaitingTime(totalWaitingTime / totalCustomersServed);
            results.setAverageSystemTime(totalSystemTime / totalCustomersServed);
        }
        
        return results;
    }
    
    // Getters
    public List<CheckoutStation> getStations() { return stations; }
    public List<Customer> getAllCustomers() { return allCustomers; }
    public List<Customer> getAbandonedCustomers() { return abandonedCustomers; }
    public double getCurrentTime() { return currentTime; }
}