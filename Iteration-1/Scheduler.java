public class Scheduler {
	
    private User_input user_input;

	private int curr_floor;
	private Boolean direction_up;
	
	public synchronized void put(User_input input) {
		while (user_input != null) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Error waiting: " + e);
				return;
			}
		}
		
		// Set the user_input to the one that was read by the floor
        user_input = input;
		// Notify Chefs that there are ingredients on the plate
		notifyAll();
	}
	
	public synchronized User_input get() {
        while (user_input == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            	System.out.println("Error waiting: " + e);
                return null;
            }
		}

		System.out.println(Thread.currentThread().getName() + " is moving to floor " + user_input.getFloor() + " to pick up user");
		// Sleep for travel time
		try {
			Thread.sleep(1000); 
		} catch (InterruptedException e) {}
		
		System.out.println(Thread.currentThread().getName() + " is moving user to floor " + user_input.getCar_button() + " to drop off user");
		// Sleep for travel time
		try {
			Thread.sleep(1000); 
		} catch (InterruptedException e) {}

		// Update the schedulers known information
		curr_floor = user_input.getCar_button();
		direction_up = user_input.getFloor_button();
		
		// Notify Agent that the plate is empty
		notifyAll();
		// Copy user_input values
		User_input input = user_input;
		// Reset the user_input
		user_input = null;
		
		// Return 
		return input;
	}

    public static void main(String[] args) {
        Thread floor, elevator;
        
        // Create table that all threads will access
        Scheduler scheduler = new Scheduler();
        
        // Create Agent and Chef threads
        floor = new Thread(new Floor(scheduler), "Floor");
        elevator = new Thread(new Elevator(scheduler), "Elevator");
        
        // Start Threads
        floor.start();
        elevator.start();
    }
}
