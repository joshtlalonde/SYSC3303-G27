import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class FloorPacket {
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.S", Locale.ENGLISH);

    private DatagramPacket sendFloorPacket; // Holds the Sent Datagram Packet
    private DatagramPacket receiveFloorPacket; // Holds the Received Datagram Packet

    private int floor; // Holds the floor info
    private int destinationFloor; // Holds the destination floor info
    private Date time; // Holds the time info
    private boolean directionUp; // Holds the direction info

    public FloorPacket(int floor, Date time, boolean directionUp, int destinationFloor) {
        this.floor = floor;
        this.time = time;
        this.directionUp = directionUp;
        this.destinationFloor = destinationFloor;
	}

    /** Default Constructor */
    public FloorPacket() {
        this.floor = 0;
        this.time = new Date();
        this.directionUp = false;
        this.destinationFloor = 0;
	}

    /** 
     * Used to send the packet 
     * 
     * @address destination address, 
     * @port destination port, 
     * @sendFloorSocket socket to send on
    */
    public void send(InetAddress address, int port, DatagramSocket sendFloorSocket, boolean sendToScheduler) {
        byte sendbytes[];

        // Convert the time to a bytes
        byte timeBytes[] = dateFormatter.format(time).getBytes();
        
        // Combine the different attributes of the packet into one array of bytes packet
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (sendToScheduler) {
            outputStream.write(0); // Add first byte to let scheduler know it is a floor packet
        }
		outputStream.write(floor);
        outputStream.write(destinationFloor);
        outputStream.write(directionUp ? 1 : 0);
        outputStream.write(timeBytes, 0, timeBytes.length);
        outputStream.write(0); // Add null character after string
        sendbytes = outputStream.toByteArray();

        // Create datagram packet
		sendFloorPacket = new DatagramPacket(sendbytes, sendbytes.length, address, port);

        // System.out.println("FloorPacket: Sending packet...");
		System.out.println("To address: " + sendFloorPacket.getAddress());
		System.out.println("on destination port: " + sendFloorPacket.getPort());
		System.out.println("with length: " + sendFloorPacket.getLength());
		System.out.print("Containing: ");
		this.printPacket();
		System.out.print("Bytes: ");
		this.printPacketBytes(sendbytes);

		// Send the datagram packet to the server via the send/receive socket. 
		try {
			sendFloorSocket.send(sendFloorPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("FloorPacket: Packet sent.\n");
    }

    /** Used to wait until a FloorPacket is received */
    public void receive(DatagramSocket receiveFloorSocket) {
        // Construct a DatagramPacket for receiving packets up to 16 bytes long. Will not be longer
		byte data[] = new byte[16];
		receiveFloorPacket = new DatagramPacket(data, data.length);

		// Block until a datagram packet is received from receiveSocket.
		try {        
			// System.out.println("FloorPacket: Waiting for Floor Packet..."); // so we know we're waiting
			receiveFloorSocket.receive(receiveFloorPacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Process the received datagram.
		System.out.println("FloorPacket: Floor Packet received:");
		System.out.println("From address: " + receiveFloorPacket.getAddress());
		System.out.println("on port: " + receiveFloorPacket.getPort());
		System.out.println("with length: " + receiveFloorPacket.getLength());

        // Convert the bytes to assign packet variables
        this.convertBytesToPacket(receiveFloorPacket.getData());

		// Print packet info
		System.out.print("Containing: ");
		this.printPacket();
		System.out.print("Bytes: ");
		this.printPacketBytes(receiveFloorPacket.getData());

        System.out.println();
    }

    public void convertBytesToPacket(byte packet[]) {
		// Create new FloorPacket object from data
        floor = packet[0];
        destinationFloor = packet[1];
        directionUp = packet[2] == 1 ? true : false;

        // Skip the first 3 bytes then copy until the null character is reached, to get the Time string
        int i = 3;
        for (; i < packet.length && packet[i] != 0; i++) {}
        String timeString = new String(packet, 3, i - 3);
        
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
    
    public DatagramPacket getSendFloorPacket() {
        return sendFloorPacket;
    }

    public DatagramPacket getReceiveFloorPacket() {
        return receiveFloorPacket;
    }

    ///////////// PRINTERS /////////////

    private void printPacket() {
        System.out.println("Floor number: " + floor + ", destination floor: " + destinationFloor + 
                            ", direction: " + (directionUp ? "Up" : "Down") + ", time: " + dateFormatter.format(time));
    }

    private void printPacketBytes(byte packet[]) {
		for (int i = 0; i < packet.length; i++) {
			System.out.print(String.format("0x%02X ", packet[i]));
		}
		// Print new line
		System.out.println();
	}
}