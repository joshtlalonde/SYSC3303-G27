import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.io.*;

public class ElevatorGUI extends JFrame{
	
	
	JSlider[] sliders;
	JLabel[] elevatorNumbers;
	JLabel[] elevatorStates;
	JLabel[] numRequests;
	Elevator_GUI_Image[] ELE;
	JPanel[] Backgrounds;
	
	private int RECT_X;
	private int RECT_Y;
	private int RECT_HEIGHT;
	private int RECT_WIDTH;
	
	
	// create the frame and initialize the GUI
	public ElevatorGUI(int numElevators, int numFloors) {
		
		//Backgrounds[0] = new JPanel();
		//Initilization
		/* setRectangle();
		ELE = new Elevator_GUI_Image[numElevators];
		Backgrounds = new JPanel[numElevators+2];
		Backgrounds[0] = new JPanel();
		for(int i = 0; i < numElevators; i++) {
			ELE[i] = new Elevator_GUI_Image(Color.gray, i);
			Backgrounds[i+1] = new JPanel();
			Backgrounds[i+1].add(ELE[i]);
		}
		Backgrounds[1 + numElevators] = new JPanel();
		JFrame frame = new JFrame();
		GridBagLayout GBLayout = new GridBagLayout();
		GridBagConstraints GBConstraints = new GridBagConstraints();
		
		
		//Frame setup
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 450, 300);
		
		
		//Background Setup
		Backgrounds[0].setForeground(new Color(193, 193, 255));
		Backgrounds[0].setBackground(new Color(193, 193, 255));
		Backgrounds[0].setBorder(new EmptyBorder(5,5,5,5));
		Backgrounds[0].setLayout(GBLayout);
		
		frame.setContentPane(Backgrounds[0]); 
		
		elevatorNumbers = new JLabel[numElevators];
		elevatorStates = new JLabel[numElevators];
		numRequests = new JLabel[numElevators];
		   
		frame.setVisible(true);
		*/
		//Backgrounds[0] = new JPanel();
		//Initilization
		setRectangle();
		ELE = new Elevator_GUI_Image[numElevators];
		Backgrounds = new JPanel[numElevators+2];
		Backgrounds[0] = new JPanel();
		for(int i = 0; i < numElevators; i++) {
			ELE[i] = new Elevator_GUI_Image(Color.gray, i);
			Backgrounds[i+1] = new JPanel();
			Backgrounds[i+1].add(ELE[i]);
		}
		Backgrounds[1 + numElevators] = new JPanel();
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
		
		
		for(int i = 0; i< (numElevators + 2); i++) {
			Background.add(Backgrounds[i]);
		}
		
		
	
		//Add the left and right side panels
		//JPanel leftPanel = new JPanel();
		//JPanel rightPanel = new JPanel();
		//leftPanel.setBackground(Color.white);
		//rightPanel.setBackground(Color.white);
			
		//Add the elevator panels in between the left and right side panels		
		frame.setContentPane(Background);  
		frame.setVisible(true);
	}
	
	
	
	
	public void setSlider(int elevatorNum, int floorNum) {
		sliders[elevatorNum-1].setValue(floorNum);
	}
	
	public void setRectangle() {
		//Can make changes to Elevator Rectangles in here if need to
		RECT_X = 20; 
		RECT_Y = RECT_X; 
		RECT_WIDTH = 50; 
		RECT_HEIGHT = 100;
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
	
	public static void main(String[] args) {
		ElevatorGUI GUI = new ElevatorGUI(4,20);
		
	}
	
}
