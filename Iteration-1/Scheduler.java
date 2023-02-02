public class Scheduler {
	
    private User_input user_input;
	
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
