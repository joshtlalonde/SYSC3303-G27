import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class FloorPacketTest extends junit.framework.TestCase {

    public void testConstructor() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2);

        // Assert each of the attributs are equal
        assertEquals(floorPacket.getFloor(), 1);
        try {
            assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(floorPacket.getDirectionUp(), false);
        assertEquals(floorPacket.getDestinationFloor(), 2);
    }

    public void testSend() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2);

        InetAddress localAddr;
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        try {
            floorPacket.send(localAddr, 23, new DatagramSocket());
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte testData[] = new byte[14];
        testData[0] = 0x01;
        testData[1] = 0x02;
        testData[2] = 0x00;
        byte dateBytes[] = dateFormatter.format(date).getBytes();
        for (int i = 3; i < dateBytes.length + 3; i++) {
            testData[i] = dateBytes[i - 3];
        }

        // Assert the data
        assertTrue(Arrays.equals(floorPacket.getSendFloorPacket().getData(), testData));
        assertEquals(floorPacket.getSendFloorPacket().getLength(), 14);
        assertEquals(floorPacket.getSendFloorPacket().getAddress(), localAddr);
        assertEquals(floorPacket.getSendFloorPacket().getPort(), 23);
    }

    public void testReceive() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2);

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

        floorPacket.send(localAddr, 69, sendSocket);
        floorPacket.receive(receiveSocket);

        byte testData[] = new byte[16];
        testData[0] = 0x01;
        testData[1] = 0x02;
        testData[2] = 0x00;
        byte dateBytes[] = dateFormatter.format(date).getBytes();
        for (int i = 3; i < dateBytes.length + 3; i++) {
            testData[i] = dateBytes[i - 3];
        }

        assertTrue(Arrays.equals(floorPacket.getReceiveFloorPacket().getData(), testData));
        assertEquals(floorPacket.getReceiveFloorPacket().getLength(), 14);
        assertEquals(floorPacket.getReceiveFloorPacket().getAddress(), localAddr);
    }

    public void testConvertBytesToPacket() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2);

        byte testPacket[] = { 0x01, 0x02, 0x00, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x2E, 0x35, 0x00 };
        floorPacket.convertBytesToPacket(testPacket);

        assertEquals(floorPacket.getFloor(), 1);
        assertEquals(floorPacket.getDestinationFloor(), 2);
        assertEquals(floorPacket.getDirectionUp(), false);
        try {
            assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testGettersSetters() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);
        Date date;
        try {
            date = dateFormatter.parse("10:10:10.5");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        FloorPacket floorPacket = new FloorPacket(1, date, false, 2);

        // Assert Getters
        assertEquals(floorPacket.getFloor(), 1);
        try {
			assertEquals(floorPacket.getTime(), dateFormatter.parse("10:10:10.5"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        assertEquals(floorPacket.getDirectionUp(), false);
        assertEquals(floorPacket.getDestinationFloor(), 2);
    }
}
