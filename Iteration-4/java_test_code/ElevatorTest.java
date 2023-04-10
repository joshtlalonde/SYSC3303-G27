
public class ElevatorTest extends junit.framework.TestCase{
	
	public void testIdle() {
        Elevator elevator = new Elevator(5);
        assertFalse(elevator.directionLamp.getState());
    }
	
	public void testMovingup() {
		// Create an instance of the Elevator class
        Elevator elevator = new Elevator(5);
        elevator.movingUp();
        
        // Test that the elevator is moving up
        assertTrue(elevator.directionUp());
        assertTrue(elevator.directionLamp.getState());
	}
	
	public void testMovingup() {
		// Create an instance of the Elevator class
        Elevator elevator = new Elevator(5);
        elevator.movingDown();
        
        // Test that the elevator is moving down
        assertFalse(elevator.directionUp());
        assertFalse(elevator.directionLamp.getState());
	}
	
	public void testStopped() {
		Elevator elevator = new Elevator(5);
		
		//Put the elevator into stopped state
		elevator.stopped();
		
		assertFalse(elevator.motor.isMoving());
		
		
	}
	
	public void testdoorOpen() {
		Elevator elevator = new Elevator(5);
		
		elevator.doorOpen();
		
		assertTrue(elevator.door.getIsOpen());
	}
	
	public void testdoorClose() {
		Elevator elevator = new Elevator(5);
		
		elevator.doorClose();
		
		assertFalse(elevator.door.getIsOpen());
	}

}
