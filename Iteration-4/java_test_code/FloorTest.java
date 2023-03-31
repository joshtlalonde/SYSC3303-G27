import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class FloorTest extends junit.framework.TestCase {
    public void testConstructor() {
    
        Floor floor = new Floor();
    
        //checking floor button initialisation
        assertEquals(Floor.NUMBER_OF_FLOORS, floor.floorButton.size());
        //checking floor requests initialisation
	    assertEquals(Floor.NUMBER_OF_FLOORS, floor.floorRequests.size());
    
    public UserInput testFileToUser(String line) {

    }

    public void testButtonPress(boolean floorButtonUp, int floor) {
        Floor floor = new Floor();
        floor.buttonPress(true, 10);//pressed up button
		assertTrue(floor.floorButton.get(4).getUpButton());
        floor.buttonPress(false, 15);//pressed down button
        assertTrue(floor.floorButton.get(4).getDownButton());

    }

    public void testElevatorArrival(boolean elevatorDirection, int floor) {
        
        // verifying if the button was reset
        assertEquals(false, floor.floorButton.get(5).getUpButton());
        // verify if the floor requests were removed from array
        assertEquals(true, floor.floorRequests.isEmpty());

    }
}
