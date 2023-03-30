import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class ElevatorPacketTest extends junit.framework.TestCase{ 

    public void testConstructor() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 2, 4, false, new ArrayList<UserInput>(), Elevator_State.MOVING_DOWN);

        // Assert each of the attributs are equal
        assertEquals(elevatorPacket.getElevatorNumber(), 1);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 2);
        assertEquals(elevatorPacket.getDestinationFloor(), 4);
        assertEquals(elevatorPacket.getDirectionUp(), false);
        assertEquals(elevatorPacket.getPassengers(), new ArrayList<UserInput>());
        assertEquals(elevatorPacket.getCurrentState(), Elevator_State.MOVING_DOWN);
    }

    public void testDefaultConstructor() {
        ElevatorPacket elevatorPacket = new ElevatorPacket();

        // Assert each of the attributs are equal
        assertEquals(elevatorPacket.getElevatorNumber(), 0);
        assertEquals(elevatorPacket.getIsMoving(), false);
        assertEquals(elevatorPacket.getCurrentFloor(), 0);
        assertEquals(elevatorPacket.getDestinationFloor(), 0);
        assertEquals(elevatorPacket.getDirectionUp(), false);
        assertEquals(elevatorPacket.getPassengers(), new ArrayList<UserInput>());
        assertEquals(elevatorPacket.getCurrentState(), Elevator_State.IDLE);
    }

    public void testSend() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<UserInput>(),  Elevator_State.MOVING_UP);

        InetAddress localAddr;
		try {
			localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
        try {
			elevatorPacket.send(localAddr, 69, new DatagramSocket(), false);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        byte testData[] = {0x01, 0x01, 0x01, 0x02, 0x01, 0x02, (byte)0xFF};
        assertTrue(Arrays.equals(elevatorPacket.getSendElevatorPacket().getData(), testData));
        assertEquals(elevatorPacket.getSendElevatorPacket().getLength(), testData.length);
        assertEquals(elevatorPacket.getSendElevatorPacket().getAddress(), localAddr);
        assertEquals(elevatorPacket.getSendElevatorPacket().getPort(), 69);
    }

    public void testReceive() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP);

        InetAddress localAddr;
		try {
			localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
        DatagramSocket sendSocket, receiveSocket;
		try {
			sendSocket = new DatagramSocket();
            receiveSocket = new DatagramSocket(69);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

        elevatorPacket.send(localAddr, 69, sendSocket, false);
		elevatorPacket.receive(receiveSocket);

        byte testData[] = new byte[100];
        testData[0] = 0x01;
        testData[1] = 0x01;
        testData[2] = 0x01;
        testData[3] = 0x02;
        testData[4] = 0x01;
        testData[5] = 0x02;
        testData[6] = (byte)0xFF;

        assertTrue(Arrays.equals(elevatorPacket.getReceiveElevatorPacket().getData(), testData));
        assertEquals(elevatorPacket.getReceiveElevatorPacket().getLength(), 7);
        assertEquals(elevatorPacket.getReceiveElevatorPacket().getAddress(), localAddr);
    }

    public void testConvertBytesToPacket() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(2, true, 4, 7, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP);
        
        // Create the expected Bytes from the elevator
        byte testPacket[];
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
        expectedOutput.write(elevatorPacket.getElevatorNumber());
        expectedOutput.write(elevatorPacket.getIsMoving() ? 1 : 0);
        expectedOutput.write(elevatorPacket.getCurrentFloor());
        expectedOutput.write(elevatorPacket.getDestinationFloor());
        expectedOutput.write(elevatorPacket.getDirectionUp() ? 1 : 0);
        expectedOutput.write(elevatorPacket.convertStateToInt(elevatorPacket.getCurrentState()));
        try {
            ArrayList<UserInput> passengers = new ArrayList<UserInput>();
            passengers.add(new UserInput(new Date(72), 0, false, 0, true, false));
            passengers.add(new UserInput(new Date(100), 1, false, 2, false, true));
            passengers.add(new UserInput(new Date(1680143675), 1, false, 2, true, true));

            for (UserInput passenger : passengers)
			expectedOutput.write(passenger.convertToBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        expectedOutput.write(0xFF);
        testPacket = expectedOutput.toByteArray();
        
        // Call the Testing function
        try {
			elevatorPacket.convertBytesToPacket(testPacket);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                
        
        ArrayList<UserInput> testPassengers = new ArrayList<UserInput>();
        testPassengers.add(new UserInput(new Date(72), 0, false, 0, true, false));
        testPassengers.add(new UserInput(new Date(100), 1, false, 2, false, true));
        testPassengers.add(new UserInput(new Date(1680143675), 1, false, 2, true, true));
        
        assertEquals(elevatorPacket.getPassengers().size(), testPassengers.size());
        for (int i = 0; i < elevatorPacket.getPassengers().size(); i++) {
            assertEquals(elevatorPacket.getPassengers().get(i).toString(), testPassengers.get(i).toString());
        }

        assertEquals(elevatorPacket.getElevatorNumber(), 2);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 4);
        assertEquals(elevatorPacket.getDestinationFloor(), 7);
        assertEquals(elevatorPacket.getDirectionUp(), true);
    }

    public void testGettersSetters() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP);
        
        // Assert Getters
        assertEquals(elevatorPacket.getElevatorNumber(), 1);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 1);
        elevatorPacket.setDestinationFloor(5);
        assertEquals(elevatorPacket.getDestinationFloor(), 5);
        assertEquals(elevatorPacket.getDirectionUp(), true);
        assertEquals(elevatorPacket.getCurrentState(), Elevator_State.MOVING_UP);
    }

    public void testConvertStateToInt() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP);

        int elevatorStateInt = elevatorPacket.convertStateToInt(Elevator_State.MOVING_UP);
        assertEquals(elevatorStateInt, 2);
    }

    public void testConvertIntToState() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<UserInput>(), Elevator_State.MOVING_UP);

        Elevator_State elevatorState = elevatorPacket.convertIntToState(4);
        assertEquals(elevatorState, Elevator_State.STOPPED);
    }
}
