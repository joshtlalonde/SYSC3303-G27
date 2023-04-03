import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.io.*;

public class ElevatorGUI extends JFrame{
	
	
	JLabel[] elevatorNumbers;
	JLabel[] elevatorStates;
	JLabel[] numRequests;
	Elevator_GUI_Image[] ELE;
	JPanel LeftPanel;
	JPanel RightPanel;
	JPanel[] elevatorBackground;
	
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
		
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setSize(1200, 1200);
		frame.getContentPane().setBackground(Color.cyan); 
		//frame.setLayout(new GridLayout(21, 6));
		//frame.setBounds(500, 500, 550, 500);
		
		
		ELE = new Elevator_GUI_Image[numElevators];
		elevatorBackground = new JPanel[numElevators];
		int spacingWidth = 200;

		for(int i = 0; i < numElevators; i++) {
			ELE[i] = new Elevator_GUI_Image(Color.gray, i);
			elevatorBackground[i] = new JPanel(new GridLayout(21,1, 100, 0));
			elevatorBackground[i].add(ELE[i], new GridBagConstraints());
			elevatorBackground[i].setBounds(spacingWidth, 0, 180, 1200);
			spacingWidth+=200;
			frame.add(elevatorBackground[i]);
		}
	
		LeftPanel = new JPanel(new BorderLayout());
		RightPanel = new JPanel();
		LeftPanel.setBackground(Color.black);
		//LeftPanel.setPreferredSize(new Dimension(200, 1200));
		
		//LeftPanel.setSize(getPreferredSize());
		RightPanel.setBackground(Color.black);
		LeftPanel.setBounds(0, 0, 200, 1200);
		RightPanel.setBounds(1000, 0, 200, 1200);
		frame.add(LeftPanel);
		frame.add(RightPanel);
		
		int hincrement = 90;
		int hposition = 200;
		int vposition = 57;
		for(int x = 0; x < 21; x++) {
			JLabel label = new JLabel();
			label.setText(String.valueOf(x));
			label.setFont(new Font("MV Boli", Font.PLAIN, 40));
			label.setBackground(Color.red);
			for(int y = 1; y < 5; y++) {
				hposition += (y * hincrement);
				label.setLocation(hposition, vposition);
				LeftPanel.add(label);
			}
			vposition+= 57;
		}
		
		frame.setVisible(true);
		
		//RightPanel.setSize(getPreferredSize());
		
		/*
		JPanel Background = new JPanel();
		
			
		//Frame setup

			
		//Background Setup
		Background.setBackground(Color.cyan);
		Background.setBorder(new EmptyBorder(0,0,1200,1200));
		
		
		Background.add(LeftPanel);
		
		
		
		for(int i = 0; i< numElevators; i++) {
			Background.add(elevatorBackground[i]);
		}
		Background.add(RightPanel);*/
		
		
	
		//Add the left and right side panels
		//JPanel leftPanel = new JPanel();
		//JPanel rightPanel = new JPanel();
		//leftPanel.setBackground(Color.white);
		//rightPanel.setBackground(Color.white);
			
		//Add the elevator panels in between the left and right side panels	

	}
	
	
	public void setRectangle() {
		//Can make changes to Elevator Rectangles in here if need to
		RECT_X = 5; 
		RECT_Y = RECT_X; 
		RECT_WIDTH = 5; 
		RECT_HEIGHT = 10;
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
