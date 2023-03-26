import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket receiveSocket; // Socket for receiving packets from Floor and Elevator
	private DatagramPacket receivePacket; // Holds the most recently received packet.
	private Scheduler_State currentState = Scheduler_State.RECEIVE;
	
	/** NOTE: Technically don't need syncs but never hurts? For now at least */
    private List<UserInput> floorRequests = Collections.synchronizedList(new ArrayList<UserInput>()); // Holds list of requests from Floor
	// private ArrayList<ElevatorPacket> elevatorRequests = new ArrayList<ElevatorPacket>(); // Holds the list of request packets from the elevators
	private List<ElevatorInfo> elevatorInfos = Collections.synchronizedList(new ArrayList<ElevatorInfo>()); // Holds the list of elevators and their associated information

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
		System.out.println("\nScheduler: Entering RECEIVE state");

		/** Wait to receive a packet */
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		try{
			System.out.println("Scheduler: Waiting for Packet..."); 
			receiveSocket.receive(receivePacket);
		} catch(IOException e){
			System.out.println("IO Exception: Likely: ");
			System.out.println("Receive Socket Timed Out. \n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		/** See if packet is a FloorPacket or an ElevatorPacket */
		byte receiveID = data[0];
		if(receiveID == 0){
			System.out.println("Scheduler: Floor Packet Received");
			currentState = Scheduler_State.PROCESS_FLOOR;
		}
		else if(receiveID == 1){
			System.out.println("Scheduler: Elevator Packet Received");	
			currentState = Scheduler_State.PROCESS_ELEVATOR;
		}
		else{
			System.out.println("Scheduler: Unknown Packet Received");
		}
	}

	/**
	 * Process the Floor Packet by converting to user input
	 * Adds the UserInput to the Queue of UserInputs
	 */
	public void processFloor() {
		System.out.println("\nScheduler: Entering PROCESS_FLOOR state");

		/** Receives the Floor Packet and converts it to UserInput */
		UserInput userInput = this.receiveFloorPacket();

		/** Adds the userInput to the List of User's waiting */
		floorRequests.add(userInput);
		
		/** Send ACK back to Floor */
		this.sendFloorPacket(userInput, receivePacket.getAddress(), receivePacket.getPort());

		/** Move to RECEIVE state */
		currentState = Scheduler_State.RECEIVE;
	}

	
	/**
	 * Process the Elevator Packet then convert to elevatorInfo
	 * Updates/adds to the elevatorInfos array
	 * Depending on the currentState of the elevator a corresponding method is called
	 * The elevatorInfo is then updated with whatever the method returns
	 * Then a response packet is sent to the Elevator
	 */
	public void processElevator() {
		System.out.println("\nScheduler: Entering PROCESS_ELEVATOR state");

		ElevatorInfo elevatorInfo = receiveElevatorPacket();
		
		/** Add/Update ElevatorInfo Array */
		boolean updated = false;
		for (ElevatorInfo elevator : elevatorInfos) {
			if (elevator.getElevatorNumber() == elevatorInfo.getElevatorNumber()) {
				elevator = elevatorInfo;
				updated = true;
			}
		}

		// Must not exist in list, adding now 
		if (!updated) {
			elevatorInfos.add(elevatorInfo);
		}

		/** Act upon elevator request depending on currentState */
		switch (elevatorInfo.getCurrentState()) {
			case IDLE:
				elevatorInfo = this.serviceElevatorIdle(elevatorInfo);
				break;
			case MOVING_UP:
				elevatorInfo = this.serviceElevatorMovingUp(elevatorInfo);
				break;
			case MOVING_DOWN:
				elevatorInfo = this.serviceElevatorMovingDown(elevatorInfo);
				break;
			case STOPPED:
				elevatorInfo = this.serviceElevatorStopped(elevatorInfo);
				break;
			case DOOR_OPEN:
				elevatorInfo = this.serviceElevatorDoorOpen(elevatorInfo);
				break;
			case DOOR_CLOSE:
				elevatorInfo = this.serviceElevatorDoorClose(elevatorInfo);
				break;
		}

		/** Update the elevatorInfo returned from the method */
		for (ElevatorInfo elevator : elevatorInfos) {
			if (elevator.getElevatorNumber() == elevatorInfo.getElevatorNumber()) {
				elevator = elevatorInfo;

				/** Send the elevator its updated values */
				elevator.sendPacket(receiveSocket);

				break;
			}
		}		

		/** Update State of scheduler */
		currentState = Scheduler_State.RECEIVE;
	}
	
	/** 
	 * Determines which floorRequest the Elevator should service
	 * 
	 * @param elevator The elevator that scheduler is updating
	 * @return Returns the updated elevatorInfo to be sent in processElevator
	 */
	public ElevatorInfo serviceElevatorIdle(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in IDLE State");

		ArrayList<Integer> noService = new ArrayList<Integer>();
		ArrayList<Integer> doService = new ArrayList<Integer>();
		
		/** Creates an array of all of the possible floors that can be serviced */
		for(int i = 0; i < NUMBER_OF_FLOORS;i++){
			doService.add(i);
		}
		
		/** Determines if a person is about to be serviced by a moving elevator */
		for(ElevatorInfo x : elevatorInfos) {
			if(x.getIsMoving()) {
				int ele_cf = x.getCurrentFloor();
				int ele_df = x.getDestinationFloor();
				for(UserInput y : floorRequests) {
					if(x.getDirectionUp() && (ele_cf < y.getFloor()) && (ele_df > y.getFloor())) {
						noService.add(y.getFloor());
					}
					else if((!x.getDirectionUp() && (ele_cf > y.getFloor()) && (ele_df < y.getFloor()))) {
						noService.add(y.getFloor());
					}
				}
			}
		}
		
		/** Removes all of the floors to not service since there is already an elevator about to service them */
		doService.removeAll(noService);
		
		/** Gets the longest waiting request out of the floors that the elevator should service */
		boolean noFloorRequests = true;
		UserInput least_time = floorRequests.get(0);
		for(Integer f : doService) {
			for(UserInput y : floorRequests) {
				if(y.getFloor() == f) {
					noFloorRequests = false;
					if(y.getTime().getTime() < least_time.getTime().getTime()) {
						least_time = y;
					}
				}
			}
		}
		
		/** If noone was on a floor to be serviced then we should move to the longest waiting request */
		if (noFloorRequests) {
			for(Integer f : noService) {
				for(UserInput y : floorRequests) {
					if(y.getFloor() == f) {
						if(y.getTime().getTime() < least_time.getTime().getTime()) {
							least_time = y;
						}
					}
				}
			}
		}

		/** Updates the destination floor that the elevator needs to go to pick up the floorRequest */
		elevator.setDestinationFloor(least_time.getFloor());
		
		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingUp(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in MOVING_UP State");
		
		for(UserInput y : floorRequests) {
			if(y.getFloor() == elevator.getCurrentFloor() && y.getFloorButtonUp() == elevator.getDirectionUp()){
				serviceElevatorStopped(elevator);
			}
	
		}
		//Find request that can be serviced with Moving Up elevator within floors serviced
			// If there is one tell elevator to stop by setting isMoving to false
		// Refer to Elevator.java as to what information the elevator needs to updated on 'elevatorInfo'

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingDown(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in MOVING_DOWN State");
		
		for(UserInput y : floorRequests) {
			if(y.getFloor() == elevator.getCurrentFloor() && y.getFloorButtonUp() == elevator.getDirectionUp()){
				serviceElevatorStopped(elevator);
			}
	
		}

		//Find request that can be serviced with Moving Down elevator within floors serviced
			// If there is one tell elevator to stop by setting isMoving to false
		// Refer to Elevator.java as to what information the elevator needs to updated on 'elevatorInfo'
		
		return elevator;
	}
	
	public ElevatorInfo serviceElevatorStopped(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in STOPPED State");
		elevator.setIsMoving(false);
		elevator.setCurrentState(Elevator_State.STOPPED);
		serviceElevatorDoorOpen(elevator);
		// Send same shit back, no updates needed here
		// Refer to Elevator.java as to what information the elevator needs to updated on 'elevatorInfo'
		
		return elevator;
	}
	public ElevatorInfo serviceElevatorDoorOpen(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in DOOR_OPEN State");
		int k = 0;
		for(Integer i : elevator.getPassengerDestinations()) {
			if(elevator.getCurrentFloor() == i) {
				elevator.getPassengerDestinations().remove(k);
			}
			k++;
		}
		
		

		//Destinations cleared, people with same floor destination as elevator currentFloor removed. Done
		//Send message to floor stating who got off the elevator (remove them from passengerDestinations)
		//Update Floor Buttons/lights in floor.java (Send a packet back to floor (this needs to be implemented still))
		// Refer to Elevator.java as to what information the elevator needs to updated on 'elevatorInfo'
		
		return elevator;
	}
	
	
	public ElevatorInfo serviceElevatorDoorClose(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in DOOR_CLOSE State");
		ArrayList<Integer> Service = new ArrayList<Integer>();
		
		for(UserInput y : floorRequests) {
			if(y.getFloor() == elevator.getCurrentFloor() && y.getFloorButtonUp() == elevator.getDirectionUp()){
				Service.add(y.getCarButton());
			}
	
		}
		for(Integer i : Service) {
			elevator.addPassengerDestination(Service.get(i));
		}

		//Destinations added, people that got on updated.
	    // Add people to the passengerDestinations 
		// Refer to Elevator.java as to what information the elevator needs to updated on 'elevatorInfo'

		return elevator;
	}
	
	
	/** 
	 * Receives a FloorRequest packet from Floor
	 * Returns the UserInput of the request
	 * 		
	 */
	public UserInput receiveFloorPacket() {
		// Create Default FloorPacket
		FloorPacket floorPacket = new FloorPacket();
		// Skip the byte first in the array
		byte[] byteArr = Arrays.copyOfRange(this.receivePacket.getData(), 1, this.receivePacket.getData().length);
		// Convert the bytes to an elevatorPacket
		floorPacket.convertBytesToPacket(byteArr);

		// Wait for FloorPacket to arrive
		// System.out.println("Scheduler: Waiting for Floor Packet..."); 
		// floorPacket.receive(receiveSocket);

		// Convert Floor Packet to UserInput
		UserInput userInput = new UserInput(floorPacket.getTime(), floorPacket.getFloor(), floorPacket.getDirectionUp(), floorPacket.getDestinationFloor());

		return userInput;
	}

	/**
	 * Sends a floorPacket to the Floor based on the userInput inputted
	 * 
	 * @param userInput userInput information that is to be sent to Floor
	 * @param address IP Address that the Floor exists on
	 * @param port Port number that Floor exists on
	 */
	private void sendFloorPacket(UserInput userInput, InetAddress address, int port) {
        // Create Floor Packet
        FloorPacket floorPacket = new FloorPacket(userInput.getFloor(), userInput.getTime(), userInput.getFloorButtonUp(), userInput.getCarButton());

        // Send Elevator Packet
        System.out.println("Scheduler: Sending response to the floor");
		floorPacket.send(address, port, receiveSocket, false);
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
		ElevatorPacket elevatorPacket = new ElevatorPacket();
		// Skip the byte first in the array
		byte[] byteArr = Arrays.copyOfRange(this.receivePacket.getData(), 1, this.receivePacket.getData().length);
		// Convert the bytes to an elevatorPacket
		elevatorPacket.convertBytesToPacket(byteArr);

		/** Update ElevatorInfo with the packet */
		ElevatorInfo elevatorInfo = null;
		for (ElevatorInfo elevator : elevatorInfos) {
			if (elevator.getElevatorNumber() == elevatorPacket.getElevatorNumber()) {
				// Update the elevator info
				elevator.convertPacket(elevatorPacket, receivePacket.getPort(), receivePacket.getAddress());
				elevatorInfo = elevator;
			} 
		}
		if (elevatorInfo == null) {
			// Create new elevator info
			elevatorInfo = new ElevatorInfo();
			elevatorInfo.convertPacket(elevatorPacket, receivePacket.getPort(), receivePacket.getAddress());
			elevatorInfos.add(elevatorInfo);
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

			// Sleep for 1 second
			try {
				Thread.sleep(1000); 
			} catch (InterruptedException e) {
				e.printStackTrace();
					System.exit(1);
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
	private Elevator_State currentState; // Holds the current state of the elevator

	private int port; // Holds port that the Elevator exists on
	private InetAddress address; // Holds the address that the Elevator exists on

    public ElevatorInfo(int elevatorNumber, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations, Elevator_State currentState, int port, InetAddress address) {
        this.elevatorNumber = elevatorNumber;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
		this.currentState = currentState;
		this.port = port;
		this.address = address;
	}

	/** Default Constructor */
	public ElevatorInfo() {
        this.elevatorNumber = 0;
        this.currentFloor = 0;
        this.destinationFloor = 0;
        this.directionUp = false;
        this.passengerDestinations = new ArrayList<Integer>();
		this.currentState = Elevator_State.IDLE;
	}

	public void convertPacket(ElevatorPacket elevatorPacket, int port, InetAddress address) {
		this.elevatorNumber = elevatorPacket.getElevatorNumber();
        this.isMoving = elevatorPacket.getIsMoving();
        this.currentFloor = elevatorPacket.getCurrentFloor();
        this.destinationFloor = elevatorPacket.getDestinationFloor();
        this.directionUp = elevatorPacket.getDirectionUp();
        this.passengerDestinations = elevatorPacket.getPassengerDestinations();
		this.currentState = elevatorPacket.getCurrentState();
		this.port = port; /* TODO: Needs to be tested still */
		this.address = address;
	}

	public void sendPacket(DatagramSocket socket) {
		ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengerDestinations, currentState);
		elevatorPacket.send(address, port, socket, false);
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

	public Elevator_State getCurrentState() {
        return currentState;
    }

	public void setCurrentState(Elevator_State currentState) {
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
