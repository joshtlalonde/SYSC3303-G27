public class Test extends junit.framework.TestCase{
	private Scheduler scheduler = new Scheduler(); 
	private Floor floor = new Floor(scheduler);

	public Test ()
	{
		 
	}
	
	public void testReading()
	{
		User_input ui = new User_input("10:05:15.0",2,true,4);
		User_input testui = floor.file_to_user("10:05:15.0,2,Up,4");
		assertEquals(ui.getTime(), testui.getTime());
		assertEquals(ui.getFloor(), testui.getFloor());
		assertEquals(ui.getFloor_button(), testui.getFloor_button());
		assertEquals(ui.getCar_button(), testui.getCar_button());
	}
}
