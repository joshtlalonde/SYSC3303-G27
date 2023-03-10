import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	static final int NUMBER_OF_ELEVATORS = 1; // Number of elevators in the building
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket receiveFloorSocket; // Socket for receiving packets from Floor
	private DatagramSocket receiveElevatorSocket; // Array of sockets for each of the elevators
	
    private ArrayList<UserInput> floorRequests = new ArrayList<UserInput>(); // Holds list of requests from Floor
	// TODO: elevatorRequests should be a Priority based QUEUE (elevators moving have higher priorty, then FIFO)
	private ArrayList<ElevatorPacket> elevatorRequests = new ArrayList<ElevatorPacket>(); // Holds the list of requests from the elevators
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
				// TODO: For now we will just be servicing the most recent FloorRequest
				System.out.println("Scheduler: Elevator " + elevatorPacket.getElevatorNumber() + " is not moving, sending request to pick up user");
				// Get the first FloorRequest
				UserInput floorRequest = floorRequests.get(0);
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

		synchronized (elevatorRequests) {
			System.out.println("Scheduler: Adding Elevator Request to list of requests");
			elevatorRequests.add(elevatorPacket);
			elevatorRequests.notifyAll();
		}

	}

	// /** Add a request to the elevator list */
	// public synchronized void addElevatorRequest(UserInput userInput) {		
	// 	System.out.println("Scheduler: Adding Elevator Request to list " + userInput);
	// 	elevatorRequests.add(userInput);
	// 	notifyAll();
	// }

	/** 
	 * Service an elevator request from the Queue
	 * 
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


		// synchronized (floorRequests) {
		// 	// Wait until a FloorRequest comes in 
		// 	// This will block not being able to receive new elevator requests until a floor request comes in
		// 	// But that is fine since if there are no floor requests then there is no need to service the elevator (There's nothing to tell the elevator)
		// 	while (floorRequests.isEmpty()) {
		// 		try {
		// 			floorRequests.wait();
		// 		} catch (InterruptedException e) {
		// 			System.out.println("Elevator: Synchronized Wait failed on floorRequests: " + e); 
		// 			e.printStackTrace();
		// 		}
		// 	}

		// 	// If it is moving, check if there are any floor requests on the elevators CurrentFloor going in the same direction
		// 	if (serviceElevatorRequest.getIsMoving()) {
		// 		for (UserInput floorRequest : floorRequests) {
		// 			if (floorRequest.getFloor() == serviceElevatorRequest.getCurrentFloor() &&
		// 				floorRequest.getFloorButtonUp() == serviceElevatorRequest.getDirectionUp()) 
		// 			{
		// 				// Service all floor requests on that floor in that direction
		// 				this.serviceFloorRequest(serviceElevatorRequest.getCurrentFloor(), serviceElevatorRequest.getDirectionUp());
		// 			}
		// 		}
		// 	} else {
		// 		// If its not moving, decide which FloorRequest is best to service for the elevator 
		// 		// TODO: For now we will just be servicing the most recent FloorRequest
		// 		this.serviceFloorRequest(floorRequests.get(0).getFloor(), floorRequests.get(0).getFloorButtonUp());
		// 	}
		// }
	}


    public static void main(String[] args) {
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();

		// For testing
		while (true) {
			scheduler.receiveFloorPacket();
			scheduler.receiveElevatorPacket();
			scheduler.serviceElevatorRequest();
		}
    }
}
