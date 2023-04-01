public class UserInputTest extends junit.framework.TestCase{
  public void testConstructor() {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
    UserInput userinput = new UserInput(time, 4, true, 8, false, true);
    
    try {
            assertEquals(userinput.getTime(), dateFormatter.parse("10:10:10.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		assertEquals(userinput.getCurrentFloor(), 5);
		assertEquals(userinput.getFloorButtonUp(), true);
		assertEquals(userinput.getDestinationFloor(), 10);
		assertEquals(userinput.getDoorFault(), false);
		assertEquals(userinput.getHardFault(), true);

