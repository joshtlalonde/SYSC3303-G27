package iteration1;
import java.time.*;
import java.io.*;
import java.util.Scanner;
//import Scheduler;

public class Floor implements Runnable {
	private User_input user_input;
	private User_input[] user_input_list;
	//private Scheduler schedule;
	
	public Floor(User_input user_input) {
		this.user_input = user_input;
		
	}
	public User_input[] getUser_input() {
		return user_input_list;
	}
	Scanner scan = new Scanner(new File("D:\\Books\\Third Year\\Winter\\Sysc 3303 B\\Project IT1\\iteration1\\src\\iteration1\floor_input.csv"));
	scan.useDelimiter(",");
	for(int i = 0; i<4;i++) {
		while(scan.hasNext()) { 
			LocalTime t = scan.next();
			int f = scan.next();
			int fb = scan.next();
			int cb = scan.next();
			user_input(t,f,fb,cb);
			
		
	}
	}
	public void run() {
		//schedule.put(user_input);
		try {} catch (InterruptedException e) {}
	}
	private void user_input(LocalTime t, int f, int fb, int cb) {
		// TODO Auto-generated method stub
		
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
