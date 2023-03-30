import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class FloorPacketTest extends junit.framework.TestCase {

    public void testConstructor() {
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
        assertEquals(floorPacket.getDoorFault(), true);
        assertEquals(floorPacket.getHardFault(), false);
    }

    public void testSend() {
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

        InetAddress localAddr;
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        try {
            floorPacket.send(localAddr, 23, new DatagramSocket(), false);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

        // Assert the data
        assertTrue(Arrays.equals(floorPacket.getSendFloorPacket().getData(), testData));
        assertEquals(floorPacket.getSendFloorPacket().getLength(), 18);
        assertEquals(floorPacket.getSendFloorPacket().getAddress(), localAddr);
        assertEquals(floorPacket.getSendFloorPacket().getPort(), 23);
    }

    public void testReceive() {
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

        floorPacket.send(localAddr, 69, sendSocket, false);
        floorPacket.receive(receiveSocket);

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

        byte testPacket[] = { 0x01, 0x02, 0x00, 0x01, 0x00, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x3A, 0x31, 0x30, 0x2E, 0x35, (byte)0xFF };
        floorPacket.convertBytesToPacket(testPacket);

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
        assertEquals(floorPacket.getDoorFault(), true);
        assertEquals(floorPacket.getHardFault(), false);
    }
}
