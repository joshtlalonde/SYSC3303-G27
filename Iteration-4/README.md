# SYSC3303-G27

# ======= Final Project =======

# Setup and Test Instructions
### Adding Files to ecplipse
1. Start a new project in Eclipse with any name
2. Delete the `module-info` folder/file
3. Locate the folder of this new project on your computer
4. Once located, go to the directory `<ProjectName>/src` and insert the files located in our `G27_Code/src` directory
5. Then right click on the project directory on the eclipse package explorer to add a new `Source Folder`, name this folder `tests`
6. Locate the new tests directory in your computers file explorer, then insert the files located in our `G27_Code/tests` directory into the new tests folder you just created
7. Move the `floor_input.txt` and `README.md` into your directory `<ProjectName>` 
8. Now you should be able to `refresh` the package explorer by right clicking and selecting `refresh`
9. You should now see the src and tests folders along with the floor_input.txt and the README.md

### Add JUnit Library to eclipse
1. Right click on each of the files in the `tests` folder
2. Go to properties
3. Click on library
4. Add the Junit library

For testing the code, run the files in the `tests` folder by right-clicking and RunAs JUnit test.

### Accessing the Floor_Input.txt File
To run the `Floor.java` file the path to the `floor_input.txt` must be changed to match the path of where it is located on your computer
To change the path edit the constant `FILENAME` at the top of the `Floor.java` file to the path of where the file is located on your computer 

### Changing number of Floors and Elevators
The following three files must have the constants matching eachother

##### Elevator.java
- Edit the constant `NUMBER_OF_FLOORS` at the top of the file to desired number of floors
- Edit the constant `NUMBER_OF_ELEVATORS` at the top of the file to desired number of elevators

##### Floor.java
- Edit the constant `NUMBER_OF_FLOORS` at the top of the file to desired number of floors

##### Scheduler.java
- Edit the constant `NUMBER_OF_FLOORS` at the top of the file to desired number of floors

## Files included:
##### Elevator_State
An enumeration file to enumerate the states of the elevator.

##### Elevator
Holds Elevator state based control object.
Holds the Elevator boundary objects.

##### ElevatorGUI
Holds the GUI for the elevator subsystem.

##### ElevatorPacket
Holds the application logic object to enable communication between the scheduler and elevator.

##### Floor
Holds the Floor periodic control object.
Holds the Floor boundary objects.
Holds the UserInput entity object.

##### FloorPacket
Holds the application logic object to enable communication between the scheduler and floor.

##### Scheduler_State
An enumeration file to enumerate the states of the scheduler.

##### Scheduler
Holds the Scheduler state based coordinator control object.

##### Unit Tests
A test for all classes within the project are in the `tests` folder.
The names of the tests match the class name that they are testing

# In-depth description about each file and their roles:
##### Diagrams 
Folder containing UML diagrams describing the elevator system and their functions/methods. The state diagrams show the subsystems and their relations to other subsystems. The sequence diagram on the other hand shows these subsystems in the context of their lifelines. 

##### floor_input.txt
This is the file within which the user input is stored, polled and received for the Floor subsystems. Its format is in a table with columns for Time, Floor, Floor button and Car button, door fault, and hard fault. These provide key information that the rest of the subsystems needs to schedule and run the elevators. 

##### Elevator.java
The elevator represents the physical elevators that the system is operating with. The elevator has eight states Idle, Moving Up, Moving Down, Stopped, Door Open, Door Close, Door Fault, and Hard Fault. Elevator receives requests from scheduler and then acts accordingly based on what state the elevator is in. 
While in the Idle state the elevator is waiting for any new requests to come from the scheduler; when a request is received the motor of the elevator activates and the elevator travels to its passenger. 
While in one of the moving states the elevator is transporting passengers to their desired location. As the elevator travels past each floor the elevator checks with the scheduler seeing if any passengers are heading in the same direction, if a passenger is present and heading in the same direction the elevator collects the passenger. The elevator also checks with the scheduler to determine if any passengers that are on the elevator currently are wanting to get off on the current floor. 
In the stopped state the elevator turns off its motor at a floor. 
In the door open state any passengers that are to get off on the current floor exit the elevator. 
Then in the door close state any passengers that had gotten on the elevator notify the elevator of their destination through a button click, this is then communicated to the scheduler. After the door close state the elevator will either return to the Idle or Moving states depending on if their are any passengers on the elevator or not.
If there is a passenger that causes a door fault the elevator moves into the door fault state. When this happens the elevator notifies the scheduler and the scheduler resets the passenger's door fault flag and tells the elevator to move back to the stopped state.
If there is a passenger that causes a hard fault then while the elevator is moving it will detect this passenger's hard fault flag and move into the hard fault state. Once in this state it will notify the scheduler and the scheduler will command the elevator to turn off.

