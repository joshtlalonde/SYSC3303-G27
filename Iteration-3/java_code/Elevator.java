import java.io.*;
import java.net.*;
import java.util.*;

class Elevator implements Runnable
{
    static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building
    static final int IDLE = 0; // Value of IDLE state 
    static final int MOVING_UP = 1; // Value of MOVING_UP state 
    static final int MOVING_DOWN = 2; // Value of MOVING_DOWN state 
    static final int STOPPED = 3; // Value of STOPPED state 
    static final int DOOR_OPEN = 4; // Value of DOOR_OPEN state 
    static final int DOOR_CLOSE = 5; // Value of DOOR_CLOSE state 

    private int currentState = IDLE; // Holds the current State of the elevator
    private DatagramSocket sendReceiveSocket; // Socket that Elevator sends and receives packets from

    private int elevatorNumber; // Number of the elevator
    private boolean isMoving; // Is the elevator moving
    private int currentFloor; // Current Floor the elevator is on
    private int destinationFloor; // Floor the elevator is moving to
    private boolean directionUp; // Direction elevator is moving in
    private ArrayList<Integer> passengerDestinations = new ArrayList<Integer>(); // Holds an array of all of the passenger destinations. This can also be used to determine how many people are on the elevator

    private DirectionLamp directionLamp = new DirectionLamp(); // Lamp to indicate direction moving and floor location
    private ArrivalSensor arrivalSensor = new ArrivalSensor(); // Sensor to indicate when an elevator is approaching a floor
    private Motor motor = new Motor(); // The elevators motor, controls motion of elevator
    private Door door = new Door(); // Elevators door
    private ArrayList<ElevatorButton> elevatorButtons = new ArrayList<ElevatorButton>(); // Holds the buttons for each of the floors (up and down)
    
    public Elevator(int elevatorNumber)
    {
        /** Start in IDLE state */
        this.currentState = IDLE;

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
     * The elevator sends a packet to the scheduler to let it know it is idle
     * Waits until scheduler tells it to pick a new passenger up
     * Moves to MOVING state, either up or down
     * 
     */
    public void idle() {
        System.out.println("Elevator: Entering IDLE state");

        /** Send ElevatorPacket to tell scheduler we are in idle state (stopped, curr = dest, no passDests) */ 
        this.sendElevatorRequest();

        /** Wait for response from scheduler to move to new request */ 
        ElevatorPacket newFloorRequest = this.receiveSchedulerResponse();

        /** Set the destination floor to go to */
        destinationFloor = newFloorRequest.getDestinationFloor();

        /** Start Moving to Pickup Passenger */
        if (currentFloor < destinationFloor) {
            // Move up
            currentState = MOVING_UP;
        } else if (currentFloor > destinationFloor) {
            // Move down
            currentState = MOVING_DOWN;
        }
    }

    /** 
     * Elevator is in moving up state 
     * 
     * Moves to destinationFloor
     * Between every floor it sends a packet to the scheduler to ask if there are any users to pick up
     * If there are users it stops, if not then continue
     * When reaching destination it moves to stopped state
     * 
     */
    public void movingUp() {
        System.out.println("Elevator: Entering MOVING_UP state");

        // Update the direction of the elevator
        directionUp = true;

        // Start motor in Up direction
        motor.startMoving(true);
        // Set elevator moving state
        isMoving = true;

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
            // Tell arrival sensor what floor we're on
            arrivalSensor.setFloor(currentFloor);
            // Send updated elevator request to scheduler
            this.sendElevatorRequest(); // TODO: Should activate serviceElevatorMovingRequest

            // Wait for response, to see if there are new users to service or not
            ElevatorPacket movingResponsePacket = this.receiveSchedulerResponse();

            // Check if scheduler wants us to stop
            if (!movingResponsePacket.getIsMoving()) {
                // Move to stopped state
                currentState = STOPPED;
            }
        }

        /** Move to stopped state */
        currentState = STOPPED;
    }

    /** 
     * Elevator is in moving down state 
     * 
     * Moves to destinationFloor
     * Between every floor it sends a packet to the scheduler to ask if there are any users to pick up
     * If there are users it moves to stopped state, if not it continues
     * When reaching destination it moves to stopped state
     * 
     */
    public void movingDown() {
        System.out.println("Elevator: Entering MOVING_DOWN state");

        // Update the direction of the elevator
        directionUp = false;

        // Start motor in Up direction
        motor.startMoving(false);
        // Set elevator moving state
        isMoving = true;

        // Update currentFloor and arrivalSensor as you are moving
        while (currentFloor > destinationFloor) {
            // Sleep for amount of time to move between floors
			try {
				Thread.sleep(2000); // TODO: Must be the times we determined
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}

            // Moved a floor down
            currentFloor--;
            // Tell arrival sensor what floor we're on
            arrivalSensor.setFloor(currentFloor);
            // Send updated elevator request to scheduler
            this.sendElevatorRequest(); // TODO: Should activate serviceElevatorMovingRequest

            // Wait for response, to see if there are new users to service or not
            ElevatorPacket movingResponsePacket = this.receiveSchedulerResponse();

            // Check if scheduler wants us to stop
            if (!movingResponsePacket.getIsMoving()) {
                // Move to stopped state
                currentState = STOPPED;
            }
        }

        /** Move to stopped state */
        currentState = STOPPED;
    }

