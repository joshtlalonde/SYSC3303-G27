package iteration1;
import java.time.*;

public class Floor implements Runnable {
	private User_input user_input;
	
	public Floor(User_input user_input) {
		this.user_input = user_input;
		
	}
	public User_input getUser_input() {
		return user_input;
	}
	public void run() {
		Scheduler.put(user_input)
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}
	
	
}
class User_input{
	private LocalTime time;
	private int floor;
	private boolean floor_button;
	private int car_button;
	
	
	//Making the constructor for the user input class
	public User_input(LocalTime time, int floor, boolean floor_button, int car_button) {
		this.time = time;
		this.floor = floor;
		this.floor_button= floor_button;
		this.car_button = car_button;
		
	}
	
	//Getting the data from the user input
	public LocalTime getTime() {
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