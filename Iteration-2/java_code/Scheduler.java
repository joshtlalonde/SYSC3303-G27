import java.io.*;
import java.util.*;

public class Scheduler {
	static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
	
    private ArrayList<UserInput> floorRequests = new ArrayList<UserInput>(); // Holds list of requests from Floor
	private int servicingFloor;
	private ArrayList<UserInput> elevatorRequests = new ArrayList<UserInput>(); // Holds list of requests from Elevator
	
	/** Returns the number of floors that the system has */
	public int getNumberOfFloors() {
		return NUMBER_OF_FLOORS;
	}

	/** Adds a new Floor request to the list of floorRequests */
	public synchronized void addFloorRequest(UserInput userInput) {		
		System.out.println("Scheduler: Adding Floor Request to list " + userInput);
		floorRequests.add(userInput);
		notifyAll();
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

	/** Return all of the users that are waiting on this floor for an elevator in a certain direction*/
	public synchronized ArrayList<UserInput> serviceFloorRequest(int floor, boolean directionUp) {
		while (floorRequests.isEmpty() && elevatorRequests.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
            	System.out.println("Error waiting: " + e);
                return null;
            }
		}


		ArrayList<UserInput> users = new ArrayList<UserInput>();

		// This means elevator is not moving, no one is on it
		if (elevatorRequests.isEmpty()) {
			int lowest = 0;
			UserInput user = null;
			// Pick up the user if they are the closest to the elevator
			for (UserInput floorRequest : floorRequests) {
				if (floorRequest.getFloor() - floor < lowest) { 
					user = floorRequest; 
					floorRequests.remove(floorRequest);
				}
			}
			users.add(user);
		} else {
			for (UserInput floorRequest : floorRequests) {
				// Pick up the user if they are on the right floor and going in the right direction
				if (floorRequest.getFloor() == floor && floorRequest.getFloorButtonUp() == directionUp) {
					users.add(floorRequest);
					floorRequests.remove(floorRequest);
				}
			}
		}

		System.out.println("Scheduler: Picking up a user at floor " + floor);
		servicingFloor = floor;
		notifyAll();

		return users;
	}

	/** Add a request to the elevator list */
	public synchronized void addElevatorRequest(UserInput userInput) {		
		System.out.println("Scheduler: Adding Elevator Request to list " + userInput);
		elevatorRequests.add(userInput);
		notifyAll();
	}

	/** Return all of the users that are getting off the elevator at this floor */
	public synchronized ArrayList<UserInput> serviceElevatorRequest(int floor) {
		while (floorRequests.isEmpty() && elevatorRequests.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
            	System.out.println("Error waiting: " + e);
                return null;
            }
		}


		ArrayList<UserInput> users = new ArrayList<UserInput>();

		for (UserInput elevatorRequest : elevatorRequests) {
			users.add(elevatorRequest);
			elevatorRequests.remove(elevatorRequest);
		}

		System.out.println("Scheduler: Dropping off a user at floor " + floor);
		servicingFloor = floor;
		notifyAll();

		return users;
	}


    public static void main(String[] args) {
        Thread floor, elevator;
        
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();
		DirectionLamp directionLamp = new DirectionLamp();
        
        // Create Agent and Chef threads
        floor = new Thread(new Floor(scheduler, directionLamp), "Floor");
        elevator = new Thread(new Elevator(scheduler, directionLamp), "Elevator");
        
        // Start Threads
        floor.start();
        elevator.start();
    }
}
