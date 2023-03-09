import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class FloorPacket {
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

	private DatagramSocket sendReceiveSocket;
    private int floor, destinationFloor;
    private Date time;
    private boolean directionUp;

    public FloorPacket(int floor, Date time, boolean directionUp, int destinationFloor) {
		try {
			// Construct a datagram socket and bind it to any available 
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets to the Scheduler.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 

        this.floor = floor;
        this.time = time;
        this.directionUp = directionUp;
        this.destinationFloor = destinationFloor;
	}

    /** Used to send the packet */
    public void send(InetAddress address, int port) {
        DatagramPacket sendPacket;
        byte sendbytes[];

        // Convert the time to a bytes
        byte timeBytes[] = dateFormatter.format(time).getBytes();
        
        // Combine the different attributes of the packet into one array of bytes packet
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(floor);
        outputStream.write(destinationFloor);
        outputStream.write(directionUp ? 1 : 0);
        outputStream.write(timeBytes, 0, timeBytes.length);
        outputStream.write(0); // Add null character after string
        sendbytes = outputStream.toByteArray();

        // Create datagram packet
		sendPacket = new DatagramPacket(sendbytes, sendbytes.length, address, port);

        System.out.println("FloorPacket: Sending packet...");
		System.out.println("To address: " + sendPacket.getAddress());
		System.out.println("Destination port: " + sendPacket.getPort());
		System.out.println("Length: " + sendPacket.getLength());
		System.out.print("Containing: ");
		this.printPacket();
		System.out.print("Bytes: ");
		this.printPacketBytes(sendbytes);

		// Send the datagram packet to the server via the send/receive socket. 
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("FloorPacket: Packet sent.\n");
    }

    /** Used to wait until a FloorPacket is received */
    public void receive(byte packet[]) {
        floor = packet[0];
        destinationFloor = packet[1];
        directionUp = packet[2] == 1 ? true : false;

        // Skip the first 3 bytes then copy until the null character is reached, to get the Time string
        int i = 3;
        for (; i < packet.length && packet[i] != 0; i++) {}
        String timeString = new String(packet, 3, i - 3);
        
        System.out.println(timeString);
        
        try {
            time = dateFormatter.parse(timeString);
        } catch (ParseException e) {
            System.out.println("Failed to convert Time from received packet: " + e);
            e.printStackTrace();
        }
    }

    ///////////// GETTERS AND SETTERS /////////////
    
    public int getFloor() {
        return floor;
    }

    public Date getTime() {
        return time;
    }

    public boolean getDirectionUp() {
        return directionUp;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    ///////////// PRINTERS /////////////
    public void printPacket() {
        System.out.println("Floor number: " + floor + ", destination floor: " + destinationFloor + 
                            ", direction: " + (directionUp ? "Up" : "Down") + ", time: " + dateFormatter.format(time));
    }

    public void printPacketBytes(byte packet[]) {
		for (int i = 0; i < packet.length; i++) {
			System.out.print(String.format("0x%02X ", packet[i]));
		}
		// Print new line
		System.out.println();
	}
}