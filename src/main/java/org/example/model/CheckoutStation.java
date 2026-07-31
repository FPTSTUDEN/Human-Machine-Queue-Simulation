package org.example.model;

import org.example.model.CheckoutType;
import org.example.model.Customer;

import java.util.LinkedList;
import java.util.Queue;
/**
 * Represents one checkout service point.
 * A station can be either a human cashier or a self-checkout kiosk.
 * It manages its own waiting queue and current customer.
 */

public class CheckoutStation {

    private final int id;
    private final CheckoutType type;

    private final Queue<Customer> customerQueue;

    private Customer currentCustomer;
    private boolean busy;

    private double totalBusyTime;
    private int servedCustomerCount;

    public CheckoutStation(int id, CheckoutType type) {
        this.id = id;
        this.type = type;
        this.customerQueue = new LinkedList<>();
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
     * Adds a customer to this station's waiting queue.
     */

    public void addCustomer(Customer customer) {
        customer.setCheckoutType(type);
        customerQueue.add(customer);
    }

    /**
     * Returns the next customer waiting in the queue.
     */

    public Customer getNextCustomer() {
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
     * Completes the current customer's service
     */

    public void completeService(double currentTime) {
        if (currentCustomer == null) {
            return;
        }

        currentCustomer.setDepartureTime(currentTime);

        totalBusyTime += currentCustomer.getServiceTime();
        servedCustomerCount++;

        currentCustomer = null;
        busy = false;
    }
}