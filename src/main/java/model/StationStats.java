// StationStats.java
package model;

public class StationStats {
    private int stationId;
    private CheckoutType type;
    private int servedCustomers;
    private double totalBusyTime;
    private double utilization;
    
    // Getters and Setters
    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }
    
    public CheckoutType getType() { return type; }
    public void setType(CheckoutType type) { this.type = type; }
    
    public int getServedCustomers() { return servedCustomers; }
    public void setServedCustomers(int servedCustomers) { this.servedCustomers = servedCustomers; }
    
    public double getTotalBusyTime() { return totalBusyTime; }
    public void setTotalBusyTime(double totalBusyTime) { this.totalBusyTime = totalBusyTime; }
    
    public double getUtilization() { return utilization; }
    public void setUtilization(double utilization) { this.utilization = utilization; }
    
    @Override
    public String toString() {
        return String.format("Station %d (%s): Served=%d, Utilization=%.2f%%",
                stationId, type, servedCustomers, utilization * 100);
    }
}