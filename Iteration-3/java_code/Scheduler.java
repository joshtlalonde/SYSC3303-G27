import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket receiveSocket; // Socket for receiving packets from Floor and Elevator
	private Scheduler_State currentState;
	
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
		//Receive a gigantic FLOOR packet
		// add to ArrayList FloorRequest
		
	}

	
	/**
	* This is the fucky wucky one ~ Jakob2023
	*
	*/
	public void processElevator() {
		//receive a ginormous ELE PACKET
		//Determine state 
		//Update ArrayList elevatorinfo with "current received state"
		//Determine which state method to proceed with
			//Nah nvm... **JOSH LOOK LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOK** Add send method to elevatorInfo 
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

	/** Sends floor request to specific elevator */
	private void sendFloorRequest(UserInput userInput, DatagramPacket elevatorPacket) {
		// Create FloorPacket
		FloorPacket floorPacket = new FloorPacket(userInput.getFloor(), userInput.getTime(), userInput.getFloorButtonUp(), userInput.getCarButton());

		// Send FloorPacket
		try {
			floorPacket.send(InetAddress.getLocalHost(), elevatorPacket.getPort(), receiveElevatorSocket);
		} catch (UnknownHostException e) {
			System.out.println("Failed to send FloorPacket: " + e);
			e.printStackTrace();
		}
		
		System.out.println("Scheduler: Sent floor request to elevator: " + userInput);
	}

	/** Receives a FloorRequest packet from Floor
	 * TODO: 
	 * 		
	 */
	public void receiveFloorPacket() {
		// Create new FloorPacket object from data
		FloorPacket floorPacket = new FloorPacket(0,new Date(),false,0);

		// Wait for FloorPacket to arrive
		System.out.println("Scheduler: Waiting for Floor Packet..."); 
		floorPacket.receive(receiveFloorSocket);

		// Convert Floor Packet to UserInput
		UserInput userInput = new UserInput(floorPacket.getTime(), floorPacket.getFloor(), floorPacket.getDirectionUp(), floorPacket.getDestinationFloor());

		// Add floor request to the list of floor requests
		synchronized (floorRequests) {
			System.out.println("Scheduler: Adding Floor Request to list of requests: " + userInput);
			floorRequests.add(userInput);
			floorRequests.notifyAll();
		}
	}

	/** Notify the Floor that its request has been serviced */
	/** This function will not be used once UDP is setup */
	public synchronized UserInput respondFloorRequest(int floor) {
        while (servicingFloor != floor) {
            try {
                wait();
            } catch (InterruptedException e) {
            	System.out.println("Error waiting: " + e);
                return null;
            }
		}

		for (UserInput request : floorRequests) {
			if (request.getFloor() == floor) {
				System.out.println("Scheduler: Notifiying Floor of elevator arrival at floor " + floor);
				return request;
			}
		}

		return null;
	}

	/** 
	 * TODO: Find out which elevator packet came from, state, and port.
	 * 
	 * Get a Packet from an elevator, then add it to the Queue
	 * 
	 */
	public void receiveElevatorPacket() {
		// Create Default ElevatorPacket
		ElevatorPacket elevatorPacket = new ElevatorPacket(0, false, 0, 0, false, new ArrayList<Integer>());

		// Wait for ElevatorPacket to arrive
		System.out.println("Scheduler: Waiting for Elevator Packet..."); 
		elevatorPacket.receive(receiveElevatorSocket);

		/** Update ElevatorInfo with the packet */
		ElevatorInfo elevator = null;
		for (ElevatorInfo elevatorInfo : elevatorsInfo) {
			if (elevatorInfo.getElevatorNumber() == elevatorPacket.getElevatorNumber()) {
				// Update the elevator info
				elevatorInfo.convertPacket(elevatorPacket);
				elevator = elevatorInfo;
			} 
		}
		if (elevator == null) {
			// Create new elevator info
			elevator = new ElevatorInfo(elevatorPacket.getElevatorNumber(), 
										elevatorPacket.getIsMoving(), elevatorPacket.getCurrentFloor(), 
										elevatorPacket.getDestinationFloor(), elevatorPacket.getDirectionUp(), 
										elevatorPacket.getPassengerDestinations());
			elevatorsInfo.add(elevator);
			
		}


		/** Figure out what state the elevator is in */
		if (elevatorPacket.getIsMoving() == false && 
			elevatorPacket.getCurrentFloor() == elevatorPacket.getDestinationFloor() && 
			elevatorPacket.getPassengerDestinations().isEmpty()) 
		{
			/** Idle state: Sends closest FloorRequest */
			this.serviceElevatorIdleRequest(elevatorPacket, elevator);
		}
		else if (elevatorPacket.getIsMoving() == false) {
			/** Stopped state: Updates passengerDestinations */
		}
		else if (elevatorPacket.getIsMoving()) {
			/** Moving State: Tells to stop if needed */
			this.serviceElevatorMovingRequest(elevatorPacket, elevator);
		}
		else {
			System.out.println("Scheduler: Elevator is in an unknown state"); 
		} 
	}

	/** Elevator is in Idle state, give it a new FloorRequest to service */ 
	public void serviceElevatorIdle(ElevatorPacket elevatorPacket, ElevatorInfo elevator) {
		synchronized (floorRequests) {
			// Wait until a FloorRequest comes in 
			while (floorRequests.isEmpty()) { //tbh why???
				try {
					floorRequests.wait();
				} catch (InterruptedException e) {
					System.out.println("Scheduler: Synchronized wait failed on floorRequests: " + e); 
					e.printStackTrace();
				}
			}
 
			// TODO: For now we will just be servicing the most recent FloorRequest
			// TODO: We should be looking through the FloorRequests and deciding who is best to service
			System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is in Idle State, sending request to pick up new passenger");

			// Get the first FloorRequest
			UserInput floorRequest = floorRequests.get(0);

			// Set the floor the passenger sent the request on to the destination for the elevator
			elevator.setDestinationFloor(floorRequest.getFloor());

			// Add passengerDestination to elevator Packet
			// elevator.getPassengerDestinations().add(floorRequest.getCarButton()); The passenger hasn't clicked a button yet

			// Remove Floor Request from list of Requests
			// floorRequests.remove(floorRequest); // Think it should only be removed once they get on the elevator which is in STOPPED state

			// Send elevatorPacket to the elevator
			System.out.println("Scheduler: Sending Elevator Packet to elevator: ");
			// elevatorPacket.send(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);
			elevator.sendPacket(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);

			// Notify any thread waiting on floorRequests
			floorRequests.notifyAll();
		}
	}

	public void serviceElevatorMovingRequest(ElevatorPacket elevatorPacket, ElevatorInfo elevator) {
		synchronized (floorRequests) { 
			// TODO: Need to build this out still
			System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is in Moving State, checking if there is anyone to pickup");

			// Check if there are any floorRequests on the floor the elevator is on
			boolean sendPacket = false;
			for (UserInput floorRequst : floorRequests) {
				if (floorRequst.getFloor() == elevator.getCurrentFloor()) {
					sendPacket = true;
				}
			}
			if (sendPacket) {
				// Stop the elevator
				elevator.setIsMoving(false);
			}

			// Send elevatorPacket to the elevator
			System.out.println(sendPacket);
			System.out.println("Scheduler: Sending Elevator Packet to elevator: ");
			// elevatorPacket.send(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);
			elevator.sendPacket(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);

			// Notify any thread waiting on floorRequests
			floorRequests.notifyAll();
		}
	}
	
	//Stop State
	public void serviceElevatorStopRequest(ElevatorPacket elevatorPacket, ElevatorInfo elevator) {
		//Every passenger on that floor going in the same direction 
		System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is in the Stopped state, checking if there is anyone to pickup on floor " + elevatorPacket.getCurrentFloor());
		synchronized (floorRequests) {
			while (floorRequests.isEmpty()) {
				try {
					floorRequests.wait();
				} catch (InterruptedException e) {
					System.out.println("Scheduler: Synchronized wait failed on floorRequests: " + e); 
					e.printStackTrace();
				}
			}
		}
		
		for(UserInput floorRequest : floorRequests) {
			if(floorRequest.getFloor() == elevator.getCurrentFloor() && (floorRequest.getFloorButtonUp() == elevator.getDirectionUp())) {
				// Add passengerDestination to elevator Packet
				elevator.getPassengerDestinations().add(floorRequest.getCarButton());

				// Remove Floor Request from list of Requests
				floorRequests.remove(floorRequest);

				// Send elevatorPacket to the elevator
				System.out.println("Scheduler: Sending Elevator Packet to elevator: ");
				
				// elevatorPacket.send(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);
				elevator.sendPacket(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);

				// Notify any thread waiting on floorRequests
				floorRequests.notifyAll();
			}
				
			}
			
			
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

    public ElevatorInfo(int elevatorNumber, State elev_State, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations) {
        this.elevatorNumber = elevatorNumber;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
	this.elev_State = elev_State;
	}

	public void convertPacket(ElevatorPacket elevatorPacket) {
		this.elevatorNumber = elevatorPacket.getElevatorNumber();
        this.isMoving = elevatorPacket.getIsMoving();
        this.currentFloor = elevatorPacket.getCurrentFloor();
        this.destinationFloor = elevatorPacket.getDestinationFloor();
        this.directionUp = elevatorPacket.getDirectionUp();
        this.passengerDestinations = elevatorPacket.getPassengerDestinations();
	}

	public void sendPacket(InetAddress address, int port, DatagramSocket socket) {
		ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengerDestinations);
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

    public ArrayList<Integer> getPassengerDestinations() {
        return passengerDestinations;
    }

	public boolean addPassengerDestination(int passengerDestination) {
        return passengerDestinations.add(passengerDestination);
    }

    ///////////// PRINTERS /////////////

	@Override
	public String toString() {
		return "Elevator number: " + elevatorNumber + ", is elevator moving: " + (isMoving ? "yes" : "no") + 
				", current floor: " + currentFloor + ", destination floor: " + destinationFloor + 
				", direction: " + (directionUp ? "up" : "down") + ", passenger destinations: " + (passengerDestinations.toString());
	}
}
