public class Test extends junit.framework.TestCase{
	private Scheduler scheduler = new Scheduler(); 
	DirectionLamp directionLamp = new DirectionLamp();
	private Floor floor = new Floor(scheduler, directionLamp);

	public Test ()
	{
		 
	}
	
	public void testReading()
	{
		UserInput ui = new UserInput("10:05:15.0",2,true,4);
		UserInput testui = floor.fileToUser("10:05:15.0,2,Up,4");
		assertEquals(ui.getTime(), testui.getTime());
		assertEquals(ui.getFloor(), testui.getFloor());
		assertEquals(ui.getFloorButton(), testui.getFloorButton());
		assertEquals(ui.getCarButton(), testui.getCarButton());
	}
}
