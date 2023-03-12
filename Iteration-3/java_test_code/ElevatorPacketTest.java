import java.net.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class ElevatorPacketTest extends junit.framework.TestCase{ 

    public void testConstructor() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<Integer>());

        // Assert each of the attributs are equal
        assertEquals(elevatorPacket.getElevatorNumber(), 1);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 1);
        assertEquals(elevatorPacket.getDestinationFloor(), 2);
        assertEquals(elevatorPacket.getDirectionUp(), true);
        assertEquals(elevatorPacket.getPassengerDestinations(), new ArrayList<Integer>());
    }

    public void testSend() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<Integer>());

        InetAddress localAddr;
		try {
			localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
        try {
			elevatorPacket.send(localAddr, 69, new DatagramSocket());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        byte testData[] = {0x01, 0x01, 0x01, 0x02, 0x01, (byte)0xFF};
        assertTrue(Arrays.equals(elevatorPacket.getSendElevatorPacket().getData(), testData));
        assertEquals(elevatorPacket.getSendElevatorPacket().getLength(), 6);
        assertEquals(elevatorPacket.getSendElevatorPacket().getAddress(), localAddr);
        assertEquals(elevatorPacket.getSendElevatorPacket().getPort(), 69);
    }

    public void testReceive() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<Integer>());

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

        elevatorPacket.send(localAddr, 69, sendSocket);
		elevatorPacket.receive(receiveSocket);

        byte testData[] = new byte[100];
        testData[0] = 0x01;
        testData[1] = 0x01;
        testData[2] = 0x01;
        testData[3] = 0x02;
        testData[4] = 0x01;
        testData[5] = (byte)0xFF;

        assertTrue(Arrays.equals(elevatorPacket.getReceiveElevatorPacket().getData(), testData));
        assertEquals(elevatorPacket.getReceiveElevatorPacket().getLength(), 6);
        assertEquals(elevatorPacket.getReceiveElevatorPacket().getAddress(), localAddr);
    }

    public void testConvertBytesToPacket() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<Integer>());
        
        byte testPacket[] = {0x02, 0x01, 0x04, 0x07, 0x01, 0x02, 0x03, 0x04, (byte)0xFF};
        elevatorPacket.convertBytesToPacket(testPacket);
                
        ArrayList<Integer> testPassengerDestinations = new ArrayList<Integer>();
        testPassengerDestinations.add(2); testPassengerDestinations.add(3); testPassengerDestinations.add(4);
        
        assertEquals(elevatorPacket.getPassengerDestinations().size(), testPassengerDestinations.size());
        for (int i = 0; i < elevatorPacket.getPassengerDestinations().size(); i++) {
            assertEquals(elevatorPacket.getPassengerDestinations().get(i), testPassengerDestinations.get(i));
        }

        assertEquals(elevatorPacket.getElevatorNumber(), 2);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 4);
        assertEquals(elevatorPacket.getDestinationFloor(), 7);
        assertEquals(elevatorPacket.getDirectionUp(), true);
    }

    public void testGettersSetters() {
        ElevatorPacket elevatorPacket = new ElevatorPacket(1, true, 1, 2, true, new ArrayList<Integer>());
        
        // Assert Getters
        assertEquals(elevatorPacket.getElevatorNumber(), 1);
        assertEquals(elevatorPacket.getIsMoving(), true);
        assertEquals(elevatorPacket.getCurrentFloor(), 1);
        elevatorPacket.setDestinationFloor(5);
        assertEquals(elevatorPacket.getDestinationFloor(), 5);
        assertEquals(elevatorPacket.getDirectionUp(), true);
    }
}
