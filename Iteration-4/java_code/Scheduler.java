import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	// private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);

	private DatagramSocket receiveSocket; // Socket for receiving packets from Floor and Elevator
	private InetAddress floorAddress;
	private int floorPort;
	private Scheduler_State currentState;
	
    private List<UserInput> floorRequests = Collections.synchronizedList(new ArrayList<UserInput>()); // Holds list of requests from Floor
	private List<ElevatorInfo> elevatorInfos = Collections.synchronizedList(new ArrayList<ElevatorInfo>()); // Holds the list of elevators and their associated information

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to port 69 
			receiveSocket = new DatagramSocket(69);

			// Set current state to Receive
			currentState = Scheduler_State.RECEIVE;
			
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	/**
	 * Waits until a packet is received and determines if it is an Elevator or Floor packet
	 */
	public DatagramPacket receive() {
		System.out.println("\nScheduler: Entering RECEIVE state");

		/** Wait to receive a packet */
		byte data[] = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
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

		return receivePacket;
	}

	/**
	 * Process the Floor Packet by converting to user input
	 * Adds the UserInput to the Queue of UserInputs
	 */
	public void processFloor(DatagramPacket receivePacket) {
		System.out.println("\nScheduler: Entering PROCESS_FLOOR state");

		/** Set the Floors address */
		floorAddress = receivePacket.getAddress();
		/** Set the Floors port */
		floorPort = receivePacket.getPort();

		/** Receives the Floor Packet and converts it to UserInput */
		UserInput userInput = this.receiveFloorPacket(receivePacket);

		/** Adds the userInput to the List of User's waiting */
		floorRequests.add(userInput);

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
	public void processElevator(DatagramPacket receivePacket) {
		System.out.println("\nScheduler: Entering PROCESS_ELEVATOR state");

		ElevatorInfo elevatorInfo = receiveElevatorPacket(receivePacket);
		
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
			case DOOR_FAULT:
				elevatorInfo = this.serviceElevatorDoorFault(elevatorInfo);
				break;
			case HARD_FAULT:
				elevatorInfo = this.serviceElevatorHardFault(elevatorInfo);
				break;
			case BOOM:
				// Remove the elevator
				elevatorInfos.remove(elevatorInfo);
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

		/** If there are no Floor Requests then return */
		if (floorRequests.isEmpty()) {
			elevator.setDestinationFloor(-1);	
			return elevator;
		}

		ArrayList<Integer> noService = new ArrayList<Integer>();
		ArrayList<Integer> doService = new ArrayList<Integer>();
		
		/** Creates an array of all of the possible floors that can be serviced */
		for(int i = 0; i < NUMBER_OF_FLOORS;i++){
			doService.add(i);
		}
		
		/** Determines if a person is about to be serviced by a moving elevator */
		for(ElevatorInfo elevatorInfo : elevatorInfos) {
			if(elevatorInfo.getIsMoving()) {
				for(UserInput floorRequest : floorRequests) {
					if(elevatorInfo.getDirectionUp() && (elevatorInfo.getCurrentFloor() < floorRequest.getCurrentFloor()) && (elevatorInfo.getDestinationFloor() > floorRequest.getCurrentFloor())) {
						noService.add(floorRequest.getCurrentFloor());
					}
					else if((!elevatorInfo.getDirectionUp() && (elevatorInfo.getCurrentFloor() > floorRequest.getCurrentFloor()) && (elevatorInfo.getDestinationFloor() < floorRequest.getCurrentFloor()))) {
						noService.add(floorRequest.getCurrentFloor());
					}
				}
			}
		}
		
		/** Removes all of the floors to not service since there is already an elevator about to service them */
		doService.removeAll(noService);
		
		/** 
		 * Gets the longest waiting request out of the floor Requests that the elevator should service 
		 * That isn't already being serviced by another elevator
		*/
		boolean noFloorRequests = true;
		UserInput earliest_request = floorRequests.get(0);
		for (Integer floor : doService) {
			for (UserInput floorRequest : floorRequests) {
				if (floorRequest.getCurrentFloor() == floor) {
					noFloorRequests = false;
					if (floorRequest.getTime().getTime() < earliest_request.getTime().getTime()) {
						earliest_request = floorRequest;
					}
				}
			}
		}
		
		/** If noone was on a floor to be serviced then we should move to the longest waiting request */
		if (noFloorRequests) {
			for(Integer floor : noService) {
				for(UserInput floorRequest : floorRequests) {
					if(floorRequest.getCurrentFloor() == floor) {
						if(floorRequest.getTime().getTime() < earliest_request.getTime().getTime()) {
							earliest_request = floorRequest;
						}
					}
				}
			}
		}

		/** Updates the elevator destination floor to where the User is waiting */
		System.out.println("\nScheduler: Sending Elevator to User on floor " + earliest_request.getCurrentFloor());
		elevator.setDestinationFloor(earliest_request.getCurrentFloor());

		/** Add the passenger to the elevator (even though they are not on yet) */
		elevator.addPassenger(earliest_request);

		/** Remove passenger from array of floorRequests */
		floorRequests.remove(earliest_request);
		
		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingUp(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in MOVING_UP State");

		/** The elevator is on a floor with one of their passenger's destinations */
		for (UserInput passenger : elevator.getPassengers()) {
			if (passenger.getDestinationFloor() == elevator.getCurrentFloor() && passenger.getFloorButtonUp() == elevator.getDirectionUp()){
				elevator.setIsMoving(false);
			}	

			/** 
			 * If the elevator is going in a different direction than the a passenger's request
			 * Then that must mean that we are going to service a passenger from the IDLE state
			 * Therefore, keep moving
			*/
			if (passenger.getFloorButtonUp() != elevator.getDirectionUp()) {
				return elevator;
			}
		}
		
		/** The elevator is on a floor with a current FloorRequest */
		for(UserInput floorRequest : floorRequests) {
			if(floorRequest.getCurrentFloor() == elevator.getCurrentFloor() && floorRequest.getFloorButtonUp() == elevator.getDirectionUp()){
				elevator.setIsMoving(false);
			}
		}



		return elevator;
	}
	
	public ElevatorInfo serviceElevatorMovingDown(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in MOVING_DOWN State");
		
		/** The elevator is on a floor with one of their passenger's destinations */
		for (UserInput passenger : elevator.getPassengers()) {
			if(passenger.getDestinationFloor() == elevator.getCurrentFloor() && passenger.getFloorButtonUp() == elevator.getDirectionUp()){
				elevator.setIsMoving(false);
			}	

			/** 
			 * If the elevator is going in a different direction than the a passenger's request
			 * Then that must mean that we are going to service a passenger from the IDLE state
			 * Therefore, keep moving
			*/
			if (passenger.getFloorButtonUp() != elevator.getDirectionUp()) {
				return elevator;
			}
		}

		/** The elevator is on a floor with a current FloorRequest */
		for(UserInput floorRequest : floorRequests) {
			if(floorRequest.getCurrentFloor() == elevator.getCurrentFloor() && floorRequest.getFloorButtonUp() == elevator.getDirectionUp()){
				elevator.setIsMoving(false);
			}
		}

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorStopped(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in STOPPED State");
		
		/** 
		 * Updates the direction of the elevator to match that of the 
		 * passenger's that are already on the elevator from the IDLE
		 * state. 
		 * This makes it so that if the elevator was moving to pick up
		 * a passenger from the IDLE state it doesn't pick up anyone going 
		 * in a different direction  
		 * All passengers on an elevator should be moving in the same direction
		 * at the point it is in the STOPPED state
		*/		
		for (UserInput passenger : elevator.getPassengers()) {
			elevator.setDirectionUp(passenger.getFloorButtonUp());
		}

		return elevator;
	}

	public ElevatorInfo serviceElevatorDoorOpen(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in DOOR_OPEN State");
		
		/** 
		 * Searches through the passenger Destinations then remove all of the passengers
		 * That have a destination on the elevator's current floor in the same direction
		 * 
		 * Also pops the user from the list of pending requests
		 */
		Iterator<UserInput> iterator = elevator.getPassengers().iterator();
		while (iterator.hasNext()) {
			UserInput passenger = iterator.next();
			if(elevator.getCurrentFloor() == passenger.getDestinationFloor() && elevator.getDirectionUp() == passenger.getFloorButtonUp()) {
				/** Remove passenger from the elevator's passengers array */
				System.out.println("Scheduler: Passenger " + passenger.toString() + " exiting the elevator");
				iterator.remove();
			}
		}

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorDoorClose(ElevatorInfo elevator) {
		System.out.println("\nScheduler: Servicing Elevator in DOOR_CLOSE State");
		
		/** 
		 * Adds the passenger destinations for the passengers that are on the same floor as the elevator
		 * and are requesting to go in the same direction
		 * Removes the floorRequest since it is going to be serviced by this elevator
		 */
		Iterator<UserInput> iterator = floorRequests.iterator();
		while (iterator.hasNext()) {
			UserInput floorRequest = iterator.next();
			if(floorRequest.getCurrentFloor() == elevator.getCurrentFloor() && floorRequest.getFloorButtonUp() == elevator.getDirectionUp()){
				/** Add passenger to the elevator's passengers array */
				System.out.println("Scheduler: Passenger " + floorRequest.toString() + " entering the elevator");
				elevator.addPassenger(floorRequest);

				/** Remove passenger from the schedulers floorRequests array */
				iterator.remove();
			}
		}
		
		/**
		 * Send message to the Floor for each passenger that just got on
		 * Must be done using passengers in case the elevator was in the idle state
		 */
		for (UserInput passenger : elevator.getPassengers()) {
			if(passenger.getCurrentFloor() == elevator.getCurrentFloor() && passenger.getFloorButtonUp() == elevator.getDirectionUp()){
				/** Send message to the Floor to tell it an Elevator has Arrived */
				this.sendFloorPacket(passenger, floorAddress, floorPort);	
			}
		}

		return elevator;
	}
	
	public ElevatorInfo serviceElevatorDoorFault(ElevatorInfo elevator) {
	    for(UserInput passenger : elevator.getPassengers()) {
			if(passenger.getDoorFault() == true) {
				passenger.setDoorFault(false);
		    }
		}

	    return elevator;
	}
	public ElevatorInfo serviceElevatorHardFault(ElevatorInfo elevator) {
	    for(UserInput passenger : elevator.getPassengers()) {
			if(passenger.getHardFault() == true) {
				passenger.setHardFault(false);
			}
		}

	    return elevator;
	}
	
	/** 
	 * Receives a FloorRequest packet from Floor
	 * Returns the UserInput of the request
	 */
	public UserInput receiveFloorPacket(DatagramPacket receivePacket) {
		// Create Default FloorPacket
		FloorPacket floorPacket = new FloorPacket();
		// Skip the byte first in the array
		byte[] byteArr = Arrays.copyOfRange(receivePacket.getData(), 1, receivePacket.getData().length);
		// Convert the bytes to an elevatorPacket
		floorPacket.convertBytesToPacket(byteArr);

		// Convert Floor Packet to UserInput
		UserInput userInput = new UserInput(floorPacket.getTime(), floorPacket.getFloor(), 
											floorPacket.getDirectionUp(), floorPacket.getDestinationFloor(),
											floorPacket.getDoorFault(), floorPacket.getHardFault());

		return userInput;
	}

	/**
	 * Sends a floorPacket to the Floor based on the userInput inputted
	 * 
	 * @param userInput userInput information that is to be sent to Floor
	 * @param address IP Address that the Floor exists on
	 * @param port Port number that Floor exists on
	 */
	public void sendFloorPacket(UserInput userInput, InetAddress address, int port) {
        // Create Floor Packet
        FloorPacket floorPacket = new FloorPacket(userInput.getCurrentFloor(), userInput.getTime(), 
													userInput.getFloorButtonUp(), userInput.getDestinationFloor(),
													userInput.getDoorFault(), userInput.getHardFault());

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
	public ElevatorInfo receiveElevatorPacket(DatagramPacket receivePacket) {
		// Create Default ElevatorPacket
		ElevatorPacket elevatorPacket = new ElevatorPacket();
		// Skip the byte first in the array
		byte[] byteArr = Arrays.copyOfRange(receivePacket.getData(), 1, receivePacket.getData().length);
		// Convert the bytes to an elevatorPacket
		try {
			elevatorPacket.convertBytesToPacket(byteArr);
		} catch (ParseException e) {
			System.out.println("Scheduler: Failed to Convert Bytes to Elevator Packet");
			e.printStackTrace();
		}

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

	
	/**
	 * Getter for receiveSocket
	 */
	public DatagramSocket getReceiveSocket() {
		return receiveSocket;
	}

	/**
	 * Getter for floorAddress
	 */
	public InetAddress getFloorAddress() {
		return floorAddress;
	}

	/**
	 * Getter for floorPort
	 */
	public int getFloorPort() {
		return floorPort;
	}

	/**
	 * Getter for currentState
	 */
	public Scheduler_State getCurrentState() {
		return currentState;
	}

	/**
	 * Getter for floorRequests
	 */
	public List<UserInput> getFloorRequests() {
		return floorRequests;
	}

	/**
	 * Getter for elevatorInfos
	 */
	public List<ElevatorInfo> getElevatorInfos () {
		return elevatorInfos;
	}

    public static void main(String[] args) {
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();
		ElevatorGUI elevatorGUI = new ElevatorGUI();

		DatagramPacket receivePacket = null;
		while (true) {
			switch(scheduler.currentState) {
				case RECEIVE:
					receivePacket = scheduler.receive();
					break;
				case PROCESS_FLOOR:
					scheduler.processFloor(receivePacket);
					break;
				case PROCESS_ELEVATOR:
					scheduler.processElevator(receivePacket);
					break;
			}

			// Update the GUI based on the current states of the elevator
			for (ElevatorInfo elevator : scheduler.elevatorInfos) {
				int num_passengers = 0;
				for (UserInput passenger : elevator.getPassengers()) {
					if (passenger.getCurrentFloor() < elevator.getCurrentFloor() && elevator.getDirectionUp() && elevator.getCurrentState() != Elevator_State.IDLE) {
						num_passengers++;
					}
					else if (passenger.getCurrentFloor() > elevator.getCurrentFloor() && !elevator.getDirectionUp() && elevator.getCurrentState() != Elevator_State.IDLE) {
						num_passengers++;
					}
				}
				elevatorGUI.updateStatus(elevator.getElevatorNumber() - 1, elevator.getCurrentFloor(), elevator.getCurrentState().toString(), num_passengers, elevator.getDestinationFloor());
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
    private ArrayList<UserInput> passengers; // Holds the array of passenger destination floors
	private Elevator_State currentState; // Holds the current state of the elevator

	private int port; // Holds port that the Elevator exists on
	private InetAddress address; // Holds the address that the Elevator exists on

    public ElevatorInfo(int elevatorNumber, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<UserInput> passengers, Elevator_State currentState, int port, InetAddress address) {
        this.elevatorNumber = elevatorNumber;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengers = passengers;
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
        this.passengers = new ArrayList<UserInput>();
		this.currentState = Elevator_State.IDLE;
	}

	public void convertPacket(ElevatorPacket elevatorPacket, int port, InetAddress address) {
		this.elevatorNumber = elevatorPacket.getElevatorNumber();
        this.isMoving = elevatorPacket.getIsMoving();
        this.currentFloor = elevatorPacket.getCurrentFloor();
        this.destinationFloor = elevatorPacket.getDestinationFloor();
        this.directionUp = elevatorPacket.getDirectionUp();
        this.passengers = elevatorPacket.getPassengers();
		this.currentState = elevatorPacket.getCurrentState();
		this.port = port; /* TODO: Needs to be tested still */
		this.address = address;
	}

	public void sendPacket(DatagramSocket socket) {
		ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengers, currentState);
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

    public ArrayList<UserInput> getPassengers() {
        return passengers;
    }

	public boolean addPassenger(UserInput passenger) {
        return passengers.add(passenger);
    }

	public boolean removePassenger(UserInput passenger) {
        return passengers.remove(passenger);
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
				", direction: " + (directionUp ? "up" : "down") + ", passenger destinations: " + (passengers.toString());
	}
}
