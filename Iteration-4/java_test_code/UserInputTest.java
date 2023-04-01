public class UserInputTest extends junit.framework.TestCase{
  public void testConstructor() {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
    UserInput userinput = new UserInput(time, 4, true, 8, false, true);
    Date date;
        try {
            date = dateFormatter.parse("12:10:09.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
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
