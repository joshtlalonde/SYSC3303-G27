import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Floor implements Runnable {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
	
	private DirectionLamp directionLamp; // direction lamp for the floor	
	private ArrayList<FloorButton> floorButton = new ArrayList<FloorButton>(); // Holds the buttons for each of the floors (up and down)
	private DatagramSocket sendReceiveSocket;
	
	/** Constructor for Floor */
	public Floor(DirectionLamp directionLamp) {
		this.directionLamp = directionLamp;

		// Create as many buttons as there are floors 
		for (int i = 0; i < NUMBER_OF_FLOORS; i++) {
			floorButton.add(new FloorButton(i));
		}

		// Create Datagram Socket on random port
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Failed to create Datagram Socket: " + e);
			e.printStackTrace();
		}
	}
	
	// Converts a line from a csv file into the appropriate types of UserInput
	public UserInput fileToUser(String line) throws ParseException {
		String[] words = line.split(",");
		
		
		words[0] = words[0].replace("\uFEFF", ""); // remove BOM character if present
		Date t = dateFormatter.parse(words[0]);
		int f = Integer.parseInt(words[1]);
		Boolean fb = words[2].toLowerCase().equals("up") ? true : false;
		int cb = Integer.parseInt(words[3]);

		// Create new user
		return new UserInput(t, f, fb, cb);
	}

	/** Turns on the button for the specific floor and the direction */
	public void buttonPress(boolean floorButtonUp, int floor) {
		// Activate the correct button depending on user input
		if (floorButtonUp) {
			// Get the button of the floor that it was pressed on
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floor) {
					// Set the button to be Up and turn it on
					button.pressUp();
					break;
				}
			}
		} else {
			// Get the button of the floor that it was pressed on
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floor) {
					// Set the button to be Down and turn it on
					button.pressDown();
					break;
				}
			}
		}
	}

	/** Sends request to the Scheduler */
	private void sendFloorRequest(UserInput userInput) {
		// Create FloorPacket
		FloorPacket floorPacket = new FloorPacket(userInput.getFloor(), userInput.getTime(), userInput.getFloorButtonUp(), userInput.getCarButton());

		// Send FloorPacket
		try {
			floorPacket.send(InetAddress.getLocalHost(), 23, sendReceiveSocket);
		} catch (UnknownHostException e) {
			System.out.println("Failed to send FloorPacket: " + e);
			e.printStackTrace();
		}
		
		System.out.println("Floor: Sent floor request to the scheduler: " + userInput);
	}

	/** Waits until Elevator has arrived, let users on, then reset buttons */
	public void elevatorArrival(int floor) {
		// Waits until the request is being serviced by the elevator
//		UserInput userInput = scheduler.respondFloorRequest(floor);
//		System.out.println("Floor: " + userInput + " is being serviced by the elevator");
//
//		// Reset the button depending on what floor it was pressed on
//		for (FloorButton button : floorButton) {
//			if (button.getButtonFloor() == userInput.getFloor()) {
//				button.reset();
//				break;
//			}
//		}
	}

	/** Function to be run on Thread.start() */
	public void run() {
		// Open and read file line-by-line
		BufferedReader reader;
		try {
			String line;
			reader = new BufferedReader(new FileReader("C:\\Users\\Josh's PC\\Documents\\University\\Classes\\SYSC3303\\G27-Project\\Eclipse\\SYSC3303Project\\src\\floor_input.txt"));
			
			while((line = reader.readLine()) != null) {
				// Returns a UserInput object from the next line in the text file
				UserInput userInput;
				try {
					userInput = fileToUser(line);
				} catch (ParseException e) {
					System.out.println("Failed to read date from File: " + e);
					reader.close();
					return;
				}
				System.out.println("Floor: Retreived " + userInput + " from file");

				// Simulate a button press
				buttonPress(userInput.getFloorButtonUp(), userInput.getFloor());

				// Send message to the Scheduler
				sendFloorRequest(userInput);

				/** TODO: Should wait until an elevator ack comes in, or a 5 second timeout occurs */
				// Waits until the request is being serviced by the elevator
				elevatorArrival(userInput.getFloor());

				// Sleep for 1 second
				try {
					Thread.sleep(1000); 
				} catch (InterruptedException e) {
					e.printStackTrace();
         			System.exit(1);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Failed to find the File: " + e);
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
	
    public static void main(String[] args) {
        
        // Create table that all threads will access
		DirectionLamp directionLamp = new DirectionLamp();
        
		// Create Floor Thread
        Thread floor = new Thread(new Floor(directionLamp), "Floor");

        // Start Threads
        floor.start();
    }

	/** Function to be run on Thread.start() */
	// public void run() {
	// 	// Open and read file line-by-line
	// 	BufferedReader reader;
	// 	try {
	// 		String line;
	// 		reader = new BufferedReader(new FileReader("C:\\Users\\Josh's PC\\Documents\\University\\Classes\\SYSC3303\\G27-Project\\Eclipse\\SYSC3303Project\\src\\floor_input.txt"));
			
	// 		while((line = reader.readLine()) != null) {
	// 			UserInput userInput = fileToUser(line);
	// 			System.out.println("Floor: Retreived " + userInput + " from file");

	// 			// Puts the user_input into the scheduler
	// 			scheduler.put(userInput);
	// 			System.out.println("Floor: Put " + userInput + " into the scheduler");

	// 			// Sleep for 1 second
	// 			try {
	// 				Thread.sleep(1000); 
	// 			} catch (InterruptedException e) {
	// 				e.printStackTrace();
    //      				System.exit(1);
	// 			}
	// 		}
	// 	} catch (FileNotFoundException e) {
	// 		System.out.println("Failed to open File: " + e);
	// 		return;
	// 	} catch (IOException e) {
	// 		System.out.println("Failed to read File: " + e);
	// 		return;
	// 	}
		
	// 	// Close reader
	// 	try {
	// 		reader.close();
	// 	} catch (IOException e) {
	// 		System.out.print("IO Exception: likely:");
	// 		System.out.println("Failed to close File: " + e);
    //      		System.exit(1);
	// 	}
	// }
}

/** Used to get the information for simulating a user from a text file */
class UserInput{
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
	private Date time; // Timestamp of when button was clicked
	private int floor; // Floor that button was clicked on
	private boolean floorButtonUp; // Direction that user wants to go
	private int carButton; // Button that was clicked in elevator to decide destination floor
	
	
	//Making the constructor for the user input class
	public UserInput(Date time, int floor, boolean floorButtonUp, int carButton) {
		this.time = time;
		this.floor = floor;
		this.floorButtonUp= floorButtonUp;
		this.carButton = carButton;
	}
	
	@Override
	public String toString() {
		return "{time: " + dateFormatter.format(time) + ", floor: " + floor + ", floor_button: " + floorButtonUp + ", car_button: " + carButton + "}";
	}
	
	//Getting the data from the user input
	public Date getTime() {
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
	private boolean upButtonState = false; // Defines the state of the up button, On or Off
	private FloorLamp upButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the up button
	private boolean downButtonState = false; // Defines the state of the down button, On or Off
	private FloorLamp downButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the down button
	private int buttonFloor = 0;

	public FloorButton(int floor) {
		this.buttonFloor = floor;
	}

	// Sets the up button state On and turns on the up Lamp
	public void pressUp() {
		upButtonState = true;
		upButtonLamp.turnOn();
		System.out.println("FloorButton: Floor Button pressed on floor " + buttonFloor + " in Up direction");
	}

	// Sets the down button state On and turns on the down Lamp
	public void pressDown() {
		downButtonState = true;
		downButtonLamp.turnOn();
		System.out.println("FloorButton: Floor Button pressed on floor " + buttonFloor + " in Down direction");
	}

	// Sets the buttons state to Off and turns off the Lamps
	public void reset() {
		upButtonState = false;
		downButtonState = false;
		upButtonLamp.turnOff();
		downButtonLamp.turnOn();
	}

	// Returns the current state of the up Button
	public boolean getUpButtonState() {
		return upButtonState;
	}

	// Returns the current state of the down Button
	public boolean getDownButtonState() {
		return downButtonState;
	}

	// Returns the floor of the Button
	public int getButtonFloor() {
		return buttonFloor;
	}
}

/** Used to simulate the lamp for the floor buttons */
class FloorLamp {
	private boolean lampState = false; // Defines the state of the lamp, On or Off

	// Sets lamp state to On
	public void turnOn() {
		System.out.println("FloorLamp: Floor Lamp turned on");
		lampState = true;
	}

	// Sets lamp state to Off
	public void turnOff() {
		System.out.println("FloorLamp: Floor Lamp turned off");
		lampState = false;
	}

	// Returns the lamp state
	public boolean getLampState() {
		return lampState;
	}
}

class DirectionLamp {
	private int floor = 0;
	private boolean directionUp = false;

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public boolean getDirectionUp() {
		return directionUp;
	}

	public void setDirectionUp(boolean direction) {
		this.directionUp = direction;
	}
}
