import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Floor implements Runnable {
	static final String FILENAME = "C:\\Users\\Josh's PC\\Documents\\University\\Classes\\SYSC3303\\Project\\SYSC3303-G27-main\\SYSC3303-G27\\Iteration-4\\floor_input.txt";
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	private DatagramSocket sendReceiveSocket;

	private ArrayList<FloorButton> floorButton = new ArrayList<FloorButton>(); // Holds the buttons for each of the floors (up and down)
	private ArrayList<UserInput> floorRequests = new ArrayList<UserInput>(); // Holds the list of floorRequests
	
	
	/** Constructor for Floor */
	public Floor() {

		// Create as many buttons as there are floors 
		for (int i = 0; i < NUMBER_OF_FLOORS; i++) {
			floorButton.add(new FloorButton(i));
		}

		// Create Datagram Socket on random port
		try {
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(3000);
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
		Boolean df = words[4].toLowerCase().equals("true") ? true : false;
		Boolean hf = words[5].toLowerCase().equals("true") ? true : false;

		// Create new user
		return new UserInput(t, f, fb, cb, df, hf);
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
		FloorPacket floorPacket = new FloorPacket(userInput.getCurrentFloor(), userInput.getTime(), 
													userInput.getFloorButtonUp(), userInput.getDestinationFloor(), 
													userInput.getDoorFault(), userInput.getHardFault());

		// Send FloorPacket to scheduler
		floorPacket.send(address, port, sendReceiveSocket, true);

		System.out.println("Floor: Sent floor request to the scheduler: " + userInput);
	}

	/** 
	 * Receive response from the Scheduler
	 * This is just a response ACK packet
	 */
	private UserInput receiveFloorResponse() {
		// Create FloorPacket
		FloorPacket floorPacket = new FloorPacket();

		// Receive FloorPacket from scheduler
		System.out.println("Floor: Waiting for Scheduler Response...");
		floorPacket.receive(sendReceiveSocket);

		/** If timeout occurred then return null */
		if (floorPacket.getTimeoutFlag()) {
			return null;
		}

		// Convert FloorPacket to UserInput
		UserInput userInput = new UserInput();
		userInput.convertPacket(floorPacket);
		
		System.out.println("Floor: Received response from Scheduler: " + userInput);
		return userInput;
	}

	/** 
	 * Removes passenger that has gotten off the elevator at their destination
	 * Resets the corresponding button
	 */
	public void elevatorArrival(UserInput floorRequest) {
		
		// Remove the floorRequest from the array of floorRequests
		floorRequests.remove(floorRequest);

		// Reset Button
		if (floorRequest.getFloorButtonUp()) {
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floorRequest.getCurrentFloor()) {
					// Set the button to be Up and turn it on
					button.resetUp();
					break;
				}
			}
		} else {
	
			for (FloorButton button : floorButton) {
				if (button.getButtonFloor() == floorRequest.getCurrentFloor()) {
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

				/** Add User to list of floorRequests */
				floorRequests.add(userInput);

				/**
				 * Wait for Scheduler Packet to let Floor know that an elevator has dropped off passengers
				 * Timeout after 3 seconds if no packet arrives
				 */
				UserInput schedulerResponse = this.receiveFloorResponse();
				if (schedulerResponse != null) {
					System.out.println("Floor: Scheduler responded with " + schedulerResponse.toString());
					this.elevatorArrival(schedulerResponse);
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

		/** Continues to wait for responses */
		while (true) {
			/**
			 * Wait for Scheduler Packet to let Floor know that an elevator has dropped off passengers
			 * Timeout after 3 seconds if no packet arrives
			 */
			UserInput schedulerResponse = this.receiveFloorResponse();
			if (schedulerResponse != null) {
				this.elevatorArrival(schedulerResponse);
			}
		}
	}
	
    public static void main(String[] args) {        
		// Create Floor Thread
        Thread floor = new Thread(new Floor(), "Floor");

        // Start Threads
        floor.start();
    }
}

/** Used to get the information for simulating a user from a text file */
class UserInput{
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	private Date time; // Timestamp of when button was clicked
	private int currentFloor; // Floor that button was clicked on
	private boolean floorButtonUp; // Direction that user wants to go
	private int destinationFloor; // Button that was clicked in elevator to decide destination floor
	private boolean doorFault; // Flag for if a Door Fault has occured
	private boolean hardFault; // Flag for if a Hard Fault has occured
	
	public UserInput(Date time, int currentFloor, boolean floorButtonUp, int destinationFloor, boolean doorFault, boolean hardFault) {
		this.time = time;
		this.currentFloor = currentFloor;
		this.floorButtonUp= floorButtonUp;
		this.destinationFloor = destinationFloor;
		this.doorFault = doorFault;
		this.hardFault = hardFault;
	}

	/** Default Constructor */
	public UserInput() {
		this.time = new Date();
		this.currentFloor = 0;
		this.floorButtonUp= false;
		this.destinationFloor = 0;
		this.doorFault = false;
		this.hardFault = false;
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
		this.doorFault = floorPacket.getDoorFault();
		this.hardFault = floorPacket.getHardFault();
	}

	/** Convert the UserInput to a Bytes */
	public byte[] convertToBytes() {
		// Convert Date object to bytes
		byte timeBytes[] = dateFormatter.format(time).getBytes();

		// Combine the different attributes of the UserInput into one array of bytes packet
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(timeBytes, 0, timeBytes.length);
		outputStream.write(currentFloor);
		outputStream.write(floorButtonUp ? 1 : 0);
		outputStream.write(destinationFloor);
		outputStream.write(doorFault ? 1 : 0);
		outputStream.write(hardFault ? 1 : 0);
		// outputStream.write(0xFF); // Add null character at the end of UserInput
		return outputStream.toByteArray();
	}
	
	@Override
	public String toString() {
		return "{time: " + dateFormatter.format(time) + ", currentFloor: " + currentFloor + ", floorButtonUp: " + floorButtonUp + ", destinationFloor: " + destinationFloor + ", doorFault: " + doorFault + ", hardFault: " + hardFault + "}";
	}

	/** 
	 * Returns the length of bytes in UserInput
	 * 
	 * Date time = 12 bytes
	 * Int currentFloor = 1 byte
	 * Boolean directionUp = 1 byte
	 * Int destinationFloor = 1 byte
	 */
	public int byte_length() {
		return 17; 
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

	public boolean getDoorFault() {
		return doorFault;
	}

	public boolean getHardFault() {
		return hardFault;
	}

	public void setDoorFault(boolean doorFault) {
		this.doorFault = doorFault;
	}

	public void setHardFault(boolean hardFault){
		this.hardFault = hardFault;
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
		if (!upButtonState) {
			System.out.println("FloorButton: Button on floor " + buttonFloor + " pressed in Up direction");
			upButtonState = true;
			upButtonLamp.turnOn();
		}
	}

	// Sets the down button state On and turns on the down Lamp
	public void pressDown() {
		if (!downButtonState) {
			System.out.println("FloorButton: Button on floor " + buttonFloor + " pressed in Down direction");
			downButtonState = true;
			downButtonLamp.turnOn();
		}
	}

	// Turns off the Floor's Up button
	public void resetUp() {
		if (upButtonState) {
			System.out.println("FloorButton: Button for floor " + buttonFloor + " has been reset in Up direction");
			upButtonState = false;
			upButtonLamp.turnOff();
		}
	}

	// Turns off the Floor's Down button
	public void resetDown() {
		if (downButtonState) {
			System.out.println("FloorButton: Button for floor " + buttonFloor + " has been reset in Down direction");
			downButtonState = false;
			downButtonLamp.turnOff();
		}
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
  
