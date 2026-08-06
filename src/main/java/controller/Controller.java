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
        Customer customer = station.getCurrentCustomer();
        double finishTime = customer.getServiceStartTime() + customer.getServiceTime();
        return finishTime <= currentTime;
    }
    
    private void completeService(CheckoutStation station) {
        Customer customer = station.getCurrentCustomer();
        station.completeService(currentTime);
        totalCustomersServed++;
        totalWaitingTime += customer.getWaitingTime();
        totalSystemTime += customer.getTotalTimeInSystem();
        station.startNextCustomer(currentTime);
    }
    
    private void processAbandonments() {
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
        Customer customer = station.removeNextCustomer();
        if (customer != null) {
            customer.setAbandoned(true);
            customer.setAbandonmentTime(currentTime);
            abandonedCustomers.add(customer);
            totalCustomersAbandoned++;
        }
    }
    
    private void advanceTime() {
        double nextEventTime = findNextEventTime();
        
        if (nextEventTime == Double.MAX_VALUE) {
            currentTime = config.getSimulationDuration();
        } else {
            currentTime = Math.min(nextEventTime, config.getSimulationDuration());
        }
    }
    
    private double findNextEventTime() {
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
        return -Math.log(1 - random.nextDouble()) / config.getArrivalRate();
    }
    
    private SimulationResults generateResults() {
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