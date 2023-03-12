import java.io.*;
import java.net.*;
import java.util.*;

class Elevator implements Runnable
{
    static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building

    private DatagramSocket sendReceiveSocket; // Socket that Elevator sends and receives packets from

    private int elevatorNumber; // Number of the elevator
    private boolean isMoving; // Is the elevator moving
    private int currentFloor; // Current Floor the elevator is on
    private int destinationFloor; // Floor the elevator is moving to
    private boolean directionUp; // Direction elevator is moving in

    private DirectionLamp directionLamp = new DirectionLamp(); // Lamp to indicate direction moving and floor location
    private ArrivalSensor arrivalSensor = new ArrivalSensor(); // Sensor to indicate when an elevator is approaching a floor
    private Motor motor = new Motor(); // The elevators motor, controls motion of elevator
    private Door door = new Door(); // Elevators door
    private ArrayList<ElevatorButton> elevatorButtons = new ArrayList<ElevatorButton>(); // Holds the buttons for each of the floors (up and down)
    
    public Elevator(int elevatorNumber)
    {
        this.elevatorNumber = elevatorNumber;

        // Create as many buttons as there are floors 
		for (int i = 0; i < NUMBER_OF_FLOORS; i++) {
			elevatorButtons.add(new ElevatorButton(i));
		}

        // Create Datagram Socket on random port
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Failed to create Datagram Socket: " + e);
			e.printStackTrace();
		}
    }

    /** 
     * The elevator is in the Idle State
     * 
     * The elevator sends a packet to the scheduler to let it know it is:
     *      stopped
     *      currentFloor == destinationFloor
     *      No passengerDestinations
     * 
     * Waits until scheduler tells it to pick a new passenger up, through a packet that states:
     *      new destinationFloor
     *      new passengerDestinations
     * 
     */
    public void idle() {
        /** Send ElevatorPacket to tell scheduler we are in idle state (stopped, curr = dest, no passDests) */ 
        this.sendElevatorRequest();

        /** Wait for response from scheduler to move to new request */ 
        ElevatorPacket newFloorRequest = this.receiveSchedulerResponse();

        // TODO: What to do after receiving new floor service request (start moving, update vars, update externals)    
        /** Close Doors */
        this.doorClose();

        /** Start Moving to Pickup Passenger */
        if (currentFloor < newFloorRequest.getDestinationFloor()) {
            // Move up
            this.movingUp(newFloorRequest.getDestinationFloor());
        } else if (currentFloor > newFloorRequest.getDestinationFloor()) {
            // Move down
            this.movingUp(newFloorRequest.getDestinationFloor());
        }

        /** Stop Moving */

        /** Open Doors */

        /** Activate clicked button */

        /** Start Moving */
    }

    /** 
     * Elevator is in moving up state 
     */
    public void movingUp(int destinationFloor) {
        // Update the Destination Floor of the elevator
        this.destinationFloor = destinationFloor;
        // Update the direction of the elevator
        this.directionUp = true;

        // Start motor in Up direction
        motor.startMoving(true);
        // Set elevator moving state
        this.isMoving = true;

        // Update currentFloor and arrivalSensor as you are moving
        while (currentFloor < destinationFloor) {
            // Sleep for amount of time to move between floors
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}

            // Moved a floor up
            currentFloor++;
            // TODO: Tell arrival sensor

            // Send updated elevator request to scheduler
            this.sendElevatorRequest(); // TODO: Should activate serviceElevatorMovingRequest

            // Wait for response, to see if there are new users to service or not
            ElevatorPacket movingResponsePacket = this.receiveSchedulerResponse();
            // Check if there are any new passengerDestinations, if there is then stop 
            // TODO: check if any of the buttons for the passengerDestination floors are new. If yes then people wanna get on at this floor 
            // Let new people on by stopping, openning doors, then closing doors, then starting again
            for (int passengerDestination : movingResponsePacket.getPassengerDestinations()) {
                for (ElevatorButton button : elevatorButtons) {
                    if (button.getButtonFloor() == passengerDestination) {
                        if (button.getButtonState() == false) {
                            // Stop elevator
                            this.stopped();

                            // Open Doors
                            this.doorOpen();

                            // Close Doors
                            this.doorClose();

                            // Start again
                            motor.startMoving(true);
                        }
                    }
                }
            }


            
        }
    }

    /** 
     * Elevator is in moving down state 
     */
    public void movingDown(int destinationFloor) {

    }

    /** 
     * Elevator is in stopped state 
     */
    public void stopped() {
        motor.stopMoving();
    }

    /** 
     * Elevator is in door open state 
     * 
     * Opens the doors
     * Lets people on/off the elevator
     * Resets buttons and passengerDestinations for people getting off
     * Sets buttons and passengerDestinations for people that got on
     * 
     */
    public void doorOpen() {
        door.open();
    }

    /** 
     * Elevator is in door close state 
     * 
     */
    public void doorClose() {
        door.close();
    }

    public void run()
    {
        while(true){     
            // Start in Idle State
            idle();


        }
    }

    /** Old and most likely not useful anymore */
    public void moveToGetUser(int floor) {
        // Close the door
        if (door.getIsOpen()) {
            door.close();
            System.out.println("Elevator: Closing door on floor " + arrivalSensor.getFloor());
        }

        // Sleep for 1 sec
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException ex) {
            System.exit(1);
        }

        System.out.println("Elevator: Moving to floor " + floor);
        if (floor > arrivalSensor.getFloor()) {
            // Move up
            System.out.println("Elevator: Motor starting to move up");
            motor.startMoving(true);

            // Set arrival sensor
            // TODO: This needs to be done as the elevator moves between floors
            arrivalSensor.setFloor(floor);
        } else {
            // Move down
            System.out.println("Elevator: Motor starting to move down");
            motor.startMoving(false);

            // Set arrival sensor
            // TODO: This needs to be done as the elevator moves between floors
            arrivalSensor.setFloor(floor);
        }


        // Sleep for 1 sec
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException ex) {
            System.exit(1);
        }

        // Stop moving
        System.out.println("Elevator: Motor stopping");
        motor.stopMoving();

        // Open the door
        door.open();
        System.out.println("Elevator: Opening door on floor " + arrivalSensor.getFloor());

        // Sleep for 1 sec
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException ex) {
            System.exit(1);
        }
    }

	/** Elevator arrived at floor now servicing anyone waiting for exiting 
     * Returns true if there are any users that were services at the floor
     * Returns false if no one is to be services at this floor
    */
    /** Old and most likely not useful anymore */
	public void elevatorArrival(int floor, ArrayList<UserInput> floorRequests, ArrayList<UserInput> elevatorRequests) {
        // Someone is to be serviced, open the doors
        if (!floorRequests.isEmpty() || !elevatorRequests.isEmpty()) {
            // Open the door
            door.open();
            System.out.println("Elevator: Opening door on floor " + floor);

            // Sleep for 1 sec
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException ex) {
                System.exit(1);
            }
        } 

        // Get button selection from new user
        if (!floorRequests.isEmpty()) {
            // Simulate the user pressing the button
            for (UserInput user : floorRequests) {
                System.out.println("Elevator: Picked up user on floor " + floor);
                buttonPress(user.getCarButton());
            }
        }

        // Remove button selection from user exitting
        if (!elevatorRequests.isEmpty()) {
            // Simulate the user getting off the elevator
            for (UserInput user : elevatorRequests) {
                // Reset the button depending on what floor was pressed
                for (ElevatorButton button : elevatorButtons) {
                    if (button.getButtonFloor() == user.getCarButton()) {
                        System.out.println("Elevator: Dropped off user on floor " + floor);
                        button.reset();
                        break;
                    }
                }
            }
        }
	}

    /** Puts the button in its clicked state */
    public void buttonPress(int carButton) {
        // Activate the correct button depending on which floor was pressed
        for (ElevatorButton button : elevatorButtons) {
            if (button.getButtonFloor() == carButton) {
                // Turn on the button for that floor
                System.out.println("Elevator: User has pressed button to go to floor " + carButton);
                button.press();
                break;
            }
        }
    }

    /** Send a request to the scheduler to let it know the state of the elevator and ask what should be done */
	private void sendElevatorRequest() {
        // Get list of passenger destination for the elevator to add as passengerDestinations in the packet
        ArrayList<Integer> passengerDestinations = new ArrayList<Integer>();
        int i = 0;
        for (ElevatorButton button : elevatorButtons) {
            if (button.getButtonState() == true) {
                passengerDestinations.add(i);
            }
            i++;
        }

        // Create Elevator Packet
        ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengerDestinations);
        // Send Elevator Packet
        System.out.println("Elevator: Sending request to the scheduler");
        try {
            elevatorPacket.send(InetAddress.getLocalHost(), 69, sendReceiveSocket);
        } catch (UnknownHostException e) {
            System.out.println("Failed to send ElevatorPacket: " + e);
            e.printStackTrace();
        }
	}

    public ElevatorPacket receiveSchedulerResponse() {
        // Create Default Elevator Packet
        ElevatorPacket elevatorPacket = new ElevatorPacket(0, false, 0, 0, false, new ArrayList<Integer>());
        // Receive Elevator Packet
        System.out.println("Elevator: Waiting for Elevator Packet from Scheduler...");
        elevatorPacket.receive(sendReceiveSocket);

        return elevatorPacket;
    }

    public static void main(String[] args) {        
		// Create Elevator Thread
        Thread elevator = new Thread(new Elevator(1), "Elevator");

        // Start Threads
        elevator.start();
    }
}

