public class ArrivalSensorTest extends junit.framework.TestCase{
    public void testSensor(){
        ArrivalSensor sense = new ArrivalSensor();
        assertEquals(0,sense.getFloor());
        sense.setFloor(5);
        assertEquals(5,sense.getFloor());
    }
}
