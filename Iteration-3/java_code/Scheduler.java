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
	private int servicingFloor;
	// TODO: Shouldn't be needed, the scheduler only needs to know where the elevator is going
	// private ArrayList<UserInput> elevatorRequests = new ArrayList<UserInput>(); // Holds list of requests from Elevator

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
	public FloorPacket receiveFloorPacket() {
		// Create new FloorPacket object from data
		FloorPacket floorPacket = new FloorPacket(0,new Date(),false,0);

		// Wait for FloorPacket to arrive
		System.out.println("Scheduler: Waiting for Floor Packet..."); 
		floorPacket.receive(receiveFloorSocket);

		return floorPacket;
	}

	/** Adds a new Floor request to the list of floorRequests */
	public synchronized void addFloorRequest(UserInput userInput) {		
		synchronized (floorRequests) {
			System.out.println("Scheduler: Adding Floor Request to list " + userInput);
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
	 * Wait until an elevator sends a packet saying it is waiting for a floor request
	 * Send a FloorPacket to the elevator to notify it of someone who wants to be picked up 
	 */
	public synchronized void serviceFloorRequest() {
		// Get an Elevator Packet
		ElevatorPacket elevatorPacket = this.receiveElevatorPacket();

		// TODO: Use and actual floorRequest (just testing for now)
		// Send Floor Packet
		// Create UserInput (For testing)
		UserInput userInput;
		try {
			userInput = new UserInput(dateFormatter.parse("10:10:10.1"), NUMBER_OF_FLOORS, false, NUMBER_OF_ELEVATORS);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sendFloorRequest(userInput, elevatorPacket.getReceiveElevatorPacket());

		// // Create FloorPacket
		// FloorPacket floorPacket = new FloorPacket(floorRequest.getFloor(), floorRequest.getTime(), floorRequest.getFloorButtonUp(), floorRequest.getCarButton());
		
		// // Send FloorPacket to the elevator
		// floorPacket.send();
		



		// while (floorRequests.isEmpty()) {
        //     try {
        //         wait();
        //     } catch (InterruptedException e) {
        //     	System.out.println("Error waiting: " + e);
        //         return null;
        //     }
		// }


		// ArrayList<UserInput> users = new ArrayList<UserInput>();

		// // This means elevator is not moving, no one is on it
		// // if (elevatorRequests.isEmpty()) {
		// 	// int lowest = 0;
		// 	// UserInput user = null;
		// 	// // Pick up the user if they are the closest to the elevator
		// 	// for (UserInput floorRequest : floorRequests) {
		// 	// 	if (floorRequest.getFloor() - floor < lowest) { 
		// 	// 		user = floorRequest; 
		// 	// 		System.out.println("Scheduler: Picking up a user at floor " + floor);
		// 	// 		floorRequests.remove(floorRequest);
		// 	// 	}
		// 	// }
		// 	// users.add(user);
		// // } else {
		// 	for (UserInput floorRequest : floorRequests) {
		// 		// Pick up the user if they are on the right floor and going in the right direction
		// 		if (floorRequest.getFloor() == floor && floorRequest.getFloorButtonUp() == directionUp) {
		// 			users.add(floorRequest);
		// 			System.out.println("Scheduler: Picking up a user at floor " + floor);
		// 			floorRequests.remove(floorRequest);
		// 		}
		// 	}
		// // }
		
		// servicingFloor = floor;
		// notifyAll();

		// return users;
	}

	public ElevatorPacket receiveElevatorPacket() {
		// Create Default ElevatorPacket
		ElevatorPacket elevatorPacket = new ElevatorPacket(0, false, 0, 0, false);

		// Wait for ElevatorPacket to arrive
		System.out.println("Scheduler: Waiting for Elevator Packet..."); 
		elevatorPacket.receive(receiveElevatorSocket);

		return elevatorPacket;
	}

	// /** Add a request to the elevator list */
	// public synchronized void addElevatorRequest(UserInput userInput) {		
	// 	System.out.println("Scheduler: Adding Elevator Request to list " + userInput);
	// 	elevatorRequests.add(userInput);
	// 	notifyAll();
	// }

	// /** Return all of the users that are getting off the elevator at this floor */
	// public synchronized ArrayList<UserInput> serviceElevatorRequest(int floor) {
	// 	while (elevatorRequests.isEmpty()) {
    //         try {
    //             wait();
    //         } catch (InterruptedException e) {
    //         	System.out.println("Error waiting: " + e);
    //             return null;
    //         }
	// 	}


	// 	ArrayList<UserInput> users = new ArrayList<UserInput>();

	// 	for (UserInput elevatorRequest : elevatorRequests) {
	// 		users.add(elevatorRequest);
	// 		System.out.println("Scheduler: Dropping off a user at floor " + floor);
	// 		elevatorRequests.remove(elevatorRequest);
	// 	}

	// 	servicingFloor = floor;
	// 	notifyAll();

	// 	return users;
	// }


    public static void main(String[] args) {
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();

		// For testing
		// scheduler.receiveFloorPacket();
		scheduler.serviceFloorRequest();
    }
}
