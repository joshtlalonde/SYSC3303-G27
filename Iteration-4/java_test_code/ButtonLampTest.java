public class ButtonLampTest extends junit.framework.TestCase{
	//private FloorButton floorbutton;
	//private FloorLamp floorlamp;

	public ButtonLampTest()
	{
		
		 
	}
	
	public void testFloorLamp()
	{
		FloorLamp floorlamp = new FloorLamp();
		assertFalse(floorlamp.getLampState());
		floorlamp.turnOn();
		assertTrue(floorlamp.getLampState());
		//FloorButton FloorButton = new FloorButton(true, floorlamp, true,5);
		//assertEquals(ui,testui);
	}
	public void testFloorButton()
	{
		FloorLamp floorlamp = new FloorLamp();
		FloorButton floorButton = new FloorButton(5);
		floorButton.buttonLamp = floorlamp;
		assertFalse(floorButton.getButtonState());
		floorButton.pressUp();
		assertTrue(floorButton.getButtonState());
		assertTrue(floorButton.getButtonDirectionUp());
		//assertEquals(floorButton.getButtonFloor(),5);

	}

}
