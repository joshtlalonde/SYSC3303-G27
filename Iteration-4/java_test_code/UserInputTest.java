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
	byte[] expectedBytes = new byte[] {0x0C, 0x22, (byte) 0xB8, (byte) 0xD9, 0x00, 0x03, 0x01, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01
		};
		
	assertArrayEquals(expectedBytes, userinput.convertToBytes());
	
	)
	
