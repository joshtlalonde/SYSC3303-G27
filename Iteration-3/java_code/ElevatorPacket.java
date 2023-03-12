import java.io.*;
import java.net.*;
import java.util.*;

public class ElevatorPacket {

    private DatagramPacket sendElevatorPacket; // Holds the Sent Datagram Packet
    private DatagramPacket receiveElevatorPacket; // Holds the Received Datagram Packet

    private int elevatorNumber; // Holds number of the elevator
    private boolean isMoving; // Holds if elevator is moving
    private int currentFloor; // Holds the current floor info
    private int destinationFloor; // Holds the destination floor info
    private boolean directionUp; // Holds the direction info
    private ArrayList<Integer> passengerDestinations;

    public ElevatorPacket(int elevatorNumber, boolean isMoving, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations) {
        this.elevatorNumber = elevatorNumber;
        this.isMoving = isMoving;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
	}

    /** 
     * Used to send the packet 
     * 
     * @address destination address, 
     * @port destination port, 
     * @sendElevatorSocket socket to send on
    */
    public void send(InetAddress address, int port, DatagramSocket sendElevatorSocket) {
        byte sendbytes[];
        
        // Combine the different attributes of the packet into one array of bytes packet
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(elevatorNumber);
        outputStream.write(isMoving ? 1 : 0);
        outputStream.write(currentFloor);
        outputStream.write(destinationFloor);
        outputStream.write(directionUp ? 1 : 0);
        for (int passenger : passengerDestinations) {
            outputStream.write(passenger);
        }
        outputStream.write(0xFF); // Write an ending character for the passengerDestinations array
        sendbytes = outputStream.toByteArray();

        // Create datagram packet
		sendElevatorPacket = new DatagramPacket(sendbytes, sendbytes.length, address, port);

        // System.out.println("FloorPacket: Sending packet...");
		System.out.println("To address: " + sendElevatorPacket.getAddress());
		System.out.println("on destination port: " + sendElevatorPacket.getPort());
		System.out.println("with length: " + sendElevatorPacket.getLength());
		System.out.print("Containing: ");
		this.printPacket();
		System.out.print("Bytes: ");
		this.printPacketBytes(sendbytes);

		// Send the datagram packet to the server via the send/receive socket. 
		try {
			sendElevatorSocket.send(sendElevatorPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("ElevatorPacket: Packet sent.\n");
    }

    /** Used to wait until a FloorPacket is received */
    public void receive(DatagramSocket receiveElevatorSocket) {
        // Construct a DatagramPacket for receiving packets up to 100 bytes long. Will not be longer
		byte data[] = new byte[100];
		receiveElevatorPacket = new DatagramPacket(data, data.length);

		// Block until a datagram packet is received from receiveSocket.
		try {        
			// System.out.println("FloorPacket: Waiting for Floor Packet..."); // so we know we're waiting
			receiveElevatorSocket.receive(receiveElevatorPacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Process the received datagram.
		System.out.println("ElevatorPacket: Elevator Packet received:");
		System.out.println("From address: " + receiveElevatorPacket.getAddress());
		System.out.println("on port: " + receiveElevatorPacket.getPort());
		System.out.println("with length: " + receiveElevatorPacket.getLength());

        // Convert the bytes to assign packet variables
        this.convertBytesToPacket(receiveElevatorPacket.getData());

		// Print packet info
		System.out.print("Containing: ");
		this.printPacket();
		System.out.print("Bytes: ");
		this.printPacketBytes(receiveElevatorPacket.getData());

        System.out.println();
    }

    public void convertBytesToPacket(byte packet[]) {
		// Create new ElevatorPacket object from data
        elevatorNumber = packet[0];
        isMoving = packet[1] == 1 ? true : false;
        currentFloor = packet[2];
        destinationFloor = packet[3];
        directionUp = packet[4] == 1 ? true : false;

        // Get each of the destination that the passengers want to go on this elevator
        int i = 5;
        while (packet[i] != -1) {
            // Convert bytes into int array
            passengerDestinations.add(Integer.parseInt(Byte.toString(packet[i])));
            System.out.println(i + ": " + packet[i]);
            i++;
        }
    }

    ///////////// GETTERS AND SETTERS /////////////
    
    public int getElevatorNumber() {
        return elevatorNumber;
    }

    public boolean getIsMoving() {
        return isMoving;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public boolean getDirectionUp() {
        return directionUp;
    }

    public ArrayList<Integer> getPassengerDestinations() {
        return passengerDestinations;
    }

    public DatagramPacket getSendElevatorPacket() {
        return sendElevatorPacket;
    }

    public DatagramPacket getReceiveElevatorPacket() {
        return receiveElevatorPacket;
    }

    ///////////// PRINTERS /////////////

    private void printPacket() {
        System.out.println("Elevator number: " + elevatorNumber + ", is elevator moving: " + (isMoving ? "yes" : "no") + 
                            ", current floor: " + currentFloor + ", destination floor: " + destinationFloor + 
                            ", direction: " + (directionUp ? "up" : "down") + ", passenger destinations: " + (passengerDestinations.toString()));
    }

    private void printPacketBytes(byte packet[]) {
		for (int i = 0; i < packet.length; i++) {
			System.out.print(String.format("0x%02X ", packet[i]));
		}
		// Print new line
		System.out.println();
	}
}