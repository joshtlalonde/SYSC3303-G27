public class UserInputTest extends junit.framework.TestCase{
	
  public void testConstructor() {
	  SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
    
        
    	  UserInput userinput = new UserInput(12:10:09.5, 4, true, 8, false, true);
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
public void testConvertPacket() {
        UserInput userInput = new UserInput();
        FloorPacket floorPacket = new FloorPacket(3, "10:00:00.000", true, 5, false, true);
	userInput.convertPacket(floorPacket);
       
        assertEquals(userInput.getCurrentFloor(), 3);
        assertEquals(userInput.getTime(), "10:00:00.000");
        assertTrue(userInput.getFloorButtonUp());
        assertEquals(userInput.getDestinationFloor(), 5);
        assertFalse(userInput.getDoorFault());
        assertTrue(userInput.getHardFault());
}
public void testconvertToBytes() throws ParseException {
	SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	UserInput userinput = new UserInput(12:10:09.5, 4, true, 8, false, true);
	////
	
	
public void testByteLength() {
	UserInput userinput = new UserInput(12:10:09.5, 4, true, 8, false, true);
        assertEquals(17, input.byte_length());
	
    }
public void testGettersAndSetters() throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
	
        UserInput userinput = new UserInput(time, 5, false, 7, false, true);
        try {
			assertEquals(userin.getTime(), dateFormatter.parse("10:10:10.5"));
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
		
		
	
	
