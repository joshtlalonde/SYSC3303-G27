public class ElevatorInfoTest extends junit.framework.TestCase{
  
  
  public void testConstructor() {
    ElevatorInfo elevator = new ElevatorInfo();
    assertEquals(0, elevator.getElevatorNumber());
    assertEquals(0, elevator.getCurrentFloor());
    assertEquals(0, elevator.getDestinationFloor());
    assertFalse(elevator.getDirectionUp());
    assertEquals(Elevator_State.IDLE, elevator.getCurrentState());
    assertTrue(elevator.getPassengers().isEmpty());
    }
  
  public void testDefaultConstructor() {
        ArrayList<UserInput> passengers = new ArrayList<>();
        passengers.add(new UserInput("Rose", 4));
        passengers.add(new UserInput("Annie", 7));
        passengers.add(new UserInput("August", 3));
        ElevatorInfo elevator = new ElevatorInfo(2, 5, 7, true, passengers, Elevator_State.MOVING_UP, 5678, InetAddress.getLocalHost());
        assertEquals(2, elevator.getElevatorNumber());
        assertEquals(5, elevator.getCurrentFloor());
        assertEquals(7, elevator.getDestinationFloor());
        assertTrue(elevator.getDirectionUp());
        assertEquals(Elevator_State.MOVING_UP, elevator.getCurrentState());
        assertEquals(passengers, elevator.getPassengers());
        assertEquals(5678, elevator.getPort());
        assertEquals(InetAddress.getLocalHost(), elevator.getAddress());
    }
  
  
    
  
  
  public void testSendPacket() {
        DatagramSocket socket = new DatagramSocket();
    
        ElevatorInfo elevator = new ElevatorInfo(3, 4, 6, true, new ArrayList<>(), Elevator_State.MOVING_UP, 1234, InetAddress.getLocalHost());
        elevator.sendPacket(socket);
    )
        
    
  public void testGettersAndSetters() {
        ElevatorInfo elevator = new ElevatorInfo();
        elevator.setElevatorNumber(7);
        assertEquals(7, elevator.getElevatorNumber());
        elevator.setIsMoving(true);
        assertTrue(elevator.getIsMoving());
        elevator.setCurrentFloor(6);
        assertEquals(6, elevator.getCurrentFloor());
        elevator.setDestinationFloor(2);
        assertEquals(2, elevator.getDestinationFloor());
        elevator.setDirectionUp(false);
        assertFalse(elevator.getDirectionUp());
        elevator.setCurrentState(Elevator_State.MOVING_DOWN);
        assertEquals(Elevator_State.MOVING_DOWN, elevator.getCurrentState());
    
        UserInput passenger = new UserInput("Alex", 3);
        elevator.addPassenger(passenger);
        assertTrue(elevator.getPassengers().contains(passenger));
        elevator.removePassenger(passenger);
        assertFalse(elevator.getPassengers().contains(passenger));
        elevator.setPort(1234);
        assertEquals(1234, elevator.getPort());
  
  }
  

}
