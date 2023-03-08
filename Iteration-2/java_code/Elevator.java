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
    private ArrayList<elevatorButtons> elevatorButtons = new ArrayList<elevatorButtons>(); // Holds the buttons for each of the floors (up and down)
    
    public Elevator(Scheduler scheduler, DirectionLamp directionLamp)
    {
        this.scheduler = scheduler;
        this.directionLamp = directionLamp;

        // Create as many buttons as there are floors 
		for (int i = 0; i < scheduler.getNumberOfFloors(); i++) {
			elevatorButtons.add(new elevatorButtons(i));
		}
    }

    public void run()
    {
        while(true){
            System.out.println("Elevator: Ready on floor " + arrivalSensor.getFloor());
            UserInput userInput = scheduler.get();
            
            System.out.println("Elevator: Moved to " + userInput.getCarButton() + " floor");
            
            // Set the floor elevator is on
            arrivalSensor.setFloor(userInput.getCarButton());
            // Set direction elevator is moving
            motor.startMoving(userInput.getFloorButtonUp());

            // Sleep for 1 second
            try {
	            Thread.sleep(1000); 
	        } catch (InterruptedException e) {
			System.exit(1);
		 }
        }
    }

//     public void run()
//     {
//         while(true){       
//             // TODO: This is only Handling one request at a time for the moment
//             // Elevator has arrived at a floor
// //            System.out.println("Elevator: Ready on floor " + arrivalSensor.getFloor());
//             ArrayList<UserInput> floorRequests = getFloorRequests(arrivalSensor.getFloor(), motor.getDirectionUp());
//             ArrayList<UserInput> elevatorRequests = getElevatorRequests(arrivalSensor.getFloor());

//             // No users are waiting to be serviced on this floor
//             boolean isEmpty = true;
//             if (floorRequests.isEmpty() && elevatorRequests.isEmpty()) {
//                 for (elevatorButtons button : elevatorButtons) {
//                     // Continue on if a user is in the elevator
//                     if (button.getButtonState() == true) {
//                         isEmpty = false;
//                     }
//                 }

//                 if (isEmpty) {
//                     // Check if anyone wants to go up
//                     for (int i = 0; i < scheduler.getNumberOfFloors(); i++) {
//                         floorRequests = getFloorRequests(i, true);
//                         if (!floorRequests.isEmpty()) {
//                             // Move to user to pick them up 
//                             moveToGetUser(i);
//                             break;
//                         }
//                     }

//                     // Check if anyone wants to go down
//                     if (floorRequests.isEmpty()) {
//                         for (int i = scheduler.getNumberOfFloors(); i >= 0; i--) {
//                             floorRequests = getFloorRequests(i, false);
//                             if (!floorRequests.isEmpty()) {
//                                 // Move to user to pick them up 
//                                 moveToGetUser(i);
//                                 break;
//                             }
//                         }
//                     }   
//                 }
//             }

//             elevatorArrival(arrivalSensor.getFloor(), floorRequests, elevatorRequests);

//             // Close the door
//             if (door.getIsOpen()) {
//                 door.close();
//                 System.out.println("Elevator: Closing door on floor " + arrivalSensor.getFloor());
//             }

//             // Sleep for 1 sec
//             try {
//                 Thread.sleep(1000); 
//             } catch (InterruptedException ex) {
//                 System.exit(1);
//             }

            
//             elevatorButtons buttonToService = null;
//             for (elevatorButtons button : elevatorButtons) {
//                 // Service the first button that is clicked
//                 // TODO: This will need to be changed
//                 if (button.getButtonState()) {
//                     buttonToService = button;
//                 }
//             }

//             if (buttonToService != null) {
//                 // Start to move the elevator
//                 if (arrivalSensor.getFloor() == scheduler.getNumberOfFloors() - 1 || (arrivalSensor.getFloor() - buttonToService.getButtonFloor()) > 0) {
//                     // Motor start moving down
//                     System.out.println("Elevator: Motor starting to move down");
//                     // Set arrival sensor to next floor
//                     arrivalSensor.setFloor(arrivalSensor.getFloor() - 1);
//                     motor.startMoving(false);
//                 } else if (arrivalSensor.getFloor() == 0 || (arrivalSensor.getFloor() - buttonToService.getButtonFloor()) < 0) {
//                     // Motor start moving up
//                     System.out.println("Elevator: Motor starting to move up");
//                     motor.startMoving(true);
//                     // Set arrival sensor to next floor
//                     arrivalSensor.setFloor(arrivalSensor.getFloor() + 1);
//                 }
//             }
//         }
//     }

    public ArrayList<UserInput> getFloorRequests(int floor, boolean directionUp) {
        return scheduler.serviceFloorRequest(floor, motor.getDirectionUp());
    }

    public ArrayList<UserInput> getElevatorRequests(int floor) {
        return scheduler.serviceElevatorRequest(floor);
    }

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
                buttonPress(user);
            }
        }

        // Remove button selection from user exitting
        if (!elevatorRequests.isEmpty()) {
            // Simulate the user getting off the elevator
            for (UserInput user : elevatorRequests) {
                // Reset the button depending on what floor was pressed
                for (elevatorButtons button : elevatorButtons) {
                    if (button.getButtonFloor() == user.getCarButton()) {
                        System.out.println("Elevator: Dropped off user on floor " + floor);
                        button.reset();
                        break;
                    }
                }
            }
        }
	}

    /** Sets the button in its clicked state
     * As well as sending a notification to the scheduler
     */
    public void buttonPress(UserInput userInput) {
        // Activate the correct button depending on which floor was pressed
        for (elevatorButtons button : elevatorButtons) {
            if (button.getButtonFloor() == userInput.getCarButton()) {
                // Turn on the button for that floor
                System.out.println("Elevator: User has pressed button to go to floor " + userInput.getCarButton());
                button.press();
                break;
            }
        }

		// Send request to scheduler
		sendElevatorRequest(userInput);
    }

    /** Sends request to the Scheduler */
	private void sendElevatorRequest(UserInput userInput) {
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
class elevatorButtons {
	private boolean buttonState = false; // Defines the state of the button, On or Off
	private ElevatorLamp buttonLamp = new ElevatorLamp(); // Used to hold the associated lamp of the button
	private int buttonFloor = 0; // Indicates the floor the button is associated to

	public elevatorButtons(int floor) {
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
