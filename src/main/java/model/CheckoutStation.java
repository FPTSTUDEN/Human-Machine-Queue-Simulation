package model;

import java.util.PriorityQueue;

/**
 * Represents one checkout service point.
 * A station can be either a human cashier or a self-checkout kiosk.
 * It manages its own waiting queue and current customer.
 */

public class CheckoutStation {

    private final int id;
    private final CheckoutType type;

    private final PriorityQueue<Customer> customerQueue;

    private Customer currentCustomer;
    private boolean busy;

    private double totalBusyTime;
    private int servedCustomerCount;

    public CheckoutStation(int id, CheckoutType type) {
        this.id = id;
        this.type = type;
        this.customerQueue = new PriorityQueue<>();
        this.busy = false;
        this.totalBusyTime = 0;
        this.servedCustomerCount = 0;
    }

    public int getId() {
        return id;
    }

    public CheckoutType getType() {
        return type;
    }

    public boolean isBusy() {
        return busy;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public int getQueueLength() {
        return customerQueue.size();
    }

    public double getTotalBusyTime() {
        return totalBusyTime;
    }

    public int getServedCustomerCount() {
        return servedCustomerCount;
    }

    /**
     * Returns the total number of customers at this station
     * including both waiting and currently being served.
     */
    public int getTotalCustomersIncludingCurrent() {
        return customerQueue.size() + (busy ? 1 : 0);
    }

    /**
     * Checks if this station can accept a new customer
     * based on the maximum allowed queue length.
     */
    public boolean canAcceptCustomer(int maxQueueLength) {
        return getTotalCustomersIncludingCurrent() <= maxQueueLength;
    }

    /**
     * Calculates the estimated wait time for a new customer
     * based on current queue length and average service time.
     */
    public double calculateEstimatedWaitTime(double avgServiceTime) {
        return getTotalCustomersIncludingCurrent() * avgServiceTime;
    }

    /**
     * Adds a customer to this station's waiting queue.
     */
    public void addCustomer(Customer customer) {
        customer.setCheckoutType(type);
        customerQueue.add(customer);
    }

    /**
     * Returns the next customer waiting in the queue.
     */
    public Customer getNextWaitingCustomer() {
        return customerQueue.poll();
    }

    /**
     * Starts serving the specified customer.
     */
    public void startService(Customer customer, double currentTime) {
        currentCustomer = customer;
        currentCustomer.setServiceStartTime(currentTime);
        busy = true;
    }

    /**
     * Completes the current customer's service and returns the completed customer.
     */
    public Customer completeCurrentService(double currentTime) {
        if (currentCustomer == null) {
            return null;
        }

        Customer completed = currentCustomer;
        completed.setDepartureTime(currentTime);

        totalBusyTime += completed.getServiceTime();
        servedCustomerCount++;

        currentCustomer = null;
        busy = false;
        
        return completed;
    }

    /**
     * Gets the next customer from the queue and starts their service.
     * Returns true if a customer was started, false otherwise.
     */
    public boolean startNextCustomer(double currentTime) {
        Customer nextCustomer = getNextWaitingCustomer();
        if (nextCustomer != null) {
            startService(nextCustomer, currentTime);
            return true;
        }
        return false;
    }

    /**
     * Returns the completion time of the current customer's service.
     */
    public double getCurrentCompletionTime() {
        if (!busy || currentCustomer == null) {
            return Double.MAX_VALUE;
        }
        return currentCustomer.getServiceStartTime() + currentCustomer.getServiceTime();
    }

    /**
     * Calculates this station's utilization over the simulation duration.
     */
    public double getUtilization(double simulationDuration) {
        return simulationDuration > 0 ? totalBusyTime / simulationDuration : 0;
    }

    /**
     * Checks if there are any customers waiting in the queue.
     */
    public boolean hasWaitingCustomers() {
        return !customerQueue.isEmpty();
    }

    /**
     * Gets the next customer from the queue without removing them.
     */
    public Customer peekNextCustomer() {
        return customerQueue.peek();
    }
}