public class DoorTest extends junit.framework.TestCase{
    public void testDoor(){
        Door door = new Door();
        door.open();
        assertTrue(door.getIsOpen());
        door.close();
        assertFalse(door.getIsOpen());
    }
}
