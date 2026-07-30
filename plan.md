```
+-------------------------------------------------------+
       |                        VIEW                           |
       |  (SimDashboardView.fxml / SimController JavaFX nodes) |
       +-------------------------------------------------------+
           |                                             ^
           | [1] Dispatches User Actions                 | [4] Binds / Listens
           |     (e.g., Sliders, Button Clicks)          |     to Properties
           v                                             |
       +-------------------------------------------------------+
       |                     CONTROLLER                        |
       |                 (SimController.java)                  |
       +-------------------------------------------------------+
           |                                             |
           | [2] Invokes Logic Operations                | [3] Observes Changes
           |     & Schedules Engine Loops                |     via Listeners
           v                                             v
       +-------------------------------------------------------+
       |                        MODEL                          |
       |       (SupermarketModel.java / CheckoutStation)       |
       +-------------------------------------------------------+
```
# Human-Machine-Queue-Simulation

```mermaid
flowchart TD
    A(User Interface) -->C[Main program]
    
    C --> D[(Waiting queue)]
    C --> E[(Waiting queue)]
    C --> F[(Waiting queue)]
    
    D --> G(Human cashier)
    E --> H[Human cashier]
    F --> I[Machine cashier/Self service cashier]
    
    G --> Q[leave]
    H --> Q
    I --> Q 

```