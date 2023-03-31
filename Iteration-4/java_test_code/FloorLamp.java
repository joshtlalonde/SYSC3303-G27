public class FloorLampTest extends junit.framework.TestCase{
  //private boolean lampState = false;
  public LampTest()
	{
		
		 
	}
	
	public void testFloorLamp()
	{
		FloorLamp floorlamp = new FloorLamp();
		assertFalse(floorlamp.getLampState());
		floorlamp.turnOn();
		assertTrue(floorlamp.getLampState());
		floorlamp.turnOff();
		assertFale(floorlamp.getLampState());
    
  }
