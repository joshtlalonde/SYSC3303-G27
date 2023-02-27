import java.io.*;
import java.util.*;

public class Floor implements Runnable {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building

	private Scheduler scheduler; // Scheduler that the Floor is associated to
	private ArrayList<FloorButton> floorButton = new ArrayList<FloorButton>(); // Holds the buttons for each of the floors (up and down)
	private DirectionLamp directionLamp; // direction lamp for the floor	
	
	/** Constructor for Floor */
	public Floor(Scheduler scheduler, DirectionLamp directionLamp) {
		this.scheduler = scheduler;	
		this.directionLamp = directionLamp;

		// Create as many buttons as there are floors 
		for (int i = 0; i < NUMBER_OF_FLOORS; i++) {
			floorButton.add(new FloorButton(i));
		}
	}
	
	// Converts a line from a csv file into the appropriate types of UserInput
	public UserInput fileToUser(String line) {
		String[] words = line.split(",");
		
		//LocalTime t = LocalTime.parse(words[0], DateTimeFormatter.ISO_LOCAL_TIME);
		String t = words[0];
		int f = Integer.parseInt(words[1]);
		Boolean fb = words[2].toLowerCase().equals("up") ? true : false;
		int cb = Integer.parseInt(words[3]);

		// Create new user
		return new UserInput(t, f, fb, cb);
	}

	/** Defines what is done when a button is pressed */
	public void buttonPress(UserInput userInput) {
		// Activate the correct button depending on user input
		if (userInput.getFloorButtonUp()) {
			// Get the button of the floor that it was pressed on
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == userInput.getFloor()) {
					// Set the button to be Up and turn it on
					button.pressUp();
					break;
				}
			}
		} else {
			// Get the button of the floor that it was pressed on
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == userInput.getFloor()) {
					// Set the button to be Down and turn it on
					button.pressDown();
					break;
				}
			}
		}

		// Send request to scheduler
		sendRequest(userInput);
	}

	/** Sends request to the Scheduler */
	private void sendRequest(UserInput userInput) {
		// Puts the user_input into the scheduler
		scheduler.addFloorRequest(userInput);
		System.out.println("Floor: Added " + userInput + " into the scheduler");
	}

	/** Waits until Elevator has arrived, let users on, then reset buttons */
	public void elevatorArrival() {
		// Waits until the request is being serviced by the elevator
		UserInput userInput = scheduler.respondFloorRequest();
		System.out.println("Floor: " + userInput + " is being serviced by the elevator");

		// Reset the button depending on what floor it was pressed on
		for (FloorButton button : floorButton) {
			if (button.getButtonFloor() == userInput.getFloor()) {
				button.reset();
				break;
			}
		}
	}

	/** Function to be run on Thread.start() */
	public void run() {
		// Open and read file line-by-line
		BufferedReader reader;
		try {
			String line;
			reader = new BufferedReader(new FileReader("../floor_input.txt"));
			
			while((line = reader.readLine()) != null) {
				// Returns a UserInput object from the next line in the text file
				UserInput userInput = fileToUser(line);
				System.out.println("Floor: Retreived " + userInput + " from file");

				// Puts the user input into the scheduler
				sendRequest(userInput);

				// Waits until the request is being serviced by the elevator
				elevatorArrival();



				// Sleep for 1 second
				try {
					Thread.sleep(1000); 
				} catch (InterruptedException e) {
					e.printStackTrace();
         			System.exit(1);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Failed to open File: " + e);
			return;
		} catch (IOException e) {
			System.out.println("Failed to read File: " + e);
			return;
		}
		
		// Close reader
		try {
			reader.close();
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Failed to close File: " + e);
         		System.exit(1);
		}
	}
}

/** Used to get the information for simulating a user from a text file */
class UserInput{
	//private LocalTime time;
	private String time; // Timestamp of when button was clicked
	private int floor; // Floor that button was clicked on
	private boolean floorButtonUp; // Direction that user wants to go
	private int carButton; // Button that was clicked in elevator to decide destination floor
	
	
	//Making the constructor for the user input class
	public UserInput(String time, int floor, boolean floorButtonUp, int carButton) {
		this.time = time;
		this.floor = floor;
		this.floorButtonUp= floorButtonUp;
		this.carButton = carButton;
	}
	
	@Override
	public String toString() {
		return "{time: " + time + ", floor: " + floor + ", floor_button: " + floorButtonUp + ", car_button: " + carButton + "}";
	}
	
	//Getting the data from the user input
	public String getTime() {
		return time;
	}
	public int getFloor() {
		return floor;
	}
	public boolean getFloorButtonUp() {
		return floorButtonUp;
	}
	public int getCarButton() {
		return carButton;
	}
	
}

/** Used to simulate a button on the floor
 *  Each button has a floor number associated to it
 *  Each button has a lamp associated to it
 */
class FloorButton {
	private boolean buttonState; // Defines the state of the button, On or Off
	private FloorLamp buttonLamp; // Used to hold the associated lamp of the button
	private boolean buttonDirectionUp;
	private int buttonFloor;

	public FloorButton(int floor) {
		this.buttonFloor = floor;
	}

	// Sets the button state On, the direction to Up, and turns on the Lamp
	public void pressUp() {
		buttonDirectionUp = true;
		buttonState = true;
		buttonLamp.turnOn();
	}

	// Sets the button state On, the direction to Down, and turns on the Lamp
	public void pressDown() {
		buttonDirectionUp = false;
		buttonState = true;
		buttonLamp.turnOn();
	}

	// Sets the button state to Off and turns off the Lamp
	public void reset() {
		buttonState = false;
		buttonLamp.turnOff();
	}

	// Returns the current state of the Button
	public boolean getButtonState() {
		return buttonState;
	}

	// Returns the floor of the Button
	public int getButtonFloor() {
		return buttonFloor;
	}

	// Returns the Button direction that was pressed
	public boolean getButtonDirectionUp() {
		return buttonDirectionUp;
	}
}

/** Used to simulate the lamp for the floor buttons */
class FloorLamp {
	private boolean lampState; // Defines the state of the lamp, On or Off

	// Defines the state of the lamp, On or Off
	public void turnOn() {
		lampState = true;
	}

	// Defines the state of the lamp, On or Off
	public void turnOff() {
		lampState = false;
	}

	// Defines the state of the lamp, On or Off
	public boolean getLampState() {
		return lampState;
	}
}

class DirectionLamp {
	private int floor;

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}
}