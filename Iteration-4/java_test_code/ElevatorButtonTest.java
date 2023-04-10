public class ElevatorButtonTest extends junit.framework.TestCase{
	
	public void testPress() {
		ElevatorButton elevatorButton = new ElevatorButton(2);
		elevatorButton.press();
		
		assertTrue(elevatorButton.getButtonState());
	}
	
	public void testReset() {
		ElevatorButton elevatorButton = new ElevatorButton(2);
		elevatorButton.reset();
		
		assertFalse(elevatorButton.getButtonState());
	}
	

}
