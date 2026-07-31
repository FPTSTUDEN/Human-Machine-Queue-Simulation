package model;

import java.util.ArrayList;
import java.util.List;
/**
 * Main Model class of the MVC architecture.
 * Manages customers, checkout stations, and simulation execution.
 */
public class SupermarketModel {

    private final List<CheckoutStation> checkoutStations;
    private int nextCustomerId;

    public SupermarketModel() {
        checkoutStations = new ArrayList<>();
        nextCustomerId = 1;
    }

    /**
     * Creates the required number of human cashier
     * and self-checkout stations.
     *
     * @param humanCashierCount number of human cashier lanes
     * @param selfCheckoutCount number of self-checkout kiosks
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
     *
     * @param arrivalTime customer arrival time
     * @return newly created customer
     */

    public Customer createCustomer(double arrivalTime) {
        Customer customer =
                new Customer(nextCustomerId, arrivalTime);

        nextCustomerId++;
        return customer;
    }

    /**
     * Runs the supermarket simulation.
     *
     * This method will contain the main simulation
     * logic in the next development phase.
     *
     * @param config simulation parameters
     * @return simulation results
     */

    public SimulationResult runSimulation(
            SimulationConfig config) {

        createCheckoutStations(
                config.getHumanCashierCount(),
                config.getSelfCheckoutCount()
        );

        // TODO: Implement event-based simulation logic in the next phase.

        return new SimulationResult(
                0,      // served customers
                0,      // abandoned customers
                0.0,    // average waiting time
                0.0,    // average service time
                0.0,    // average queue length
                0.0,    // maximum queue length
                0.0,    // human utilization
                0.0,    // machine utilization
                0.0     // throughput
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
    }
}