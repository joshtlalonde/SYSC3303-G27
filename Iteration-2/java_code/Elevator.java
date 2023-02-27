import java.io.*;
import java.util.*;

class Elevator implements Runnable
{
    private Scheduler scheduler; // Scheduler that the Elevator is associated to
    private int destinationFloor; // Floor the elevator is moving to
    private DirectionLamp directionLamp; // Lamp to indicate direction moving and floor location
    private ArrivalSensor arrivalSensor = new ArrivalSensor(); // Sensor to indicate when an elevator is approaching a floor
    private Motor motor = new Motor(); // The elevators motor, controls motion of elevator
    private Door door = new Door(); // Elevators door
    private ArrayList<ElevatorButton> elevatorButton = new ArrayList<ElevatorButton>(); // Holds the buttons for each of the floors (up and down)
    
    public Elevator(Scheduler scheduler, DirectionLamp directionLamp)
    {
        this.scheduler = scheduler;
        this.directionLamp = directionLamp;

        // Create as many buttons as there are floors 
		for (int i = 0; i < scheduler.getNumberOfFloors(); i++) {
			elevatorButton.add(new ElevatorButton(i));
		}
    }

    public void run()
    {
        while(true){       
            // TODO: This is only Handling one request at a time for the moment
            System.out.println("Elevator: Ready on floor " + arrivalSensor.getFloor());
            ArrayList<UserInput> userInput = scheduler.serviceFloorRequest(arrivalSensor.getFloor(), motor.getDirectionUp());

            // Get all elevator requests
            ArrayList<UserInput> elevatorRequests = scheduler.serviceElevatorRequest(arrivalSensor.getFloor());
            for (UserInput elevatorRequest : elevatorRequests) {
                // Simulate user exiting elevator
                elevatorArrival(elevatorRequest.getCarButton());
                System.out.println("Elevator: User exiting on floor " + arrivalSensor.getFloor());
            }

            // If there is a user on this floor then continue 
            // TODO: If no users are on the current floor should be handled much better than this
            boolean userOnFloor = false;
            for (UserInput user : userInput) {
                if (user.getFloor() == arrivalSensor.getFloor()) {
                    break;
                }
            }

            // Move to the floor to pick up the user
            if (!userOnFloor) { 
                // Close the door
                door.close();
                System.out.println("Elevator: Closing door on floor " + arrivalSensor.getFloor());

                for (UserInput user : userInput) {
                    System.out.println("Elevator: Moving to floor " + user.getFloor() + " to pick up waiting user");
                    if (user.getFloor() > arrivalSensor.getFloor()) {
                        // Move up
                        System.out.println("Elevator: Motor starting to move up");
                        motor.startMoving(true);

                        // Set arrival sensor
                        // TODO: This needs to be done as the elevator moves between floors
                        arrivalSensor.setFloor(user.getFloor());
                        break;
                    } else {
                        // Move down
                        System.out.println("Elevator: Motor starting to move down");
                        motor.startMoving(false);

                        // Set arrival sensor
                        // TODO: This needs to be done as the elevator moves between floors
                        arrivalSensor.setFloor(user.getFloor());
                        break;
                    }
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
                System.out.println("Elevator: Openning door on floor " + arrivalSensor.getFloor());
            } 

            // Simulate the user pressing the button
            for (UserInput user : userInput) {
                System.out.println("Elevator: Picked up user on floor " + arrivalSensor.getFloor());
                for (ElevatorButton button : elevatorButton) {
                    if (button.getButtonFloor() == user.getCarButton()) {
                        button.press();
                        System.out.println("Elevator: User has pressed button to go to floor " + user.getCarButton());
                        // Notify the scheduler
                        scheduler.addElevatorRequest(user);
                        continue;
                    }
                }
            }

            // Close the door
            door.close();
            System.out.println("Elevator: Closing door on floor " + arrivalSensor.getFloor());

            ElevatorButton buttonToService = null;
            for (ElevatorButton button : elevatorButton) {
                // Service the first button that is clicked
                // TODO: This will need to be changed
                if (button.getButtonState()) {
                    buttonToService = button;
                }
            }

            // Start to move the elevator
            if (arrivalSensor.getFloor() == scheduler.getNumberOfFloors() - 1 || (arrivalSensor.getFloor() - buttonToService.getButtonFloor()) > 0) {
                // Motor start moving down
                System.out.println("Elevator: Motor starting to move down");
                motor.startMoving(false);
            } else if (arrivalSensor.getFloor() == 0 || (arrivalSensor.getFloor() - buttonToService.getButtonFloor()) < 0) {
                // Motor start moving up
                System.out.println("Elevator: Motor starting to move up");
                motor.startMoving(true);
            }

            // Set arrival sensor to next floor
            arrivalSensor.setFloor(arrivalSensor.getFloor() + 1);

        }
    }

    // /** Service any requests on this floor */
    // public void serviceRequests(int floor, boolean directionUp) {
    //     // Get all floor requests
    //     ArrayList<UserInput> floorRequests = scheduler.serviceFloorRequest(floor, directionUp);
    //     for (UserInput floorRequest : floorRequests) {
    //         // Add each of the elevator requests to the scheduler
    //         buttonPress(floorRequest);
    //     }

    //     // Get all elevator requests
    //     ArrayList<UserInput> elevatorRequests = scheduler.serviceElevatorRequest(floor);
    //     for (UserInput elevatorRequest : elevatorRequests) {
    //         elevatorArrival(elevatorRequest.getCarButton());
    //     }
    // }

	/** User exits elevator, button is reset */
	public void elevatorArrival(int floor) {
		// Reset the button depending on what floor was pressed
		for (ElevatorButton button : elevatorButton) {
			if (button.getButtonFloor() == floor) {
				button.reset();
				break;
			}
		}
	}

    /** Sets the button in its clicked state
     * As well as sending a notification to the scheduler
     */
    public void buttonPress(UserInput userInput) {
        // Activate the correct button depending on which floor was pressed
        for (ElevatorButton button : elevatorButton) {
            if (button.getButtonFloor() == userInput.getCarButton()) {
                // Turn on the button for that floor
                button.press();
                break;
            }
        }

		// Send request to scheduler
		sendRequest(userInput);
    }

    /** Sends request to the Scheduler */
	private void sendRequest(UserInput userInput) {
		// Adds the elevator request to the scheduler
		scheduler.addElevatorRequest(userInput);
		System.out.println("Elevator: Added " + userInput + " into the scheduler");
	}
}

/** Simulates the activities of the motor */
class Motor {
    private boolean isMoving = false; // Is the motor moving or not
    private boolean directionUp = false; // Which direction is the motor moving in

    // Starts the motor, in a specific direction
    public void startMoving(boolean directionUp) { // TODO: Should include decelerating and accelerating
        if (directionUp) {
            this.directionUp = true;
        } else {
            this.directionUp = false;
        }

        isMoving = true;
    }

    // Stops the motor
    public void stopMoving() {
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
        isOpen = true;
    }

    // Sets isOpen to false
    public void close() {
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
		buttonState = true;
		buttonLamp.turnOn();
	}

	// Sets the button state to Off and turns off the Lamp
	public void reset() {
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
		lampState = true;
	}

	// Sets lamp state to Off
	public void turnOff() {
		lampState = false;
	}

	// Returns the lamp state
	public boolean getLampState() {
		return lampState;
	}
}