/** Simulates the activities of the motor */
class Motor {
    private boolean isMoving = false; // Is the motor moving or not
    private boolean directionUp = false; // Which direction is the motor moving in

    // Starts the motor, in a specific direction
    public void startMoving(boolean directionUp) { // TODO: Should include decelerating and accelerating
        if (directionUp) {
            System.out.println("Motor: Starting to move Up");
            this.directionUp = true;
        } else {
            System.out.println("Motor: Starting to move Up");
            this.directionUp = false;
        }

        isMoving = true;
    }

    // Stops the motor
    public void stopMoving() {
        System.out.println("Motor: Stopping");
        isMoving = false;
    }

    // Returns whether the motor is moving or not
    public boolean getIsMoving() {
        return isMoving;
    }

    // Returns the direction the motor is moving
    public boolean getDirectionUp() {
        return directionUp;
    }
}

/** Simulates the activities of the door */
class Door {
    private boolean isOpen = false;

    // Sets isOpen to true
    public void open() { // TODO: Should take time for both
        System.out.println("Door: Opening");
        isOpen = true;
    }

    // Sets isOpen to false
    public void close() {
        System.out.println("Door: Closing");
        isOpen = false;
    }

    // Returns wether the door is open or not
    public boolean getIsOpen() {
        return isOpen;
    }
}

