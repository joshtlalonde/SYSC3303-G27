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
    
    public UserInput testFileToUser() {
	Floor floor = new Floor();
	UserInput userinput = floor.fileToUser("04:10:30.200,5,Up,16,true,false");
    	assertEquals(userinput.getCurrentFloor(),5);
    	assertTrue(userinput.getFloorButtonUp());
    	assertEquals(userinputt.getDestinationFloor(),16);
    	assertTrue(userinput.getDoorFault());
    	assertFalse(userinput.getHardFault());
}
	    

    }

    public void testButtonPress() {
        Floor floor = new Floor();
        floor.buttonPress(true, 10);//pressed up button
	assertTrue(floor.floorButton.get(4).getUpButton());
        floor.buttonPress(false, 15);//pressed down button
        assertTrue(floor.floorButton.get(4).getDownButton());

    }

    public void testElevatorArrival() throws Exception {
	Floor floor = new Floor();
	UserInput userinput = floor.fileToUser("04:10:30.200,5,Up,16,true,false");
	floor.floorRequests.add(userInput);
        floor.elevatorArrival(userInput);
        
        // verifying if the button was reset
        assertEquals(false, floor.floorButton.get(5).getUpButton());
        // verify if the floor requests were removed from array
        assertEquals(true, floor.floorRequests.isEmpty());

    }
}