##### ElevatorPacket.java
This file is used as a helper class to send and receive packets between the scheduler and the elevator. The elevator needs to communicate with the scheduler what the current state of the elevator is. Therefore, the information that needs to be sent to the scheduler is the elevator number, if the elvator is moving, current floor, destination floor, direction the elevator is moving, and the passengers that the elevator has in it. The ElevatorPacket.java class ensures that all of that data is sent and received properly formatted to allow for easy access and manipulation of the data.

##### ElevatorGUI.java
The ElevatorGUI file is used to create a graphical user interface for our elevator subsystem. It is updated by the scheduler everytime a new packet is received. The gui presents the state information of the elevator: elevator number, if the elvator is moving, current floor, destination floor, direction the elevator is moving, and the passengers that the elevator has in it. It as well displays a moving object that simulates the elevators moving between floors.

##### Floor.java
This file represents the floors in the building in which the elevator functions. The primary task for the floor is to retrieve and send data from the floor_input file to provide updates to the scheduler for when a new user arrives at a floor. For every line read from the floor_input file there is a simulated button clicked on their floor. When this occurs the Floor sends a FloorPacket request to the scheduler to notify it that there is a person waiting on that specific floor.

##### FloorPacket.java
This file is used as a helper class to send and receive packets between the scheduler and the floor. The floor needs to communicate with the scheduler that a new user has arrived. Therefore, the information that needs to be sent to the scheduler is the floor, destination floor, time, and direction of the request. The FloorPacket.java class ensures that all of that data is sent and received properly formatted to allow for easy access and manipulation of the data

##### Scheduler.java
The scheduler is the brain of the program. The scheduler manages the elevators assigning them to the various floor requests that comes form the users. Firstly, the Floor sends a UserInput packet to the scheduler whenever a button is pressed requesting for an elevator. The scheduler then uses the input given to it and, with the aid of some algorithms, assigns the appropriate elevator to the users request. It also handles messages sent from the elevator by determining the current state of the elevator and then handling what information should be sent to the elevator as a response to their state in the request. 

##### Unit Tests
The `tests` folder contains the different Unit Test files that are used to ensure the proper functionality of all of the methods for each of the classes. Each of the java files are associated to a different class where the JUnit assert functions are used to ensure that the actual results of a method of the testing class is equivalent to the expected results

# Responsibilities 
##### Josh : 101109655 
- Coded ElevatorPacket and FloorPacket Classes and how they interact with the rest of the system
- Coded the complete Floor class
- Coded the UserInput and FloorButton and FloorLamp classes
- Coded Elevator Idle, Moving, and Door Open States
- Helped with coding of the Fault states
- Coded the Motor, Door, ArrivalSensor, ElevatorButton, ElevatorLamp, and DirectionLamp Classes
- Coded Scheduler receive, processFloor, processElevator, serviceElevatorDoorOpen, serviceElevatorDoorClose
- Helped with all service states and ensured multithreading capabilities
- Inserted ElevatorGUI into the scheduler
- Coded ElevatorInfo class
- Coded Unit Tests for ElevatorPacket and FloorPacket Classes
- Coded the Unit Tests for the receive, processFloor, processElevator states of the scheduler
- Created Class Diagram
- Helped with the coding of the Fault States
- Helped with the building of our state diagrams
- Helped with the building of our sequence diagram

##### Sanskar : 101189876 
- Created Elevator State Machine
- Created Scheduler State Machine
- Coded full GUI template, part operation
- Coded Scheduler algorithms: Idle, Stopped
- Helped with scheduler stopped, moving unit tests
  
##### Jakob : 101201314 
 - Coded GUI, particularly GUI operation
 - Coded SchedulerTest stopped, idle and moving unit tests
 - Aided with Elevator State Machine
 - Aided with Scheduler State Machine
  
##### Sanya : 101205395
- Helped with Class diagrams
- Helped with the code
- Aided in creation and editing of UML diagrams
- Created unit tests for Floor.java
- Created unit tests for Scheduler.java

 
##### Partha : 101191302
- Helped with State diagrams
- Helped with Elevator.java
- Created unit tests for Elevator.java
- Coded the Transient Faults and the Hard Faults for the Elevator
