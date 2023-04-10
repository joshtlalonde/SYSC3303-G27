import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;

public class FloorTest extends junit.framework.TestCase {
	public void testConstructor() {
    
        Floor floor = new Floor();
    
        //checking floor button initialisation
        assertEquals(Floor.NUMBER_OF_FLOORS, floor.floorButton.size());
        //checking floor requests initialisation
	assertEquals(Floor.NUMBER_OF_FLOORS, floor.floorRequests.size());
	}
    
 	public UserInput testFileToUser() {
		Floor floor = new Floor();
		UserInput userinput = floor.fileToUser("04:10:30.200,5,Up,16,true,false");
    		assertEquals(userinput.getCurrentFloor(),5);
    		assertTrue(userinput.getFloorButtonUp());
    		assertEquals(userinput.getDestinationFloor(),16);
    		assertTrue(userinput.getDoorFault());
    		assertFalse(userinput.getHardFault());
		}
	    

    

    	public void testButtonPress() {
        	Floor floor = new Floor();
        	floor.buttonPress(true, 10);//pressed up button
		assertTrue(floor.floorButton.get(4).getUpButton());
        	floor.buttonPress(false, 15);//pressed down button
        	assertTrue(floor.floorButton.get(4).getDownButton());

    }
	
	public void testSendFloorRequest() throws Exception{
    		Floor floor = new Floor();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        	Date date=null;
        	try {
            		date = dateFormatter.parse("10:10:10.5");
       		 } catch (ParseException e1) {
            		// TODO Auto-generated catch block
           	 e1.printStackTrace();
            	return;
        	}
		UserInput userInput = new UserInput(date, 5, true, 10, false, false);
	
		InetAddress localAddr=null;
			try {
				localAddr = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
				return;
		}
		floor.sendFloorRequest(localAddr, 23, userInput);
    }
    	public void testReceiveFloorResponse() throws Exception{
		Floor floor = new Floor();
		//wait for response
		Thread.sleep(1000);
		//calling the method to recieve response
		UserInput userInput = floor.receiveFloorResponse();
		assertNotNull(userInput);
    	
}


	public void testElevatorArrival() throws Exception {
		Floor floor = new Floor();
		UserInput userinput = floor.fileToUser("04:10:30.200,5,Up,16,true,false");
		floor.floorRequests.add(userinput);
        	floor.elevatorArrival(userinput);
        
        	// verifying if the button was reset
        	assertEquals(false, floor.floorButton.get(5).getUpButton());
        	// verify if the floor requests were removed from array
        	assertEquals(true, floor.floorRequests.isEmpty());

    }
    
    
