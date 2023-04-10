import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class FloorPacketTest extends junit.framework.TestCase {

    public void testConstructor() {
        /** Create a Floor Packet */
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2, true, false);

        /** Assert each of the attributs are equal */
        assertEquals(floorPacket.getFloor(), 1);
        try {
            assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(floorPacket.getDirectionUp(), false);
        assertEquals(floorPacket.getDestinationFloor(), 2);
        assertEquals(floorPacket.getDoorFault(), true);
        assertEquals(floorPacket.getHardFault(), false);
    }

    public void testSend() {
        /** Create a Floor Packet */
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2, true, false);

        /** Get local address to be sent to the test IP address */
        InetAddress localAddr;
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        /** Send the Packet */
        try {
            floorPacket.send(localAddr, 23, new DatagramSocket(), false);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /** Assert that the values expected are what we sent in the UDP datagram packet */
        byte testData[] = new byte[18];
        testData[0] = 0x01;
        testData[1] = 0x02;
        testData[2] = 0x00;
        testData[3] = 0x01;
        testData[4] = 0x00;
        byte dateBytes[] = dateFormatter.format(date).getBytes();
        int i = 5;
        for (; i < dateBytes.length + 5; i++) {
            testData[i] = dateBytes[i - 5];
        }
        testData[i] = (byte)0xFF;

        floorPacket.convertBytesToPacket(testData);
        floorPacket.convertBytesToPacket(floorPacket.getSendFloorPacket().getData());

        assertTrue(Arrays.equals(floorPacket.getSendFloorPacket().getData(), testData));
        assertEquals(floorPacket.getSendFloorPacket().getLength(), 18);
        assertEquals(floorPacket.getSendFloorPacket().getAddress(), localAddr);
        assertEquals(floorPacket.getSendFloorPacket().getPort(), 23);
    }

    public void testReceive() {
        /** Create a Floor Packet */
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2, true, false);

        /** Get local address to be sent to the test IP address */
        InetAddress localAddr;
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        /** Create a socket to send on and receive on */
        DatagramSocket sendSocket, receiveSocket;
        try {
            sendSocket = new DatagramSocket();
            receiveSocket = new DatagramSocket(69);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        /** Send then receive the Packet */
        floorPacket.send(localAddr, 69, sendSocket, false);
        floorPacket.receive(receiveSocket);

        /** Compare and assert that the data in the receive packet is as expected */
        byte testData[] = new byte[18];
        testData[0] = 0x01;
        testData[1] = 0x02;
        testData[2] = 0x00;
        testData[3] = 0x01;
        testData[4] = 0x00;
        byte dateBytes[] = dateFormatter.format(date).getBytes();
        int i = 5;
        for (; i < dateBytes.length + 5; i++) {
            testData[i] = dateBytes[i - 5];
        }
        testData[i] = (byte)0xFF;

        assertTrue(Arrays.equals(floorPacket.getReceiveFloorPacket().getData(), testData));
        assertEquals(floorPacket.getReceiveFloorPacket().getLength(), 18);
        assertEquals(floorPacket.getReceiveFloorPacket().getAddress(), localAddr);
    }

    public void testConvertBytesToPacket() {
        /** Create a Floor Packet */
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2, true, false);

        /** Create the expected Bytes from the floor */
        byte testPacket[] = { 0x01, 0x02, 0x00, 0x01, 0x00, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x2E, 0x35, (byte)0xFF };

        /** Call the Testing function */
        floorPacket.convertBytesToPacket(testPacket);

        /** Compare and assert that the data in the receive packet is as expected */
        assertEquals(floorPacket.getFloor(), 1);
        assertEquals(floorPacket.getDestinationFloor(), 2);
        assertEquals(floorPacket.getDirectionUp(), false);
        assertEquals(floorPacket.getDoorFault(), true);
        assertEquals(floorPacket.getHardFault(), false);
        try {
            assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testGettersSetters() {
        /** Create a Floor Packet */
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2, true, false);

        /** Assert Getters */
        assertEquals(floorPacket.getFloor(), 1);
        try {
			assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        assertEquals(floorPacket.getDirectionUp(), false);
        assertEquals(floorPacket.getDestinationFloor(), 2);
        assertEquals(floorPacket.getDoorFault(), true);
        assertEquals(floorPacket.getHardFault(), false);
        assertEquals(floorPacket.getTimeoutFlag(), false);
    }
}