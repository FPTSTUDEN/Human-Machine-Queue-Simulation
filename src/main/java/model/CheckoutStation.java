package model;

import java.util.PriorityQueue;
import java.util.Random;

/**
 * Represents one checkout service point.
 * A station can be either a human cashier or a self-checkout kiosk.
 * It manages its own waiting queue and current customer.
 */
public class CheckoutStation implements Comparable<CheckoutStation> {

    private final int id;
    private final CheckoutType type;
    private final double[] serviceTimes;
    private final Random random;

    private final PriorityQueue<Customer> customerQueue;

    private Customer currentCustomer;
    private boolean busy;

    private double totalBusyTime;
    private int servedCustomerCount;

    public CheckoutStation(int id, CheckoutType type, double[] serviceTimes) {
        this.id = id;
        this.type = type;
        this.serviceTimes = serviceTimes;
        this.random = new Random();
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
     * Picks a random service time from the service times array
     */
    private double getRandomServiceTime() {
        if (serviceTimes.length == 0) {
            return 0;
        }
        int index = random.nextInt(serviceTimes.length);
        return serviceTimes[index];
    }

    /**
     * Adds a customer to this station's waiting queue.
     */
    public void addCustomer(Customer customer) {
        customer.setCheckoutType(type);
        
        // Assign a random service time from the station's service times
        double serviceTime = getRandomServiceTime();
        customer.setServiceTime(serviceTime);
        
        customerQueue.add(customer);
    }

    /**
     * Returns the next customer waiting in the queue without removing them
     */
    public Customer peekNextCustomer() {
        return customerQueue.peek();
    }

    /**
     * Returns the next customer waiting in the queue and removes them
     */
    public Customer getNextCustomer() {
        return customerQueue.poll();
    }

    /**
     * Starts serving the specified customer.
     */
    public void startService(Customer customer, double currentTime) {
        if (customer == null) {
            return;
        }
        
        currentCustomer = customer;
        currentCustomer.setServiceStartTime(currentTime);
        busy = true;
    }

    /**
     * Starts serving the next customer in the queue
     */
    public boolean startNextCustomer(double currentTime) {
        Customer next = getNextCustomer();
        if (next == null) {
            return false;
        }
        
        startService(next, currentTime);
        return true;
    }

    /**
     * Completes the current customer's service
     */
    public void completeService(double currentTime) {
        if (currentCustomer == null) {
            return;
        }

        // Calculate departure time based on service start time + service time
        double departureTime = currentCustomer.getServiceStartTime() + currentCustomer.getServiceTime();
        currentCustomer.setDepartureTime(departureTime);

        totalBusyTime += currentCustomer.getServiceTime();
        servedCustomerCount++;

        currentCustomer = null;
        busy = false;
    }

    /**
     * Checks if the station has customers waiting in queue
     */
    public boolean hasWaitingCustomers() {
        return !customerQueue.isEmpty();
    }

    /**
     * Gets the total number of customers in the station (queue + currently served)
     */
    public int getTotalCustomers() {
        return customerQueue.size() + (busy ? 1 : 0);
    }

    /**
     * Removes the next customer from the queue without serving them (for abandonment)
     */
    public Customer removeNextCustomer() {
        return customerQueue.poll();
    }

    @Override
    public String toString() {
        return String.format("CheckoutStation{id=%d, type=%s, busy=%b, queueLength=%d, served=%d, totalBusyTime=%.2f}",
                id, type, busy, getQueueLength(), servedCustomerCount, totalBusyTime);
    }

    @Override
    public int compareTo(CheckoutStation other) {
        // Compare based on queue length + is busy (to prioritize stations with fewer customers)
        return Integer.compare(this.getTotalCustomers(), other.getTotalCustomers());
    }
}