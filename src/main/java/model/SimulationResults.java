// SimulationResults.java
package model;

import java.util.Map;

public class SimulationResults {
    private int totalCustomers;
    private int totalCustomersServed;
    private int totalCustomersAbandoned;
    private double totalWaitingTime;
    private double totalSystemTime;
    private double averageWaitingTime;
    private double averageSystemTime;
    private double simulationDuration;
    private Map<Integer, StationStats> stationStats;
    
    // Getters and Setters
    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
    
    public int getTotalCustomersServed() { return totalCustomersServed; }
    public void setTotalCustomersServed(int totalCustomersServed) { this.totalCustomersServed = totalCustomersServed; }
    
    public int getTotalCustomersAbandoned() { return totalCustomersAbandoned; }
    public void setTotalCustomersAbandoned(int totalCustomersAbandoned) { this.totalCustomersAbandoned = totalCustomersAbandoned; }
    
    public double getTotalWaitingTime() { return totalWaitingTime; }
    public void setTotalWaitingTime(double totalWaitingTime) { this.totalWaitingTime = totalWaitingTime; }
    
    public double getTotalSystemTime() { return totalSystemTime; }
    public void setTotalSystemTime(double totalSystemTime) { this.totalSystemTime = totalSystemTime; }
    
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public void setAverageWaitingTime(double averageWaitingTime) { this.averageWaitingTime = averageWaitingTime; }
    
    public double getAverageSystemTime() { return averageSystemTime; }
    public void setAverageSystemTime(double averageSystemTime) { this.averageSystemTime = averageSystemTime; }
    
    public double getSimulationDuration() { return simulationDuration; }
    public void setSimulationDuration(double simulationDuration) { this.simulationDuration = simulationDuration; }
    
    public Map<Integer, StationStats> getStationStats() { return stationStats; }
    public void setStationStats(Map<Integer, StationStats> stationStats) { this.stationStats = stationStats; }
    
    public double getAbandonmentRate() {
        return totalCustomers > 0 ? (double) totalCustomersAbandoned / totalCustomers : 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SIMULATION RESULTS ===\n");
        sb.append(String.format("Simulation Duration: %.2f\n", simulationDuration));
        sb.append(String.format("Total Customers: %d\n", totalCustomers));
        sb.append(String.format("Customers Served: %d\n", totalCustomersServed));
        sb.append(String.format("Customers Abandoned: %d\n", totalCustomersAbandoned));
        sb.append(String.format("Abandonment Rate: %.2f%%\n", getAbandonmentRate() * 100));
        sb.append(String.format("Average Waiting Time: %.2f\n", averageWaitingTime));
        sb.append(String.format("Average System Time: %.2f\n", averageSystemTime));
        sb.append("\n=== STATION STATISTICS ===\n");
        
        for (StationStats stats : stationStats.values()) {
            sb.append(stats.toString()).append("\n");
        }
        
        return sb.toString();
    }
}

