public class UserInputTest extends junit.framework.TestCase{
	public void testConstructor() {
	 	SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
    	  	UserInput userinput = new UserInput("12:10:09.5", 4, true, 8, false, true);
    	try {
            assertEquals(userinput.getTime(), dateFormatter.parse("12:10:09.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		assertEquals(userinput.getCurrentFloor(), 4);
		assertEquals(userinput.getFloorButtonUp(), true);
		assertEquals(userinput.getDestinationFloor(), 8);
		assertEquals(userinput.getDoorFault(), false);
		assertEquals(userinput.getHardFault(), true);

  }
	public void testDefaultConstructor(){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
		UserInput userInput = new UserInput();
        	try {
            	assertEquals(userInput.getTime(), dateFormatter.parse("12:10:09.5"));
        	} catch (ParseException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
        	}
        	assertEquals(0, userInput.getCurrentFloor());
        	assertFalse(userInput.getFloorButtonUp());
        	assertEquals(0, userInput.getDestinationFloor());
        	assertFalse(userInput.getDoorFault());
        	assertFalse(userInput.getHardFault());
    }
		
	public void testConvertPacket() {
        	UserInput userInput = new UserInput();
        	FloorPacket floorPacket = new FloorPacket(3, "10:00:00.000", true, 5, false, true);
		userInput.convertPacket(floorPacket);
       
        	assertEquals(userInput.getCurrentFloor(), 3);
        	try {
            	assertEquals(userInput.getTime(), dateFormatter.parse("10:00:00.000"));
        	} catch (ParseException e) {
            	e.printStackTrace();
        	}
        	assertTrue(userInput.getFloorButtonUp());
        	assertEquals(userInput.getDestinationFloor(), 5);
        	assertFalse(userInput.getDoorFault());
        	assertTrue(userInput.getHardFault());
	}
	public void testConvertToBytes() throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
		UserInput userInput = new UserInput();
		userInput.setTime(dateFormatter.parse("12:10:09.5"));
		userInput.setCurrentFloor(4);
		userInput.setFloorButtonUp(true);
		userInput.setDestinationFloor(8);
		userInput.setDoorFault(false);
		userInput.setHardFault(true);

		byte[] testBytes = {49, 50, 58, 49, 48, 58, 48, 57, 46, 53, 0, 4, 1, 8, 0, 1};

		assertArrayEquals(testBytes, userInput.convertToBytes());
	}
	
	
	public void testByteLength() {
		UserInput userinput = new UserInput("12:10:09.5", 4, true, 8, false, true);
        	assertEquals(17, userinput.byte_length());
	
	
    	}
	public String testToString(){
		UserInput userInput = new UserInput();
    		try {
			assertEquals(userInput.getTime(), dateFormatter.parse("10:10:10.5"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}; // set a specific date for testing
    		userInput.setCurrentFloor(4);
    		userInput.setFloorButtonUp(true);
    		userInput.setDestinationFloor(7);
    		userInput.setDoorFault(false);
    		userInput.setHardFault(false);

    	String myString = "{time: Apr 30, 2022 10:10:10.5 AM, currentFloor: 4, floorButtonUp: true, destinationFloor: 7, doorFault: false, hardFault: false}";

    	assertEquals(myString, userInput.toString());
}
	public void testGettersAndSetters() throws ParseException {
        	SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	
        	UserInput userinput = new UserInput("10:10:10.5", 5, false, 7, false, true);
        	try {
			assertEquals(userinput.getTime(), dateFormatter.parse("10:10:10.5"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        	assertEquals(5, userinput.getCurrentFloor());
        	assertFalse(userinput.getFloorButtonUp());
        	assertEquals(7, userinput.getDestinationFloor());
        	assertFalse(userinput.getDoorFault());
        	assertTrue(userinput.getHardFault());
        	userinput.setDoorFault(true);
        	assertTrue(userinput.getDoorFault());
        	userinput.setHardFault(false);
        	assertFalse(userinput.getHardFault());
    }
}
		
		
	
	
