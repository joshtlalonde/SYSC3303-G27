import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket receiveSocket; // Socket for receiving packets from Floor and Elevator
	private Scheduler_State currentState = Scheduler_State.RECEIVE;
	
    private ArrayList<UserInput> floorRequests = new ArrayList<UserInput>(); // Holds list of requests from Floor
	// TODO: elevatorRequests should be a Priority based QUEUE (elevators moving have higher priorty, then FIFO)
	private ArrayList<ElevatorPacket> elevatorRequests = new ArrayList<ElevatorPacket>(); // Holds the list of request packets from the elevators
	private ArrayList<ElevatorInfo> elevatorsInfo = new ArrayList<ElevatorInfo>(); // Holds the list of elevators and their associated information
	private int servicingFloor;

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to port 69 
			receiveSocket = new DatagramSocket(69);
			
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	/**
	 * Waits until a packet is received and determines if it is an Elevator or Floor packet
	 */
	public void receive() {
		System.out.println("Scheduler: Entering RECEIVE state");

		byte data[] = new byte[1];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		try{
			receiveSocket.receive(receivePacket);
		} catch(IOException e){
			System.out.println("IO Exception: Likely: ");
			System.out.println("Receive Socket Timed Out. \n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		byte receiveID = data[0];
		if(receiveID == 0){
			System.out.println("Floor Packet Received");
			currentState = Scheduler_State.PROCESS_FLOOR;
		}
		else if(receiveID == 1){
			System.out.println("Elevator Packet Received");	
			currentState = Scheduler_State.PROCESS_ELEVATOR;
		}
		else{
			System.out.println("Unknown Packet Received");
		}
	}

	/** 
	* We follow good programming procedures by making comments like this :)
	*
	*/
	
	public void processFloor() {
		System.out.println("Scheduler: Entering PROCESS_FLOOR state");

		//Receive a gigantic FLOOR packet
		// add to ArrayList FloorRequest
		
	}

	
	/**
	* This is the fucky wucky one ~ Jakob2023
	*
	*/
	public void processElevator() {
		System.out.println("Scheduler: Entering PROCESS_ELEVATOR state");

		//receive a ginormous ELE PACKET
		//Determine state 
		//Update ArrayList elevatorinfo with "current received state"
		//Determine which state method to proceed with
			//**JOSH LOOK LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOK** Add send method to elevatorInfo 
		// Send the newly editted elevatorInfo that is returned from the function	
		
	}
	
	
	public ElevatorInfo serviceElevatorIdle(ElevatorInfo elevator) {
		//Find request that can be serviced with Idle elevator instead of moving
		//Update elevatorInfo accordingly.

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingUp(ElevatorInfo elevator) {
		//Find request that can be serviced with Moving Up elevator within floors serviced
		//Update elevatorInfo accordingly.

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingDown(ElevatorInfo elevator) {
		//Find request that can be serviced with Moving Down elevator within floors serviced
		//Update elevatorInfo accordingly.
		
		return elevator;
	}
	
	public ElevatorInfo serviceElevatorStopped(ElevatorInfo elevator) {
		//Tells Elev to go stop state. 
		//Update elevatorInfo accordingly.
		
		return elevator;
	}
	public ElevatorInfo serviceElevatorDoorOpen(ElevatorInfo elevator) {
		//Destinations cleared, people with same floor destination removed.
		//Send message to floor stating who got off the elevator
		//Update Floor Buttons/lights in floor.java
		//Update elevatorInfo accordingly
		
		return elevator;
	}
	
	
	public ElevatorInfo serviceElevatorDoorClose(ElevatorInfo elevator) {
		//Destinations added, people that got on updated.
	        //Update Floor Buttons/lights in floor.java
		//Update elevatorInfo accordingly.

		return elevator;
	}

	/** 
	 * Receives a FloorRequest packet from Floor
	 * Returns the UserInput of the request
	 * 		
	 */
	public UserInput receiveFloorPacket() {
		// Create new FloorPacket object from data
		FloorPacket floorPacket = new FloorPacket(0,new Date(),false,0);

		// Wait for FloorPacket to arrive
		System.out.println("Scheduler: Waiting for Floor Packet..."); 
		floorPacket.receive(receiveSocket);

		// Convert Floor Packet to UserInput
		UserInput userInput = new UserInput(floorPacket.getTime(), floorPacket.getFloor(), floorPacket.getDirectionUp(), floorPacket.getDestinationFloor());

		return userInput;
	}

	/** 
	 * 
	 * Get a Packet from an elevator
	 * Convert it to ElevatorInfo
	 * Update the ElevatorInfo if it exists already, make new one if not
	 * Then return the elevatorInfo
	 * 
	 */
	public ElevatorInfo receiveElevatorPacket() {
		// Create Default ElevatorPacket
		ElevatorPacket elevatorPacket = new ElevatorPacket(0, false, 0, 0, false, new ArrayList<Integer>(), 0);

		// Wait for ElevatorPacket to arrive
		System.out.println("Scheduler: Waiting for Elevator Packet..."); 
		elevatorPacket.receive(receiveSocket);

		/** Update ElevatorInfo with the packet */
		ElevatorInfo elevatorInfo = null;
		for (ElevatorInfo elevator : elevatorsInfo) {
			if (elevator.getElevatorNumber() == elevatorPacket.getElevatorNumber()) {
				// Update the elevator info
				elevator.convertPacket(elevatorPacket);
				elevatorInfo = elevator;
			} 
		}
		if (elevatorInfo == null) {
			// Create new elevator info
			elevatorInfo = new ElevatorInfo();
			elevatorInfo.convertPacket(elevatorPacket);
			elevatorsInfo.add(elevatorInfo);
		}

		return elevatorInfo;
	}

    public static void main(String[] args) {
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();

		// For testing
		while (true) {
			switch(scheduler.currentState) {
				case RECEIVE:
					scheduler.receive();
					break;
				case PROCESS_FLOOR:
					scheduler.processFloor();
					break;
				case PROCESS_ELEVATOR:
					scheduler.processElevator();
					break;
			}

		}
    }
}

class ElevatorInfo {
    private int elevatorNumber; // Holds number of the elevator
    private boolean isMoving; // Holds if elevator is moving
    private int currentFloor; // Holds the current floor info
    private int destinationFloor; // Holds the destination floor info
    private boolean directionUp; // Holds the direction info
    private ArrayList<Integer> passengerDestinations; // Holds the array of passenger destination floors
	private int currentState; // Holds the current state of the elevator

	private int port; // Holds port that the Elevator exists on
	private InetAddress address; // Holds the address that the Elevator exists on

    public ElevatorInfo(int elevatorNumber, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations, int currentState) {
        this.elevatorNumber = elevatorNumber;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
		this.currentState = currentState;
	}

	/** Default Constructor */
	public ElevatorInfo() {
        this.elevatorNumber = 0;
        this.currentFloor = 0;
        this.destinationFloor = 0;
        this.directionUp = false;
        this.passengerDestinations = new ArrayList<Integer>();
		this.currentState = 0;
	}

	public void convertPacket(ElevatorPacket elevatorPacket) {
		this.elevatorNumber = elevatorPacket.getElevatorNumber();
        this.isMoving = elevatorPacket.getIsMoving();
        this.currentFloor = elevatorPacket.getCurrentFloor();
        this.destinationFloor = elevatorPacket.getDestinationFloor();
        this.directionUp = elevatorPacket.getDirectionUp();
        this.passengerDestinations = elevatorPacket.getPassengerDestinations();
		this.currentState = elevatorPacket.getCurrentState();
		this.port = elevatorPacket.getReceiveElevatorPacket().getPort(); /* TODO: Needs to be tested still */
		this.address = elevatorPacket.getReceiveElevatorPacket().getAddress();
	}

	public void sendPacket(DatagramSocket socket) {
		ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengerDestinations, currentState);
		elevatorPacket.send(address, port, socket);
	}

    ///////////// GETTERS AND SETTERS /////////////
    
    public int getElevatorNumber() {
        return elevatorNumber;
    }

	public void setElevatorNumber(int elevatorNumber) {
        this.elevatorNumber = elevatorNumber;
    }

    public boolean getIsMoving() {
        return isMoving;
    }

	public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

	public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public boolean getDirectionUp() {
        return directionUp;
    }

	public void setDirectionUp(boolean directionUp) {
        this.directionUp = directionUp;
    }

	public int getCurrentState() {
        return currentState;
    }

	public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public ArrayList<Integer> getPassengerDestinations() {
        return passengerDestinations;
    }

	public boolean addPassengerDestination(int passengerDestination) {
        return passengerDestinations.add(passengerDestination);
    }

	public int getPort() {
        return port;
    }

	public void setPort(int port) {
        this.port = port;
    }

	public InetAddress getAddress() {
        return address;
    }

	public void setAddress(InetAddress address) {
        this.address = address;
    }

    ///////////// PRINTERS /////////////

	@Override
	public String toString() {
		return "Elevator number: " + elevatorNumber + ", is elevator moving: " + (isMoving ? "yes" : "no") + 
				", current floor: " + currentFloor + ", destination floor: " + destinationFloor + 
				", direction: " + (directionUp ? "up" : "down") + ", passenger destinations: " + (passengerDestinations.toString());
	}
}
