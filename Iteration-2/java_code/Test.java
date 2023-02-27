public class Test extends junit.framework.TestCase{
	private Floor floor;

	public Test ()
	{
		 
	}
	
	public void testReading()
	{
		User_input ui = new User_input("10:05:15.0",2,true,4);
		User_input testui = floor.file_to_user("'10:05:15.0',2,true,4");
		assertEquals(ui,testui);
	}

}
