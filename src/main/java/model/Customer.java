package model;

/**
 * Represents a customer in the supermarket checkout simulation.
 * Stores arrival, service, and departure times used to calculate
 * waiting time and service time.
 */
public class Customer implements Comparable<Customer> {

    private final int id;
    private final double arrivalTime;

    private double serviceStartTime;
    private double departureTime;

    private CheckoutType checkoutType;
    private boolean abandoned;

    private double serviceTime;

    public Customer(int id, double arrivalTime, double serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceStartTime = -1;
        this.departureTime = -1;
        this.abandoned = false;
        this.serviceTime = serviceTime;
    }

    public int getId() {
        return id;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getServiceStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(double serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public CheckoutType getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(CheckoutType checkoutType) {
        this.checkoutType = checkoutType;
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void setAbandoned(boolean abandoned) {
        this.abandoned = abandoned;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
    }

    /**
     * Calculates the customer's waiting time.
     */

    public double getWaitingTime() {
        if (serviceStartTime < 0) {
            return 0;
        }

        return serviceStartTime - arrivalTime;
    }

    /**
     * Calculates the total time the customer spends
     * in the system.
     */

    public double getTotalTimeInSystem() {
        if (departureTime < 0) {
            return 0;
        }

        return departureTime - arrivalTime;
    }

    @Override
    public int compareTo(Customer other) {
        // Customers with earlier arrival time have higher priority
        return Double.compare(this.arrivalTime, other.arrivalTime);
    }
}