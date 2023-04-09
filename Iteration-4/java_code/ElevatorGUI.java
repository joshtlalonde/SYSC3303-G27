import javax.swing.*;
import java.awt.*;

public class ElevatorGUI extends JFrame {

    private JLabel[] elevatorStatusLabel;
    private JCheckBox[] elevatorDirectionCheckbox;
    private ElevatorPanel[] elevatorPanels;

    public ElevatorGUI() {
        // Set up the main window
        super("Elevator GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new GridLayout(1, 4));

        // Create the elevator status panels
        elevatorStatusLabel = new JLabel[4];
        elevatorDirectionCheckbox = new JCheckBox[4];
        JPanel[] elevatorStatusPanels = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            elevatorStatusLabel[i] = new JLabel("Elevator " + (i + 1) + " is on floor 0");
            elevatorDirectionCheckbox[i] = new JCheckBox("Moving up");
            elevatorStatusPanels[i] = new JPanel();
            elevatorStatusPanels[i].setLayout(new GridLayout(2, 1));
            elevatorStatusPanels[i].add(elevatorStatusLabel[i]);
            elevatorStatusPanels[i].add(elevatorDirectionCheckbox[i]);
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

    public void updateStatus(int elevatorId, int currentFloor, boolean isMovingUp) {
        elevatorStatusLabel[elevatorId].setText("Elevator " + (elevatorId + 1) + " is on floor " + currentFloor);
        elevatorDirectionCheckbox[elevatorId].setSelected(isMovingUp);
        elevatorPanels[elevatorId].setCurrentFloor(currentFloor);
        elevatorPanels[elevatorId].repaint();
    }

    public static void main(String[] args) {
        GUITestEle guiTestEle = new GUITestEle();
        // Call the updateStatus method periodically to update the GUI based on the current state of the elevators
        while (true) {
            // Get the current state of each elevator
            for (int i = 0; i < 4; i++) {
                int currentFloor = getCurrentFloor(i);
                boolean isMovingUp = isMovingUp(i);

                // Update the GUI for the elevator
                guiTestEle.updateStatus(i, currentFloor, isMovingUp);
            }

            // Wait for a short time before updating again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getCurrentFloor(int elevatorId) {
        // Your elevator logic to get the current floor of the elevator with ID elevatorId goes here
        return 0;
    }

    private static boolean isMovingUp(int elevatorId) {
        // Your elevator logic to determine whether the elevator with ID elevatorId is moving up goes here
        return true;
    }

    private class ElevatorPanel extends JPanel {
        private int currentFloor = 0;

        public void setCurrentFloor(int currentFloor) {
            this.currentFloor = currentFloor;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw the elevator
            g.setColor(Color.GRAY);
            g.fillRect(50 + 200 * (getWidth() / 800), getHeight() - currentFloor * (getHeight() / 21) - 40, 100, 40);
            g.setColor(Color.BLACK);
            g.drawRect(50 + 200 * (getWidth() / 800), getHeight() - currentFloor * (getHeight() / 21) - 40, 100, 40);

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
