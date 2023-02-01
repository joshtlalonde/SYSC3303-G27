import java.time.*;


public class Scheduler {
	
	
	private boolean synchronize;
	public boolean directionUp;
	public int currentElevatorFloor;
	public LocalTime time;
	public int headingFloor;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	//Do we remove synchroization
	public synchronized void put(User_input user_input) {
		// Prolly Start Synchronizing
		currentElevatorFloor = user_input.getFloor();
		headingFloor = user_input.getCar_button();
		if(headingFloor > currentElevatorFloor) {
			directionUp = true;
		}
		else if(headingFloor < currentElevatorFloor) {
			directionUp = false;
		}
		else if(headingFloor == currentElevatorFloor) {
			//At the same floor
			//Do something
		}
		else {
			System.err.println("Something with floors failed");
			//Something went wrong 
		}
		
		
		
		
		
		
		//If statements should be here to test to see if going up or down
		
		
	}
	
	public void tellElevator() {
		//Pass currentFloor to Elevator
		
	}
	
	public synchronized void get() {
		
	}
	
	public void createFloor() {
			}

}
