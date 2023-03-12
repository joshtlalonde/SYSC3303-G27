# SYSC3303-G27

# ======= ITERATION 3 =======

## Files included:

- Floor.java
- FloorPacket.java
- Elevator.java
- ElevatorPacket.java
- Scheduler.java
- floor_input.text
- Diagrams
- Unit tests

In-depth description about each file and their roles:

## Diagrams 
Folder containing two UML diagrams (one class and sequence diagram) describing the elevator system and their functions/methods. The class diagram shows the subsystems and their relations to other subsystems in a blue-print/schematic format. The sequence diagram on the other hand shows these subsystems in the context of their lifelines. 

## State diagram
State diagram is used to describe the change in state by any kind of input event. We used state diagrams to describe the state transition and the series of events that occur after a floor button is pressed.

## floor_input.text
This is the file within which the user input is stored, polled and received from the rest of the subsystems. Its format is primarily in a table with columns for Time, Floor, Floor button and Car button. These provide key information that the rest of the subsystems need to schedule and execute. 

## Floor.java
This file represents a floor in the building within which the elevator functions. The primary task for the floor is to retrieve and send data from the floor_input file to provide updates for user input and current status to the scheduler. For every line read from the floor_input file there is a simulated button clicked outside of the elevator. When this occurs the Floor sends a FloorPacket request to the scheduler to notify it that there is a person waiting on that specific floor.

## FloorPacket.java
This file is used as a helper class to send and receive packets between the scheduler and the floor. The floor needs to communicate with the scheduler that a new user has arrived. Therefore, the information that needs to be sent to the scheduler is the floor, destination floor, time, and direction of the request. The FloorPacket.java class ensures that all of that data is sent and received properly formatted to allow for easy access and manipulation of the data

## Scheduler.java
The scheduler is the brain of the program. The scheduler manages the elevators assigning them to the various floor requests that comes form the users. Firstly, the Floor sends a UserInput packet to the scheduler whenever a button is pressed requesting for an elevator. It then uses the input given to it and with the aid of some elevator algorithms assigns the appropriate elevator to the users request. Some changes made from last iteration include the introduction of a new helper class 'ElevatorInfo' as well as adjusting many methods to work with specific elevator states like 'serviceElevatorStopRequest', 'serviceElevatorMovingRequest' and 'serviceElevatorIdleRequest'.

## Elevator.java
Elevator simply takes information from the scheduler using its get() method. The elevator also has lights and buttons to the elevator. When an elevator button is pressed, it will transition from one floor to another.

## ElevatorPacket.java
This file is used as a helper class to send and receive packets between the scheduler and the elevator. The elevator needs to communicate with the scheduler that an elevator has stopped or is moving between floors. Therefore, the information that needs to be sent to the scheduler is the elevator number, if the elvator is moving, current floor, destination floor, direction the elevator is moving, and the floors that the passengers wish to stop at. The ElevatorPacket.java class ensures that all of that data is sent and received properly formatted to allow for easy access and manipulation of the data.

## Unit Tests
The `java_test_code` folder contains the different Unit Test files that are used to ensure the proper functionality of all of the methods for each of the classes. Each of the java files are associated to a different class where the JUnit assert functions are used to ensure that the actual results of a method of the testing class is equivalent to the expected results

# Responsibilities 
## Josh : 101109655 (
- Coded Communication Classes and how they interact
- Coded Elevator Idle and Moving State
- Coded Scheduler to handle Floor and Elevator requests
- Coded new Class for Elevator Info
- Coded Unit Tests for Packet Classes
- Created Class Diagram

## Sanskar : 101189876 (Created Sequence diagram, Aided with code choices)
  
## Jakob : 101201314 (Created Sequence diagram, Aided with code choices)
  
## Sanya : 101205395(Wrote the readme file, helped with the state diagram and edited some code)
 
## Partha : 101191302 (Coded the unit tests)
  

# Setup and Test Instructions
## Add JUnit Library to exclipse
- Right click on the `ElevatorPacketTest.java` and `FloorPacketTest.java` 
- Go to properties
- Click on library
- Add the Junit library

Download the `Elevator.java`, `Floor.java`, and `Scheduler.java` files and open them within a the same java project on eclipse as the test files
For testing the code, run `ElevatorPacketTest.java` and `FloorPacketTest.java` by right-clicking and RunAs JUnit test.
