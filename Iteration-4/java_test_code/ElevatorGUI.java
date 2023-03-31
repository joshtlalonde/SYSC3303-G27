import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.io.*;

public class ElevatorGUI extends JFrame{
	
	
	JSlider[] sliders;
	JLabel[] elevatorNumbers;
	JLabel[] elevatorStates;
	JLabel[] numRequests;
	
	
	// create the frame and initialize the GUI
	public ElevatorGUI(int numElevators, int numFloors) {
		
		//Initilization
		JFrame frame = new JFrame();
		GridBagLayout GBLayout = new GridBagLayout();
		GridBagConstraints GBConstraints = new GridBagConstraints();
		JPanel Background = new JPanel();
		
		//Frame setup
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 450, 300);
		
		
		//Background Setup
		Background.setForeground(new Color(193, 193, 255));
		Background.setBackground(new Color(193, 193, 255));
		Background.setBorder(new EmptyBorder(5,5,5,5));
		Background.setLayout(GBLayout); 
		
		frame.setContentPane(Background); 
		
		
		sliders = new JSlider[numElevators];
		elevatorNumbers = new JLabel[numElevators];
		elevatorStates = new JLabel[numElevators];
		numRequests = new JLabel[numElevators];
		
		for(int i = 0; i < numElevators; i++) {
			sliders[i] = new JSlider();
			sliders[i].setValue(1); 
			sliders[i].setOrientation(SwingConstants.VERTICAL);
			sliders[i].setMinimum(1); 
			sliders[i].setMaximum(numFloors); 
			sliders[i].setMinorTickSpacing(0);
			sliders[i].setMajorTickSpacing(1); 
			sliders[i].setPaintTicks(false); 
			sliders[i].setPaintLabels(true);
			GBConstraints.fill = GridBagConstraints.HORIZONTAL;
			GBConstraints.gridx = i;
	        GBConstraints.gridy = 0;
	        Background.add(sliders[i], GBConstraints); // add the slider to the content pane
	        
	       
	        //create a lable under the slider which say the elevators name
	        elevatorNumbers[i] = new JLabel("Elevator " + Integer.toString(1+i));
			// format where the label will go (next section in horizontal row 1)
			GBConstraints.fill = GridBagConstraints.HORIZONTAL;
			GBConstraints.gridx = i;
	        GBConstraints.gridy = 1;
	        Background.add(elevatorNumbers[i], GBConstraints);
			
			//create a lable under the slider which say the elevators current state
			elevatorStates[i] = new JLabel("State: IDLE         ");  
			// format where the label will go (next section in horizontal row 2)
			GBConstraints.fill = GridBagConstraints.HORIZONTAL;
			GBConstraints.gridx = i;
	        GBConstraints.gridy = 2;
	        Background.add(elevatorStates[i], GBConstraints);
			
			//create a lable under the slider which say the elevators current # of requests
			numRequests[i] = new JLabel("# Requests: 0       ");
			// format where the label will go (next section in horizontal row 3)
			GBConstraints.fill = GridBagConstraints.HORIZONTAL;
			GBConstraints.gridx = i;
	        GBConstraints.gridy = 3;
	        Background.add(numRequests[i], GBConstraints);
			
		}
		   
		frame.setVisible(true);
	}
	
	public void setSlider(int elevatorNum, int floorNum) {
		sliders[elevatorNum-1].setValue(floorNum);
	}
	
	public void setState(int elevatorNum , Elevator_State currentState) {		
		String stateName = "";
		// depending on the current state the elevator is in then we set the message to be displayed in the state name label accordingly
		switch(currentState) {
			case IDLE:
				stateName = "IDLE         ";
				break;
			case MOVING_UP:
				stateName = "MOVING UP    ";
				break;
			case MOVING_DOWN:
				stateName = "MOVING DOWN  ";
				break;
			case DOOR_OPEN:
				stateName = "DOOR OPEN    ";
				break;
			case DOOR_CLOSE:
				stateName = "DOOR CLOSED  ";
				break;
			case STOPPED:
				stateName = "ARRIVED      ";
				break;
		}
		// update the elevator's state label
		elevatorStates[elevatorNum-1].setText("State: " + stateName);
	}
	public void setNumRequests(int elevatorNum, int newNumberRequests) {
		// update the elevator's state label
		numRequests[elevatorNum-1].setText("# Requests: " + newNumberRequests + "       ");
	}
}


