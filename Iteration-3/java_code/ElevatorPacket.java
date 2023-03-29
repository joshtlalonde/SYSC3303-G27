import java.io.*;
import java.net.*;
import java.util.*;

public class ElevatorPacket {
    static final int NUMBER_OF_FLOORS = 20; // Number of floors in the building

    private Elevator_State currentState = Elevator_State.IDLE; // Holds the current State of the elevator

    private DatagramPacket sendElevatorPacket; // Holds the Sent Datagram Packet
    private DatagramPacket receiveElevatorPacket; // Holds the Received Datagram Packet

    private int elevatorNumber; // Holds number of the elevator
    private boolean isMoving; // Holds if elevator is moving
    private int currentFloor; // Holds the current floor info
    private int destinationFloor; // Holds the destination floor info
    private boolean directionUp; // Holds the direction info
    private ArrayList<Integer> passengerDestinations;

    public ElevatorPacket(int elevatorNumber, boolean isMoving, int currentFloor, int destinationFloor, boolean directionUp, ArrayList<Integer> passengerDestinations, Elevator_State currentState) {
        this.elevatorNumber = elevatorNumber;
        this.isMoving = isMoving;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.directionUp = directionUp;
        this.passengerDestinations = passengerDestinations;
        this.currentState = currentState;
	}

    /** Default Constructor */
    public ElevatorPacket() {
        this.elevatorNumber = 0;
        this.isMoving = false;
        this.currentFloor = 0;
        this.destinationFloor = 0;
        this.directionUp = false;
        this.passengerDestinations = new ArrayList<Integer>();
        this.currentState = Elevator_State.IDLE;
    }

    /** 
     * Used to send the packet 
     * 
     * @address destination address, 
     * @port destination port, 
     * @sendElevatorSocket socket to send on
    */
    public void send(InetAddress address, int port, DatagramSocket sendElevatorSocket, boolean sendToScheduler) {
        byte sendbytes[];
        
        // Combine the different attributes of the packet into one array of bytes packet
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (sendToScheduler) {
            outputStream.write(1); // Add first byte to let scheduler know it is an elevator packet
        }
		outputStream.write(elevatorNumber);
        outputStream.write(isMoving ? 1 : 0);
        outputStream.write(currentFloor);
        outputStream.write(destinationFloor);
        outputStream.write(directionUp ? 1 : 0);
        outputStream.write(this.convertStateToInt(currentState));
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

    /** Used to wait until a ElevatorPacket is received */
    public void receive(DatagramSocket receiveElevatorSocket) {
        // Construct a DatagramPacket for receiving packets up to 100 bytes long. Will not be longer
		byte data[] = new byte[100];
		receiveElevatorPacket = new DatagramPacket(data, data.length);

		// Block until a datagram packet is received from receiveSocket.
		try {        
			// System.out.println("ElevatorPacket: Waiting for Elevator Packet..."); // so we know we're waiting
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
        currentState = this.convertIntToState(packet[5]);

        // Get each of the destination that the passengers want to go on this elevator
        int i = 6;
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

    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
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

    public Elevator_State getCurrentState() {
        return currentState;
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

    ///////////// HELPERS /////////////

    /** Converts the State (Ex: IDLE) to an Integer (Ex: 1) */
    public int convertStateToInt(Elevator_State state) {
        switch(state) {
            case IDLE:
                return 1;
            case MOVING_UP:
                return 2;
            case MOVING_DOWN:
                return 3;        
            case STOPPED:
                return 4;
            case DOOR_OPEN:
                return 5;
            case DOOR_CLOSE:
                return 6;
        }

        /** Error occured */
        System.out.print("Elevator_State Failed to convert state to int");
        return -1;
    }

    /** Converts an Integer (Ex: 1) to the associated State (Ex: IDLE) */
    public Elevator_State convertIntToState(int state) {
        switch(state) {
            case 1:
                return Elevator_State.IDLE;
            case 2:
                return Elevator_State.MOVING_UP;
            case 3:
                return Elevator_State.MOVING_DOWN;     
            case 4:
                return Elevator_State.STOPPED;
            case 5:
                return Elevator_State.DOOR_OPEN;
            case 6:
                return Elevator_State.DOOR_CLOSE;
        }

        /** Error occured */
        System.out.print("Elevator_State Failed to convert int to state");
        return null;
    }

    ///////////// PRINTERS /////////////

    private void printPacket() {
        System.out.println("Elevator number: " + elevatorNumber + ", elevator moving: " + (isMoving ? "Yes" : "No") + 
                            ", current floor: " + currentFloor + ", destination floor: " + destinationFloor + 
                            ", direction: " + (directionUp ? "Up" : "Down") + ", currentState: " + stateToString(currentState) +
                            ", passenger destinations: " + (passengerDestinations.toString()));
    }

    private void printPacketBytes(byte packet[]) {
		for (int i = 0; i < packet.length; i++) {
			System.out.print(String.format("0x%02X ", packet[i]));
		}
		// Print new line
		System.out.println();
	}

    public String stateToString(Elevator_State state) {
        switch(state) {
            case IDLE:
                return "IDLE";
            case MOVING_UP:
                return "MOVING_UP";
            case MOVING_DOWN:
                return "MOVING_DOWN";       
            case STOPPED:
                return "STOPPED";
            case DOOR_OPEN:
                return "DOOR_OPEN";
            case DOOR_CLOSE:
                return "DOOR_CLOSE";
        }

        /** Error occured */
        return "UNKNOWN STATE";
    }
}
