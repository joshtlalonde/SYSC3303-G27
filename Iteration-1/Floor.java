import java.io.*;

public class Floor implements Runnable {
	private Scheduler scheduler;
	
	/** Constructor for Floor */
	public Floor(Scheduler scheduler) {
		this.scheduler = scheduler;	
	}
	
	public User_input file_to_user(String line) {
		String[] words = line.split(",");
		
		// Convert strings to their appropriate types
		//LocalTime t = LocalTime.parse(words[0], DateTimeFormatter.ISO_LOCAL_TIME);
		String t = words[0];
		int f = Integer.parseInt(words[1]);
		Boolean fb = words[2].toLowerCase().equals("up") ? true : false;
		int cb = Integer.parseInt(words[3]);

		// Create new user
		return new User_input(t, f, fb, cb);
	}

	/** Function to be run on Thread.start() */
	public void run() {
		// Open and read file line-by-line
		BufferedReader reader;
		try {
			String line;
			reader = new BufferedReader(new FileReader("../floor_input.txt"));
			
			while((line = reader.readLine()) != null) {
				User_input user_input = file_to_user(line);
				System.out.println("Floor: Retreived " + user_input + " from file");

				// Puts the user_input into the scheduler
				scheduler.put(user_input);
				System.out.println("Floor: Put " + user_input + " into the scheduler");

				// Sleep for 1 second
				try {
					Thread.sleep(1000); 
				} catch (InterruptedException e) {
					e.printStackTrace();
         				System.exit(1);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Failed to open File: " + e);
			return;
		} catch (IOException e) {
			System.out.println("Failed to read File: " + e);
			return;
		}
		
		// Close reader
		try {
			reader.close();
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Failed to close File: " + e);
         		System.exit(1);
		}
	}
}

class User_input{
	//private LocalTime time;
	private String time; // Timestamp of when button was clicked
	private int floor; // Floor that button was clicked on
	private boolean floor_button; // Direction that user wants to go
	private int car_button; // Button that was clicked in elevator to decide destination floor
	
	
	//Making the constructor for the user input class
	public User_input(String time, int floor, boolean floor_button, int car_button) {
		this.time = time;
		this.floor = floor;
		this.floor_button= floor_button;
		this.car_button = car_button;
	}
	
	@Override
	public String toString() {
		return "{time: " + time + ", floor: " + floor + ", floor_button: " + floor_button + ", car_button: " + car_button + "}";
	}
	
	//Getting the data from the user input
	public String getTime() {
		return time;
	}
	public int getFloor() {
		return floor;
	}
	public boolean getFloor_button() {
		return floor_button;
	}
	public int getCar_button() {
		return car_button;
	}
	
}
