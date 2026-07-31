package org.example;

import org.example.model.SimulationConfig;
import org.example.model.SimulationResult;
import org.example.model.SupermarketModel;

public class Main {

    public static void main(String[] args) {

        SupermarketModel model = new SupermarketModel();

        SimulationConfig config =
                new SimulationConfig(
                        2,
                        1,
                        20,
                        3,
                        4,
                        60,
                        5
                );

        SimulationResult result =
                model.runSimulation(config);

        System.out.println(
                "Number of stations: "
                        + model.getCheckoutStations().size()
        );

        System.out.println(
                "Customers served: "
                        + result.getServedCustomers()
        );

        System.out.println(
                "Average waiting time: "
                        + result.getAverageWaitingTime()
        );
    }
}