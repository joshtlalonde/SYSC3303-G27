import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Floor implements Runnable {
	static final String FILENAME = "C:\\Users\\jtbub\\Documents\\University\\Classes\\SYSC 3303\\SYSC3303-G27\\Eclipse\\SYSC3303Project\\src\\floor_input.txt";
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
	private void sendFloorRequest(InetAddress address, int port, UserInput userInput) {
		// Create FloorPacket
		FloorPacket floorPacket = new FloorPacket(userInput.getCurrentFloor(), userInput.getTime(), userInput.getFloorButtonUp(), userInput.getDestinationFloor());

		// Send FloorPacket to scheduler
		floorPacket.send(address, port, sendReceiveSocket, true);

		System.out.println("Floor: Sent floor request to the scheduler: " + userInput);
	}

	/** 
	 * Receive response from the Scheduler
	 * This is just a response ACK packet
	 */
	private void receiveFloorResponse() {
		// Create FloorPacket
		FloorPacket floorPacket = new FloorPacket();

		// Receive FloorPacket from scheduler
		System.out.println("Floor: Waiting for Scheduler Response...");
		floorPacket.receive(sendReceiveSocket);

		// Convert FloorPacket to UserInput
		UserInput userInput = new UserInput();
		userInput.convertPacket(floorPacket);
		
		System.out.println("Floor: Received response from Scheduler: " + userInput);
	}

	/** Scheduler sent a message saying that an elevator arraived at this floor */
	public void elevatorArrival(boolean elevatorDirection, int floor) {
		// Turn off button for direction
		// Should be similar to buttonPress()
		// Activate the correct button depending on user input
		if (elevatorDirection) {
		
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floor) {
					// Set the button to be Up and turn it on
					button.resetUp();
					break;
				}
			}
		} else {
	
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floor) {
					// Set the button to be Down and turn it on
					button.resetDown();
					break;
				}
			}
		}
	}

	/** Function to be run on Thread.start() */
	public void run() {
		// Open and read file line-by-line
		BufferedReader reader;
		try {
			String line;
			reader = new BufferedReader(new FileReader(FILENAME));
			
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

				/** Simulate a button press */
				this.buttonPress(userInput.getFloorButtonUp(), userInput.getCurrentFloor()); // TODO: Change FloorButtonUp to FloorButtonDirection and change carButton to floorDestination

				/** Send message to the Scheduler */
				this.sendFloorRequest(InetAddress.getLocalHost(), 69, userInput);

				/** Receive Floor response from Scheduler */
				this.receiveFloorResponse();

				/** TODO: Should wait until an FloorPacket to comes in, or a 5 second timeout occurs
				 * This FloorPacket will say that the scheduler is picking people up on a specific floor
				 */
				// Waits until the request is being serviced by the elevator
				this.elevatorArrival(userInput.getFloorButtonUp(), userInput.getCurrentFloor());

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
}

/** Used to get the information for simulating a user from a text file */
class UserInput{
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
	private Date time; // Timestamp of when button was clicked
	private int currentFloor; // Floor that button was clicked on
	private boolean floorButtonUp; // Direction that user wants to go
	private int destinationFloor; // Button that was clicked in elevator to decide destination floor
	
	public UserInput(Date time, int floor, boolean floorButtonUp, int carButton) {
		this.time = time;
		this.currentFloor = floor;
		this.floorButtonUp= floorButtonUp;
		this.destinationFloor = carButton;
	}

	/** Default Constructor */
	public UserInput() {
		this.time = new Date();
		this.currentFloor = 0;
		this.floorButtonUp= false;
		this.destinationFloor = 0;
	}

	/**
	 * Converts a FloorPacket to a UserInput
	 * 
	 * @param floorPacket the floorpacket to be converted
	 */
	public void convertPacket(FloorPacket floorPacket) {
		this.time = floorPacket.getTime();
		this.currentFloor = floorPacket.getFloor();
		this.floorButtonUp = floorPacket.getDirectionUp();
		this.destinationFloor = floorPacket.getDestinationFloor();
	}
	
	@Override
	public String toString() {
		return "{time: " + dateFormatter.format(time) + ", floor: " + currentFloor + ", floor_button: " + floorButtonUp + ", car_button: " + destinationFloor + "}";
	}
	
	//Getting the data from the user input
	public Date getTime() {
		return time;
	}
	public int getCurrentFloor() {
		return currentFloor;
	}
	public boolean getFloorButtonUp() {
		return floorButtonUp;
	}
	public int getDestinationFloor() {
		return destinationFloor;
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

	// Turns off the Floor's Up button
	public void resetUp() {
		upButtonState = false;
		upButtonLamp.turnOff();
	}

	// Turns off the Floor's Down button
	public void resetDown() {
		downButtonState = false;
		downButtonLamp.turnOff();
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
  
