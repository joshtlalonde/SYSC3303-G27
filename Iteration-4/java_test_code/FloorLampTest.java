public class FloorLampTest extends junit.framework.TestCase{
	//private boolean lampState = false;
  	public LampTest()
	{
		
		 
	}
	
	public void testTurnOn() {
		FloorLamp floorlamp = new FloorLamp();
		floorlamp.turnOn();
		assertTrue(floorlamp.getLampState());
		}
		
	public void testTurnOff() {
		FloorLamp floorlamp = new FloorLamp();
		floorlamp.turnOff();
		assertFalse(floorlamp.getLampState());
	}
	public void testGetLampState() {
		FloorLamp floorlamp = new FloorLamp();
		floorlamp.turnOn();
		assertTrue(floorlamp.getLampState());
		floorlamp.turnOff();
		assertFalse(floorlamp.getLampState());
	}

    
  }
