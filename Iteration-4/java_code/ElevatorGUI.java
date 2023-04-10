import javax.swing.*;
import java.awt.*;

public class ElevatorGUI extends JFrame {

    private static JLabel[] elevatorCurrentFloor;
    private static JLabel[] elevatorDestinationFloor;
    private static JLabel[] elevatorCurrentStatus;
    private static JLabel[] elevatorPassengers;
    private static ElevatorPanel[] elevatorPanels;

    public ElevatorGUI() {
        // Set up the main window
        super("Elevator GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new GridLayout(2, 4));

        // Create the elevator status panels
        elevatorCurrentFloor = new JLabel[4];
        elevatorDestinationFloor = new JLabel[4];
        elevatorCurrentStatus = new JLabel[4];
        elevatorPassengers = new JLabel[4];
        JPanel[] elevatorStatusPanels = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            elevatorCurrentFloor[i] = new JLabel("Elevator" + (i + 1) + " current floor: 0");
            elevatorDestinationFloor[i] = new JLabel("Elevator" + (i + 1) + " destination floor: 0");
            elevatorCurrentStatus[i] = new JLabel("Elevator" + (i + 1)  + " state: IDLE");
            elevatorPassengers[i] = new JLabel("Elevator" + (i + 1)  + " has 0 passengers");
            elevatorStatusPanels[i] = new JPanel();
            elevatorStatusPanels[i].setLayout(new GridLayout(4, 1));
            elevatorStatusPanels[i].add(elevatorCurrentFloor[i]);
            elevatorStatusPanels[i].add(elevatorDestinationFloor[i]);
            elevatorStatusPanels[i].add(elevatorCurrentStatus[i]);
            elevatorStatusPanels[i].add(elevatorPassengers[i]);
            add(elevatorStatusPanels[i]);
        }

        // Create the elevator panels
        elevatorPanels = new ElevatorPanel[4];
        for (int i = 0; i < 4; i++) {
            elevatorPanels[i] = new ElevatorPanel();
            add(elevatorPanels[i]);
        }

        // Show the GUI
        setVisible(true);
    }

    public void updateStatus(int elevatorId, int currentFloor, String state, int numPassengers, int destinationFloor) {
        elevatorCurrentFloor[elevatorId].setText("Elevator" + (elevatorId + 1) + " current floor: " + currentFloor);
        elevatorDestinationFloor[elevatorId].setText("Elevator" + (elevatorId + 1) + " destination floor: " + destinationFloor);
        elevatorCurrentStatus[elevatorId].setText("Elevator" + (elevatorId + 1)  + " state: " + state);
        elevatorPassengers[elevatorId].setText("Elevator" + (elevatorId + 1)  + " has " + numPassengers + " passengers");
        elevatorPanels[elevatorId].setCurrentFloor(currentFloor);
        elevatorPanels[elevatorId].repaint();
    }

    public int getCurrentFloor(int elevatorId) {
        // Your elevator logic to get the current floor of the elevator with ID elevatorId goes here
    	return elevatorPanels[elevatorId].currentFloor;
    }

    public int getDestinationFloor(int elevatorId) {
    	return elevatorPanels[elevatorId].destinationFloor;
    }

    public class ElevatorPanel extends JPanel {
        private int currentFloor = 0;
        private int destinationFloor = 5;
        
        public void setCurrentFloor(int currentFloor) {
            this.currentFloor = currentFloor;
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw the elevator
            g.setColor(Color.GRAY);
            g.fillRect(50 + 200 * (getWidth() / 800), getHeight() - currentFloor * (getHeight() / 21) - 20, 100, 20);
            g.setColor(Color.BLACK);
            g.drawRect(50 + 200 * (getWidth() / 800), getHeight() - currentFloor * (getHeight() / 21) - 20, 100, 20);

            // Draw the floors
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i <= 21; i++) {
                g.drawString(Integer.toString(i), 5 + 200 * (getWidth() / 800), getHeight() - i * (getHeight() / 21) + 5);
                g.drawLine(50 + 200 * (getWidth() / 800), getHeight() - i * (getHeight() / 21), 150 + 200 * (getWidth() / 800), getHeight() - i * (getHeight() / 21));
            }
        }
    }
}
