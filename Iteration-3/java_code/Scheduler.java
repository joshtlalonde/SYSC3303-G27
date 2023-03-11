import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.math.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket receiveFloorSocket; // Socket for receiving packets from Floor
	private DatagramSocket receiveElevatorSocket; // Array of sockets for each of the elevators
	
    private ArrayList<UserInput> floorRequests = new ArrayList<UserInput>(); // Holds list of requests from Floor
	// TODO: elevatorRequests should be a Priority based QUEUE (elevators moving have higher priorty, then FIFO)
	private ArrayList<ElevatorPacket> elevatorRequests = new ArrayList<ElevatorPacket>(); // Holds the list of request packets from the elevators
	private ArrayList<ElevatorInfo> elevatorsInfo = new ArrayList<ElevatorInfo>(); // Holds the list of elevators and their associated information
	private int servicingFloor;

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to port 69 
			// This socket will be used to receive Elevators's packets.
			receiveElevatorSocket = new DatagramSocket(69);

			// Construct a datagram socket and bind it to port 23 
			// This socket will be used to receive Floor's packets.
			receiveFloorSocket = new DatagramSocket(23);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
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
	 * TODO: Should this be running as its own thread that way it can always be listening?
	 * 		So this should be its own class, maybe within the scheduler?
	 * 		Or maybe outside of it and just call the addFloorRequest function as a kind of "put"
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
	 * Sends an editted elevatorPacket to the elevator to let the elevator know where it is moving and who is on it
	 * 
	 * @elevatorPacket Takes a Request elevatorPacket as input, this packet is an elevator asking for a response from the scheduler
	 */
	public void serviceFloorRequest(ElevatorPacket elevatorPacket) {
		int minIndex  = 0;
		int elevatorDistance = 1000;
		synchronized (floorRequests) {
	 		// Wait until a FloorRequest comes in 
	 		while (floorRequests.isEmpty()) {
	 			try {
	 				floorRequests.wait();
	 			} catch (InterruptedException e) {
					System.out.println("Elevator: Synchronized wait failed on floorRequests: " + e); 
	 				e.printStackTrace();
				}
			}

	 		// If it is moving, check if there are any floor requests on the elevators CurrentFloor going in the same direction
	 		if (elevatorPacket.getIsMoving()) {
	 			System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is not moving, checking if there is a request on floor " + 
	 								elevatorPacket.getCurrentFloor() + " going " + (elevatorPacket.getDirectionUp() ? "down" : "up"));
	 			for (UserInput floorRequest : floorRequests) {
	 				if (floorRequest.getFloor() == elevatorPacket.getCurrentFloor() &&
	 					floorRequest.getFloorButtonUp() == elevatorPacket.getDirectionUp()) 
					{
	 					// Add passengerDestination to elevator Packet (This is the button that the passenger will be clicking)
	 					elevatorPacket.getPassengerDestinations().add(floorRequest.getCarButton());
						// Remove Floor Request from list of Requests
	 					floorRequests.remove(floorRequest);
	 				}
	 			}
	 		} else {
	 			// If its not moving, decide which FloorRequest is best to service for the elevator 
	 			// TODO: To Do for Iteration 4 -> Currently services floors to elevators, this could cause a floor to experience starvation
	 			// TODO: need to find a way to have a floor that is really far away eventually get an elevator 
	 			// TODO: And not starve out. Idly would look through elevators to see which elevator could service floor
	 			// TODO: Another thought is to add a priority to floors that if skipped over enough would activate putting
	 			// TODO: it next in line for an elevator. 
	 			for(int i = 0; i < floorRequests.size(); i++) {
	 				int tempElevatorDistance = Math.abs(elevatorPacket.getCurrentFloor() - floorRequests.get(i).getFloor());
	 				if(tempElevatorDistance < elevatorDistance){
	 					elevatorDistance = tempElevatorDistance;
	 					minIndex = i;
	 				}
	 				
	 			}
	 			
	 			System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is not moving, sending request to pick up user");
	 			// Get the first FloorRequest
	 			UserInput floorRequest = floorRequests.get(minIndex);
	 			// Set the floor the passenger sent the request on to the destination for the elevator
				elevatorPacket.setDestinationFloor(floorRequest.getFloor());
	 			// Add passengerDestination to elevator Packet
	 			elevatorPacket.getPassengerDestinations().add(floorRequest.getCarButton());
	 			// Remove Floor Request from list of Requests
	 			floorRequests.remove(floorRequest);
			}

	 		// Send elevatorPacket to the elevator
	 		System.out.println("Scheduler: Sending Elevator Packet to elevator: ");
	 		elevatorPacket.send(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);

	 		// Notify any thread waiting on floorRequests
	 		floorRequests.notifyAll();
	 		}
	}

	 			
	 			
	 /** 
	 * TODO: Should this be in its own class thread that way the scheduler can receive multiple packets at a time and act on them concurrently? 
	 * Yes and add each of the elevator reqeusts to a Queue to be serviced
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

		// synchronized (elevatorRequests) {
		// 	System.out.println("Scheduler: Adding Elevator Request to list of requests");
		// 	elevatorRequests.add(elevatorPacket);
		// 	elevatorRequests.notifyAll();
		// }

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
	public void serviceElevatorIdleRequest(ElevatorPacket elevatorPacket, ElevatorInfo elevator) {
		synchronized (floorRequests) {
			// Wait until a FloorRequest comes in 
			while (floorRequests.isEmpty()) {
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

	public void serviceElevatorMovingRequest(ElevatorPacket elevatorPacket, ElevatorInfo elevator) {
		synchronized (floorRequests) { 
			// TODO: Need to build this out still
			System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is in Moving State, checking if there is anyone to pickup");

			// Check if there are any floorRequests on the floor the elevator is on
			// if there is a floorRequest
			for(UserInput floorRequest : floorRequests){
				if(floorRequest.getFloor() == elevatorPacket.getCurrentFloor()) {
					//Add floorRequest to elevatorPacket
					//Send a packet to the Elevator with the new passengerDestinations
					//Do we do this in the array or?
				}
			}
			
			// If no:
				// Send packet back as it was received
			
			// Send elevatorPacket to the elevator
			System.out.println("Scheduler: Sending Elevator Packet to elevator: ");
			// elevatorPacket.send(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);
			elevator.sendPacket(elevatorPacket.getReceiveElevatorPacket().getAddress(), elevatorPacket.getReceiveElevatorPacket().getPort(), receiveElevatorSocket);

			// Notify any thread waiting on floorRequests
			floorRequests.notifyAll();
		}
	}

	 /** 
	  * Service an elevator request from the Queue
	  * 
	  * TODO: The elevatorRequests should be a Priority Queue
	  * If the elevator is not moving then send it to a new request
	  * If the elevator is moving then check if anyone is waiting at the currentFloor
	  * 		If there is tell the elevator to stop
	  */
	 public void serviceElevatorRequest() {
	 	/** Wait until elevator Request arrives */ 
	 	ElevatorPacket serviceElevatorRequest = null;
	 	synchronized (elevatorRequests) {
	 		while (elevatorRequests.isEmpty()) {
	 			try {
	 				elevatorRequests.wait();
	 			} catch (InterruptedException e) {
	 				System.out.println("Elevator: Synchronized Wait failed on elevatorRequests: " + e); 
	 				e.printStackTrace();
	 			}
	 		}

	 		// Get and remove the first instance of an elevator request that is moving
	 		for (ElevatorPacket elevatorRequest : elevatorRequests) {
	 			if (elevatorRequest.getIsMoving()) {
	 				serviceElevatorRequest = elevatorRequest;
	 				elevatorRequests.remove(elevatorRequest);
				}
	 		}

	 		// If none of the elevators are moving then get and remove the first instance of a stopped elevator
	 		if (serviceElevatorRequest == null) {
	 			serviceElevatorRequest = elevatorRequests.remove(0);
	 		}

	 		// Notify any thread waiting on elevatorRequests
	 		elevatorRequests.notifyAll();
	 	}

	 	// Check if serviceElevatorRequest was set
	 	if (serviceElevatorRequest == null) {
	 		System.out.println("Elevator: serviceElevatorRequest failed to set");
	 		return;
	 	} 

	 	/** Decide what which FloorRequest should be services by the elevator */
	 	// This will block not being able to receive new elevator requests until a floor request comes in
	 	// But that is fine since if there are no floor requests then there is no need to service the elevator (There's nothing to tell the elevator)
	 	this.serviceFloorRequest(serviceElevatorRequest);
	 }


    public static void main(String[] args) {
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();

		// For testing
		while (true) {
			scheduler.receiveFloorPacket(); //TODO: Will need a timeout on this
			scheduler.receiveElevatorPacket();
			// scheduler.serviceElevatorRequest();
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

    public ElevatorInfo(int elevatorNumber, boolean isMoving, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations) {
        this.elevatorNumber = elevatorNumber;
        this.isMoving = isMoving;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
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

	public boolean addPassengerDestinations(int passengerDestination) {
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