class ArrivalSensor {
    private int floor = 0;

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}

/** Used to simulate a buttons on the Elevator
 *  Each button has a floor number associated to it
 *  Each button has a lamp associated to it
 */
class ElevatorButton {
	private boolean buttonState = false; // Defines the state of the button, On or Off
	private ElevatorLamp buttonLamp = new ElevatorLamp(); // Used to hold the associated lamp of the button
	private int buttonFloor = 0; // Indicates the floor the button is associated to

	public ElevatorButton(int floor) {
		this.buttonFloor = floor;
	}

	// Sets the button state On, and turns on the Lamp
	public void press() {
        System.out.println("ElevatorButton: Button for floor " + buttonFloor + " has been clicked");
		buttonState = true;
		buttonLamp.turnOn();
	}

	// Sets the button state to Off and turns off the Lamp
	public void reset() {
        System.out.println("ElevatorButton: Button for floor " + buttonFloor + " has been reset");
		buttonState = false;
		buttonLamp.turnOff();
	}

	// Returns the current state of the Button
	public boolean getButtonState() {
		return buttonState;
	}

	// Returns the floor of the Button
	public int getButtonFloor() {
		return buttonFloor;
	}
}

/** Used to simulate the lamp for the elevator buttons */
class ElevatorLamp {
	private boolean lampState = false; // Defines the state of the lamp, On or Off

	// Sets lamp state to On
	public void turnOn() {
        System.out.println("ElevatorLamp: Turned on");
		lampState = true;
	}

	// Sets lamp state to Off
	public void turnOff() {
        System.out.println("ElevatorLamp: Turned off");
		lampState = false;
	}

	// Returns the lamp state
	public boolean getLampState() {
		return lampState;
	}
}
