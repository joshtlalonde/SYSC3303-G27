import java.net.*;
import java.util.*;

class Elevator implements Runnable
{
    static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building

    private Elevator_State currentState = Elevator_State.IDLE; // Holds the current State of the elevator
    private DatagramSocket sendReceiveSocket; // Socket that Elevator sends and receives packets from

    private int elevatorNumber; // Number of the elevator
    private boolean isMoving; // Is the elevator moving
    private int currentFloor; // Current Floor the elevator is on
    private int destinationFloor; // Floor the elevator is moving to
    private boolean directionUp; // Direction elevator is moving in
    private ArrayList<UserInput> passengers = new ArrayList<UserInput>(); // Holds the array of passengers that are on the elevator 

    private DirectionLamp directionLamp = new DirectionLamp(); // Lamp to indicate direction moving and floor location
    private ArrivalSensor arrivalSensor = new ArrivalSensor(); // Sensor to indicate when an elevator is approaching a floor
    private Motor motor = new Motor(); // The elevators motor, controls motion of elevator
    private Door door = new Door(); // Elevators door
    private ArrayList<ElevatorButton> elevatorButtons = new ArrayList<ElevatorButton>(); // Holds the buttons for each of the floors (up and down)
    
    public Elevator(int elevatorNumber)
    {
        /** Start in IDLE state */
        this.currentState = Elevator_State.IDLE;

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
        System.out.println(Thread.currentThread().getName() + ": Entering IDLE state"); //probably shouldn't say this as it could be in Idle from the beginning.

        /** Turn off the direction lamp */
        directionLamp.turnOff();

        /** Send ElevatorPacket to tell scheduler we are in idle state (stopped, curr = dest, no passDests) */ 
        this.sendElevatorRequest();

        /** Wait for response from scheduler to move to new request */ 
        ElevatorPacket newFloorRequest = this.receiveSchedulerResponse();

        /** Set the destination floor to go to */
        destinationFloor = newFloorRequest.getDestinationFloor();

        /** Get the requesting passenger */
        passengers = newFloorRequest.getPassengers();

        /** Flag to state that there is no one waiting for an elevator */
        if (destinationFloor == -1) {
            // Stay in IDLE state
            currentState = Elevator_State.IDLE;
            return;
        }

        // /** Start Moving to Pickup Passenger */
        if (currentFloor < destinationFloor) {
            // Move up
            currentState = Elevator_State.MOVING_UP;
        } else if (currentFloor > destinationFloor) {
            // Move down
            currentState = Elevator_State.MOVING_DOWN;
        } else if (currentFloor == destinationFloor) {
            // Move to stopped
            currentState = Elevator_State.STOPPED;
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
        System.out.println(Thread.currentThread().getName() + ": Entering MOVING_UP state");

        // Update the direction of the elevator
        directionUp = true;
        // Set DirectionLamp direction
        directionLamp.setDirectionUp(directionUp);
        // Start motor in Up direction
        motor.startMoving(directionUp);
        // Set elevator moving state
        isMoving = true;

        // Update currentFloor and arrivalSensor as you are moving
        while (currentFloor < destinationFloor) {
            /** Check if any passengers have a HARD_FAULT */
            for (UserInput passenger : passengers) {
                if(passenger.getHardFault()){
                    for(int i = 1; i <= 10; i++){
                        System.out.println("Elevator stuck for " + i + " seconds");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Elevator is stuck, Servicing elevator...");
                    currentState= Elevator_State.HARD_FAULT;
                    return;
                }
            }

            /** Sleep for 2 seconds for moving between Floors */
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
            this.sendElevatorRequest();

            // Wait for response, to see if there are new users to service or not
            ElevatorPacket movingResponsePacket = this.receiveSchedulerResponse();

            // Check if scheduler wants us to stop
            if (!movingResponsePacket.getIsMoving()) {
                // Move to stopped state
                currentState = Elevator_State.STOPPED;
                return;
            }
        }

        /** Move to stopped state */
        currentState = Elevator_State.STOPPED;
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
        System.out.println(Thread.currentThread().getName() + ": Entering MOVING_DOWN state");

        // Update the direction of the elevator
        directionUp = false;
        // Set DirectionLamp direction
        directionLamp.setDirectionUp(directionUp);
        // Start motor in Up direction
        motor.startMoving(directionUp);
        // Set elevator moving state
        isMoving = true;

        // Update currentFloor and arrivalSensor as you are moving
        while (currentFloor > destinationFloor) {
            /** Check if any passengers have a HARD_FAULT */
            for (UserInput passenger : passengers) {
                if(passenger.getHardFault()){
                    for(int i = 1; i <= 10; i++){
                        System.out.println("Elevator stuck for " + i + " seconds");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Elevator is stuck, Servicing elevator...");
                    currentState= Elevator_State.HARD_FAULT;
                    return;
                }
            }

            /** Sleep for 2 seconds for moving between Floors */
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}

            // Moved a floor down
            currentFloor--;
            // Tell arrival sensor what floor we're on
            arrivalSensor.setFloor(currentFloor);
            // Send updated elevator request to scheduler
            this.sendElevatorRequest();

            // Wait for response, to see if there are new users to service or not
            ElevatorPacket movingResponsePacket = this.receiveSchedulerResponse();

            // Check if scheduler wants us to stop
            if (!movingResponsePacket.getIsMoving()) {
                // Move to stopped state
                currentState = Elevator_State.STOPPED;
                return;
            }
        }

        /** Move to stopped state */
        currentState = Elevator_State.STOPPED;
    }

    /** 
     * Elevator is in stopped state 
     * 
     * Stops the motor
     * Sends message to scheduler update state
     * 
     */
    public void stopped() {
        System.out.println(Thread.currentThread().getName() + ": Entering STOPPED state");

        // Stop the elevator motor
        motor.stopMoving();
        // Set elevator moving state
        isMoving = false;
        
        /** Tell scheduler that elevator is in stopped state */
        this.sendElevatorRequest();

        /** Wait for response from scheduler */ 
        ElevatorPacket stoppedResponsePacket = this.receiveSchedulerResponse();

        /** Update Direction */
        directionUp = stoppedResponsePacket.getDirectionUp();
        
        /** Move to Door open State */
        currentState = Elevator_State.DOOR_OPEN;
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
        System.out.println(Thread.currentThread().getName() + ": Entering DOOR_OPEN state");

        /** Reset button for floor */
        this.buttonReset(currentFloor);
        
        /** Open the door */
        door.open();

        /** Tell scheduler that elevator is in door open state */
        this.sendElevatorRequest();

        /** Wait for response from scheduler saying who got off the elevator */ 
        ElevatorPacket doorOpenResponse = this.receiveSchedulerResponse();

        /** Update the passengerDestinations based on the changes made in the scheduler of who got off */
        passengers = doorOpenResponse.getPassengers();

        /** Move to DOOR_CLOSE State */
        currentState = Elevator_State.DOOR_CLOSE;
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
        System.out.println(Thread.currentThread().getName() + ": Entering DOOR_CLOSE state");

        /** Close the door */
        door.close();

        /** Tell scheduler that elevator is in door close state */
        this.sendElevatorRequest();

        /** Wait for scheulder to say who got on the elevator */
        ElevatorPacket doorCloseResponse = this.receiveSchedulerResponse();

        /** Update the new Passenger Destinations array */
        passengers = doorCloseResponse.getPassengers();
        
        /** Walk through the passenger destinations updated from the scheduler */
        for (UserInput passenger : passengers) {
            /** Update buttons clicked for anyone that got on the elevator */
            for (ElevatorButton button : elevatorButtons) {
                if (button.getButtonFloor() == passenger.getDestinationFloor()) {
                    this.buttonPress(button.getButtonFloor());
                }
            }
        }

        /**
         * Decides which state the elevator should move to
         */
        if (currentFloor == destinationFloor && passengers.isEmpty()) {
            /** Change current State to IDLE */
            currentState = Elevator_State.IDLE;
        } else if (directionUp) {
            /** Change the current State to MOVING_UP */
            currentState = Elevator_State.MOVING_UP;
        } else if (!directionUp) {
            /** Change the current State to MOVING_DOWN */
            currentState = Elevator_State.MOVING_DOWN;
        }
        
        /** 
         * Checks for Door Fault
         * And updates the destination floor
        */
        for (UserInput passenger : passengers) {
            if (passenger.getDoorFault() == true) {
                for(int i = 1;i<=5;i++){
                    System.out.println("Door stuck for " + i + " seconds");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Door is stuck, Servicing Door...");
                currentState= Elevator_State.DOOR_FAULT;
            }
			
            if (directionUp) {
                if (destinationFloor < passenger.getDestinationFloor()) {
                    /** Update destinationFloor if a passenger destinationFloor is larger than the current one */
                    destinationFloor = passenger.getDestinationFloor();
                }
            } else {
                if (destinationFloor > passenger.getDestinationFloor()) {
                    /** Update destinationFloor if a passenger destinationFloor is smaller than the current one */
                    destinationFloor = passenger.getDestinationFloor();
                }
            }
        }
    }

    public void doorFault() {
        System.out.println(Thread.currentThread().getName() + ": Entering DOOR_FAULT state");
		
        //Tells the scheduler that elevator is in door fault state
        this.sendElevatorRequest();
        //Waits for the scheduler to respond
        ElevatorPacket doorFaultResponse = this.receiveSchedulerResponse();
        // Update the passengers
        passengers = doorFaultResponse.getPassengers();
            
        //Change current state to stopped
        currentState = Elevator_State.STOPPED;        
    }

    public void hardFault() {
        System.out.println(Thread.currentThread().getName() + ": Entering HARD_FAULT state");
		
        //Tells the scheduler that elevator is in door fault state
        this.sendElevatorRequest();
            
        //Waits for the scheduler to respond
        ElevatorPacket hardFaultResponse = this.receiveSchedulerResponse();

        // Set the updated passengers
        passengers = hardFaultResponse.getPassengers();
            
        for (int i = 3; i > 0; i--) {
            System.out.println("Self Destruct in " + i);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("BOOOOOOOOOOOOOOM");

        //Terminate thread
        currentState = Elevator_State.BOOM;
    }
    
    public void run()
    {
        while(true){ 
            switch(currentState) {
				case IDLE:
                    this.idle();
					break;
				case MOVING_UP:
                    this.movingUp();
					break;
				case MOVING_DOWN:
                    this.movingDown();
					break;
				case STOPPED:
                    this.stopped();
					break;
				case DOOR_OPEN:
                    this.doorOpen();
					break;
				case DOOR_CLOSE:
                    this.doorClose();
					break;
                case DOOR_FAULT:
                    this.doorFault();
					break;
                case HARD_FAULT:
                    this.hardFault();
					break;
                case BOOM:
                    return;
			}
        }
    }

    /** Puts the button in its unclicked state */
    public void buttonReset(int carButton) {
        // Deactivate the correct button depending on which floor was pressed
        for (ElevatorButton button : elevatorButtons) {
            if (button.getButtonFloor() == carButton) {
                // Turn off the button for that floor
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
                button.press();
                break;
            }
        }
    }

    /** Send a request to the scheduler to let it know the state of the elevator and ask what should be done */
	public void sendElevatorRequest() {
        // Create Elevator Packet
        ElevatorPacket elevatorPacket = new ElevatorPacket(elevatorNumber, isMoving, currentFloor, destinationFloor, directionUp, passengers, currentState);
        // Send Elevator Packet
        System.out.println(Thread.currentThread().getName() + ": Sending request to the scheduler");
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
        System.out.println(Thread.currentThread().getName() + ": Waiting for Elevator Packet from Scheduler...");
        elevatorPacket.receive(sendReceiveSocket);

        return elevatorPacket;
    }

    public static void main(String[] args) {        
		// Create Elevator Thread
        Thread elevator1 = new Thread(new Elevator(1), "Elevator1");
        Thread elevator2 = new Thread(new Elevator(2), "Elevator2");

        // Start Threads
        elevator1.start();
        elevator2.start();
    }
}

/** Simulates the activities of the motor */
class Motor {
    private boolean isMoving = false; // Is the motor moving or not
    private boolean directionUp = false; // Which direction is the motor moving in

    // Starts the motor, in a specific direction
    public void startMoving(boolean directionUp) { 
        if (directionUp) {
            System.out.println("Motor: Starting to move Up");
            this.directionUp = true;
        } else {
            System.out.println("Motor: Starting to move Down");
            this.directionUp = false;
        }

        /** Three Seconds for Acceleration */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isMoving = true;
    }

    // Stops the motor
    public void stopMoving() {
        System.out.println("Motor: Stopping");

        /** Three Seconds for Deceleration */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
    public void open() { 
        System.out.println("Door: Opening");

        /** Four Seconds for Opening Door */
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isOpen = true;
    }

    // Sets isOpen to false
    public void close() {
        System.out.println("Door: Closing");

        /** Four Seconds for Closing Door */
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

class DirectionLamp {
	private boolean directionLampState = false;
	private boolean directionUp = false;

    // Return Direction Lamp state
	public boolean getState() {
		return directionLampState;
	}

    // Return Direction Lamp direction
	public boolean getDirectionUp() {
		return directionUp;
	}

    // Turn off the Direction Lamp
    public void turnOff() {
        System.out.println("DirectionLamp: Turned off");
		this.directionLampState = false;
	}

    // Turn on and set the direction of the Direction Lamp
	public void setDirectionUp(boolean direction) {
        System.out.println("DirectionLamp: Turned on in " + (direction ? "Up" : "Down") + " direction");
        this.directionLampState = true;
		this.directionUp = direction;
	}
}