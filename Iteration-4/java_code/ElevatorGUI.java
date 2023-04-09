import javax.swing.*;
import java.awt.*;
import java.lang.Math;

public class ElevatorGUI extends JFrame {

    private static JLabel[] elevatorStatusLabel;
    private static JLabel[] elevatorCurrentStatus;
    private static ElevatorPanel[] elevatorPanels;

    public ElevatorGUI() {
        // Set up the main window
        super("Elevator GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new GridLayout(1, 4));

        // Create the elevator status panels
        elevatorStatusLabel = new JLabel[4];
        elevatorCurrentStatus = new JLabel[4];
        JPanel[] elevatorStatusPanels = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            elevatorStatusLabel[i] = new JLabel("Elevator " + (i + 1) + " is on floor 0");
            elevatorCurrentStatus[i] = new JLabel("Moving up");
            elevatorStatusPanels[i] = new JPanel();
            elevatorStatusPanels[i].setLayout(new GridLayout(2, 1));
            elevatorStatusPanels[i].add(elevatorStatusLabel[i]);
            elevatorStatusPanels[i].add(elevatorCurrentStatus[i]);
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

    public void updateStatus(int elevatorId, int currentFloor, String state) {
        elevatorStatusLabel[elevatorId].setText("Elevator " + (elevatorId + 1) + " is on floor " + currentFloor);
        elevatorCurrentStatus[elevatorId].setText(state);
        elevatorPanels[elevatorId].setCurrentFloor(currentFloor);
        elevatorPanels[elevatorId].repaint();
    }

    public void Running() {
        ElevatorGUI guiTestEle = new ElevatorGUI();
        // Call the updateStatus method periodically to update the GUI based on the current state of the elevators
        // Get the current state of each elevator
        for (int i = 0; i < 4; i++) {
        	int currentFloor = getCurrentFloor(i);
            String state = getState(i);
            int destinationFloor = getDestinationFloor(i);

             // Update the GUI for the elevator
                	
             if(currentFloor < destinationFloor) {
            	 currentFloor = currentFloor + 1;
                 state = "Moving Up";
              }
             else if(destinationFloor < currentFloor) {
            	 currentFloor = currentFloor - 1;
            	 state = "Moving Down";
             	}
             else {
            	 setDestinationFloor(i,(int)(Math.random() * 10));
            	 state = "Idle";
            }
        
               guiTestEle.updateStatus(i, currentFloor, state);
            }

            // Wait for a short time before updating again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    public int getCurrentFloor(int elevatorId) {
        // Your elevator logic to get the current floor of the elevator with ID elevatorId goes here
    	return elevatorPanels[elevatorId].currentFloor;
        
    }

    public void setState(int elevatorId, String state) {
    	elevatorPanels[elevatorId].state = state;
    }
    public int getDestinationFloor(int elevatorId) {
    	return elevatorPanels[elevatorId].destinationFloor;
    }
    public  String getState(int elevatorId) {
    	return elevatorPanels[elevatorId].state;
    }
    
    public void setDestinationFloor(int elevatorId, int destinationFloor) {
    		elevatorPanels[elevatorId].destinationFloor = destinationFloor;
    }

    public class ElevatorPanel extends JPanel {
        private int currentFloor = 0;
        private boolean isMovingUp = false;
        private int destinationFloor = 5;
        private String state;
        

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
