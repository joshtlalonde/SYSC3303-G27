# SYSC3303-G27

# ======= ITERATION 2 =======

## Files included:

- Floor.java
- elevator.java
- Scheduler.java
- floor_input.text
- UML-diagrams
- State diagram
- Unit tests



In-depth description about each file and their roles:

## UML-diagrams 
Folder containing two UML diagrams (one class and sequence diagram) describing the elevator system and their functions/methods. The class diagram shows the subsystems and their relations to other subsystems in a blue-print/schematic format. The sequence diagram on the other hand shows these subsystems in the context of their lifelines. 

## State diagram
State diagram is used to describe the change in state by any kind of input event. We used state diagrams to describe the state transition and the series of events that occur after a floor button is pressed.


## floor_input.text
This is the file within which the user input is stored, polled and received from the rest of the subsystems. Its format is primarily in a table with columns for Time, Floor, Floor button and Car button. These provide key information that the rest of the subsystems need to schedule and execute. 

## Floor.java
This file represents a floor in the building within which the elevator functions. The primary task for the floor is to retrieve and send data from the floor_input file to provide updates for user input and current status to the scheduler. The floor also has floor buttons which communicates with the scheduler to determine which floor to go to.

## Scheduler.java
The scheduler is the brain of the program. Its function is to take user inputs that it receives from Floor and create algorithms that the elevators will follow to satisfy the requests of users. A notable method that it uses for communication is get(). get() serves to take modified information based off of the input and algorithm that is being executed to be used by elevator.

## Elevator.java
Elevator simply takes information from the scheduler using its get() method. The elevator also has lights and buttons to the elevator. When an elevator button is pressed, it will transition from one floor to another.

## Unit Tests
Unit tests are essentially used to test the code. This folder contains two files Test.java and ButtonLampTest.java

# Responsibilities [Make less verbose]
## Josh : 101109655
  
## Sanskar : 101189876
  
## Jakob : 101201314
  

## Sanya : 101205395
 

## Partha : 101191302
  

# Setup and Test Instructions
Download the `Elevator.java`, `Floor.java`, and `Scheduler.java` files and open them within a java project on eclipse
Run Eclipse from the `Scheduler.java` file to start the main program within that class
