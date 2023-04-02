public class FloorButtonTest extends junit.framework.TestCase{
	//private boolean upButtonState = false; // Defines the state of the up button, On or Off
	//private FloorLamp upButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the up button
	//private boolean downButtonState = false; // Defines the state of the down button, On or Off
	//private FloorLamp downButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the down button
	//private int buttonFloor = 0;

	public void testConstructor() {
		FloorButton floorButton = new FloorButton(5);
		floorButton.pressUp();
		assertTrue(floorButton.getUpButtonState());
		floorButton.pressDown();
		assertTrue(floorButton.getDownButtonState());
		floorButton.resetUp();
		assertFalse(floorButton.getUpButtonState());
		floorButton.resetDown();
		assertFalse(floorButton.getDownButtonState());
		assertEquals(floorButton.getButtonFloor(),5);
	}
	public void testPressUp() {
		FloorButton floorbutton = new FloorButton(3);
		assertFalse(floorbutton.getUpButtonState());
		floorbutton.pressUp();
		assertTrue(floorbutton.getUpButtonState());
		floorbutton.pressUp(); 
		assertTrue(floorbutton.getUpButtonState());
	}
	public void testPressDown() {
		FloorButton floorbutton = new FloorButton(5);
		assertFalse(floorbutton.getDownButtonState());
		floorbutton.pressDown();
		assertTrue(floorbutton.getDownButtonState());
		floorbutton.pressDown(); 
		assertTrue(floorbutton.getDownButtonState());
	}
	public void testResetUp() {
		FloorButton floorbutton = new FloorButton(2);
		assertFalse(floorbutton.getUpButtonState());
		floorbutton.pressUp();
		assertTrue(floorbutton.getUpButtonState());
		floorbutton.resetUp();
		assertFalse(floorbutton.getUpButtonState());
		floorbutton.resetUp(); 
		assertFalse(floorbutton.getUpButtonState());
	}
	public void testResetDown() {
		FloorButton floorbutton = new FloorButton(4);
		assertFalse(floorbutton.getDownButtonState());
		floorbutton.pressDown();
		assertTrue(floorbutton.getDownButtonState());
		floorbutton.resetDown();
		assertFalse(floorbutton.getDownButtonState());
		floorbutton.resetDown(); 
		assertFalse(floorbutton.getDownButtonState());
	}
	public void testGetUpButtonState() {
		FloorButton floorbutton = new FloorButton(1);
		floorbutton.pressUp();
		assertTrue(floorbutton.getUpButtonState());
		floorbutton.resetUp();
		assertFalse(floorbutton.getUpButtonState());
	}

	public void testGetDownButtonState() {
		FloorButton floorbutton = new FloorButton(2);
		floorbutton.pressDown();
		assertTrue(floorbutton.getDownButtonState());
		floorbutton.resetDown();
		assertFalse(floorbutton.getDownButtonState());
	}

	public void testGetButtonFloor() {
		FloorButton floorbutton = new FloorButton(1);
		assertEquals(1, floorbutton.getButtonFloor());
	}
}