    /** 
     * Elevator is in stopped state 
     * 
     * Stops the motor
     * Sends message to scheduler update state
     * 
     */
    public void stopped() {
        System.out.println("Elevator: Entering STOPPED state");

        motor.stopMoving();

        /** Sleep for deceleration time */
        try {
            Thread.sleep(2000); // TODO: Must be the times we determined
        } catch (InterruptedException e ) { // TODO: Should this be in motor.stopMoving
            e.printStackTrace();
            System.exit(1);
        }
        
        /** Tell scheduler that elevator is in stopped state */
        this.sendElevatorRequest();

        /** Wait for response from scheduler */ 
        this.receiveSchedulerResponse();
        
        /** Move to Door open State */
        currentState = DOOR_OPEN;
    }

    /** 
     * Elevator is in door open state 
     * 
     * Opens the doors
     * Lets people on/off the elevator
     * Resets buttons and passengerDestinations for people getting off
     * 
     */
    public void doorOpen() {
        System.out.println("Elevator: Entering DOOR_OPEN state");

        /** Reset button for floor */
        this.buttonReset(currentFloor);
        
        /** Open the door */
        door.open();

        /** Tell scheduler that elevator is in door open state */
        this.sendElevatorRequest();

        /** Wait for response from scheduler saying who got off the elevator */ 
        ElevatorPacket doorOpenResponse = this.receiveSchedulerResponse();

        /** Update the passengerDestinations based on the changes made in the scheduler of who got off */
        passengerDestinations = doorOpenResponse.getPassengerDestinations();

        /** Move to DOOR_CLOSE State */
        currentState = DOOR_CLOSE;
    }

    /** 
     * Elevator is in door close state 
     * 
     * Elevator closes door 
     * Sends packet to update state
     * Updates passengerDestinations for people getting on 
     * Sets buttons to on for those destinations
     * 
     */
    public void doorClose() {
        System.out.println("Elevator: Entering DOOR_CLOSE state");

        /** Close the door */
        door.close();

        /** Tell scheduler that elevator is in door close state */
        this.sendElevatorRequest();

        /** Wait for scheulder to say who got on the elevator */
        ElevatorPacket doorCloseResponse = this.receiveSchedulerResponse();
        
        /** Walk through the passenger destinations updated from the scheduler */
        for (int passengerDestination : doorCloseResponse.getPassengerDestinations()) {
            /** Add Passenger Destinations to array */
            this.passengerDestinations.add(passengerDestination);

            /** Update buttons clicked for anyone that got on the elevator */
            for (ElevatorButton button : elevatorButtons) {
                if (button.getButtonFloor() == passengerDestination) {
                    this.buttonPress(button.getButtonFloor());
                }
            }
        }

        /** Change current State to IDLE */
        currentState = IDLE;
    }
    
    public void run()
    {
        while(true){   
            if (currentState == IDLE) {
                this.idle();
            } 
            else if (currentState == MOVING_UP) {
                this.movingUp();
            } 
            else if (currentState == MOVING_DOWN) {
                this.movingDown();
            } 
            else if (currentState == STOPPED) {
                this.stopped();
            } 
            else if (currentState == DOOR_OPEN) {
                this.doorOpen();
            } 
            else if (currentState == DOOR_CLOSE) {
                this.doorClose();
            } 
        }
    }

    /** Puts the button in its unclicked state */
    public void buttonReset(int carButton) {
        // Deactivate the correct button depending on which floor was pressed
        for (ElevatorButton button : elevatorButtons) {
            if (button.getButtonFloor() == carButton) {
                // Turn off the button for that floor
                System.out.println("Elevator: Resetting the button for floor " + carButton);
                button.reset();
                break;
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
        // Create Elevator Packet
        // ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, this.passengerDestinations, this.currentState);
        ElevatorPacket elevatorPacket = new ElevatorPacket(9, isMoving, currentFloor, destinationFloor, directionUp, this.passengerDestinations, this.currentState);
        // Send Elevator Packet
        System.out.println("Elevator: Sending request to the scheduler");
        try {
            elevatorPacket.send(InetAddress.getLocalHost(), 69, sendReceiveSocket, true);
        } catch (UnknownHostException e) {
            System.out.println("Failed to send ElevatorPacket: " + e);
            e.printStackTrace();
        }
	}

    public ElevatorPacket receiveSchedulerResponse() {
        // Create Default Elevator Packet
        ElevatorPacket elevatorPacket = new ElevatorPacket();
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
        System.out.println("ArrivalSensor: On Floor " + floor);
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
        if (!buttonState) {
            System.out.println("ElevatorButton: Button for floor " + buttonFloor + " has been clicked");
            buttonState = true;
            buttonLamp.turnOn();
        }
	}

	// Sets the button state to Off and turns off the Lamp
	public void reset() {
        if (buttonState) {
            System.out.println("ElevatorButton: Button for floor " + buttonFloor + " has been reset");
            buttonState = false;
            buttonLamp.turnOff();
        }
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
