import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;

public class SchedulerTest extends junit.framework.TestCase {

    public void testReceive() {
    	/** Create DatagramSocket */ 
    	DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	/** Create an elevatorPacket and a floorPacket */
    	ElevatorPacket elevatorPacket = new ElevatorPacket();
    	FloorPacket floorPacket = new FloorPacket();
    	
    	/** Create a scheduler object */
    	Scheduler scheduler = new Scheduler();
    	
    	/** Send a floor packet */
    	try {
			floorPacket.send(InetAddress.getLocalHost(), 69, socket, true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/** Receive the FloorPacket */
    	DatagramPacket floorResponse = scheduler.receive();
    	/** Assert that the first byte of data is a 0 */
    	assertEquals(floorResponse.getData()[0], 0);    
    	/** Assert that current state is PROCESS_FLOOR */
    	assertEquals(scheduler.getCurrentState(), Scheduler_State.PROCESS_FLOOR);   
    	
    	/** Send a elevator packet */
    	try {
			elevatorPacket.send(InetAddress.getLocalHost(), 69, socket, true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/** Receive the FloorPacket */
    	DatagramPacket elevatorResponse = scheduler.receive();
    	/** Assert that the first byte of data is a 0 */
    	assertEquals(elevatorResponse.getData()[0], 1);    
    	/** Assert that current state is PROCESS_ELEVATOR */
    	assertEquals(scheduler.getCurrentState(), Scheduler_State.PROCESS_ELEVATOR);
    	
    	// Close the sockets
    	socket.close();
    	scheduler.getReceiveSocket().close();
    }

    public void testProcessFloor() {
    	/** Create DatagramSocket */ 
    	DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	/** Create an elevatorPacket and a floorPacket */
    	FloorPacket floorPacket = new FloorPacket(1, new Date(), true, 10, false, false);
    	
    	/** Create a scheduler object */
    	Scheduler scheduler = new Scheduler();
    	
    	/** Send a floor packet */
    	try {
			floorPacket.send(InetAddress.getLocalHost(), 69, socket, true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/** Receive the FloorPacket */
    	DatagramPacket floorResponse = scheduler.receive();
    	
    	/** Call processFloor */
    	scheduler.processFloor(floorResponse);
    	/** Assert that floorAddress is same as this address */
    	try {
			assertEquals(scheduler.getFloorAddress(), InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/** Assert that new floorRequest has been added and its destination floor is 10*/
    	assertEquals(scheduler.getFloorRequests().get(0).getDestinationFloor(), 10);
    	/** Assert that current state is RECEIVE */
    	assertEquals(scheduler.getCurrentState(), Scheduler_State.RECEIVE);
    	
    	// Close the sockets
    	socket.close();
    	scheduler.getReceiveSocket().close();
    }

    public void testProcessElevator() {
    	/** Create DatagramSocket */ 
    	DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	/** Create an elevatorPacket and a floorPacket */
    	ElevatorPacket elevatorPacket = new ElevatorPacket(3, true, 6, 9, false, new ArrayList<UserInput>(), Elevator_State.STOPPED);
    	
    	/** Create a scheduler object */
    	Scheduler scheduler = new Scheduler();
    	
    	/** Send a floor packet */
    	try {
    		elevatorPacket.send(InetAddress.getLocalHost(), 69, socket, true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/** Receive the FloorPacket */
    	DatagramPacket elevatorResponse = scheduler.receive();
    	
    	/** Call processFloor */
    	scheduler.processElevator(elevatorResponse);
    	/** Assert that elevatorInfos has been updated and that the currentFloor is 6 */
    	assertEquals(scheduler.getElevatorInfos().get(0).getCurrentFloor(), 6);
    	/** Assert that current state is RECEIVE */
    	assertEquals(scheduler.getCurrentState(), Scheduler_State.RECEIVE);
    	
    	// Close the sockets
    	socket.close();
    	scheduler.getReceiveSocket().close();
    }

    

    public void testServiceElevatorIdle() {

    }

    public void testServiceElevatorMovingUp() {
    	Scheduler scheduler = new Scheduler();
    	ElevatorInfo elevatorInfo = null;
    	ElevatorInfo elevator = new ElevatorInfo();

		try {
			elevatorInfo = new ElevatorInfo(1, 2, 5, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP, 69, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	elevator = scheduler.serviceElevatorMovingUp(elevatorInfo);
    	
    	assertEquals(elevator.getCurrentFloor(), 2);
    	assertEquals(elevator.getCurrentState(), Elevator_State.MOVING_UP);
    	assertEquals(elevator.getDestinationFloor(), 5);
    	
    	
    	scheduler.getReceiveSocket().close();
    }

    public void testServiceElevatorMovingDown() {
    	Scheduler scheduler = new Scheduler();
    	ElevatorInfo elevatorInfo = null;
    	ElevatorInfo elevator = new ElevatorInfo();

		try {
			elevatorInfo = new ElevatorInfo(1, 2, 5, false, new ArrayList<UserInput>(), Elevator_State.MOVING_DOWN, 69, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	elevator = scheduler.serviceElevatorMovingDown(elevatorInfo);
    	
    	assertEquals(elevator.getCurrentFloor(), 2);
    	/** Assert that current state is RECEIVE */
    	assertEquals(elevator.getCurrentState(), Elevator_State.MOVING_DOWN);
    	assertEquals(elevator.getDestinationFloor(), 5);

    	scheduler.getReceiveSocket().close();
    }

    public void testServiceElevatorStopped() {
    	Scheduler scheduler = new Scheduler();
    	ElevatorInfo elevatorInfo = null;
    	ElevatorInfo elevator = new ElevatorInfo();

		try {
			elevatorInfo = new ElevatorInfo(1, 2, 5, true, new ArrayList<UserInput>(), Elevator_State.STOPPED, 69, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	elevator = scheduler.serviceElevatorStopped(elevatorInfo);
    	
    	assertEquals(elevator.getCurrentFloor(), 2);
    	/** Assert that current state is RECEIVE */
    	assertEquals(elevator.getCurrentState(), Elevator_State.STOPPED);
    	assertEquals(elevator.getDestinationFloor(), 5);
    	
    	scheduler.getReceiveSocket().close();

    }

    public void testServiceElevatorDoorOpen() {
	    Scheduler scheduler = new Scheduler();
	    
	    
	    ArrayList<UserInput> passengers = new ArrayList<UserInput>();
	    passengers.add(new UserInput(new Date(72), 0, false, 0, true, false));
	    
	    try {
	    	ElevatorInfo elevator = new ElevatorInfo(1, 2, 5, true, passengers, Elevator_State.DOOR_OPEN, 69, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ElevatorInfo elevator = scheduler.serviceElevatorDoorOpen(elevator);
	    
	    assertTrue(elevator.getPassengers().isEmpty());
	    
	    
    }

    public void testServiceElevatorDoorClose() {
	    Scheduler scheduler = new Scheduler();
	    ArrayList<UserInput> passengers = new ArrayList<UserInput>();
	    passengers.add(new UserInput(new Date(72), 0, false, 0, true, false));
            passengers.add(new UserInput(new Date(100), 1, false, 2, false, true));
	    ElevatorInfo elevatorInfo = null;
	    try {
	    	ElevatorInfo elevator = new ElevatorInfo(1, 2, 5, true, passengers, Elevator_State.DOOR_CLOSE, 69, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    elevator = scheduler.serviceElevatorDoorClose(elevator);
	    assertEquals(2, elevator.getPassengers().size());
	    assertTrue(elevator.getPassengers().contains(passengers.get(0)));
	    assertTrue(elevator.getPassengers().contains(passengers.get(1)));

    }
}
