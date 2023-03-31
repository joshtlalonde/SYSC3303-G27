public class FloorButtonTest extends junit.framework.TestCase{
	//private boolean upButtonState = false; // Defines the state of the up button, On or Off
	//private FloorLamp upButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the up button
	//private boolean downButtonState = false; // Defines the state of the down button, On or Off
	//private FloorLamp downButtonLamp = new FloorLamp(); // Used to hold the associated lamp of the down button
	//private int buttonFloor = 0;

	public void testConstructor() {
    FloorButton floorbutton = new FloorButton(5);
    floorButton.pressUp();
		assertTrue(floorButton.getUpButtonState());
    floorButton.pressDown();
		assertTrue(floorButton.getButtonDirectionUp());
    floorButton.resetUp();
    assertFalse(floorButton.getButtonDirectionUp());
    floorButton.resetDown();
    assertFalse(floorButton.getButtonDirectionDown());
		
		 
	}
	
	
