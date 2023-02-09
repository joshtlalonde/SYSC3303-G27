# ======= ITERATION 1 =======

## Files included:
- Floor.java
- elevator.java
- Scheduler.java
- floor_input.text
- UML-diagrams

In-depth description about each file and their roles:

## UML-diagrams 
Folder containing two UML diagrams (one class and sequence diagram) describing the elevator system and their functions/methods. The class diagram shows the subsystems and their relations to other subsystems in a blue-print/schematic format. The sequence diagram on the other hand shows these subsystems in the context of their lifelines. 

## floor_input.text
This is the file within which the user input is stored, polled and received from the rest of the subsystems. Its format is primarily in a table with columns for Time, Floor, Floor button and Car button. These provide key information that the rest of the subsystems need to schedule and execute. 

## Floor.java
This file represents a floor in the building within which the elevator functions. The primary task for the floor is to retrieve and send data from the floor_input file to provide updates for user input and current status to the scheduler. 

## Scheduler.java
The scheduler is the brain of the program. Its function is to take user inputs that it receives from Floor and create algorithms that the elevators will follow to satisfy the requests of users. A notable method that it uses for communication is get(). get() serves to take modified information based off of the input and algorithm that is being executed to be used by elevator.

## Elevator.java
Elevator simply takes information from the scheduler using its get() method. 

# Responsibilities [Make less verbose]
## Josh : 101109655
  - Wrote the get() Function within the Scheduler. 
  - Wrote the file_to_user() within Floor 
  - Worked on ensuring the integration of Floor and Elevator into the Scheduler subsystem

## Sanskar : 101189876
  - Wrote the Elevator class and file
  - Made and completed the Readme file
  - Tested program files
## Jakob : 101201314
  - Wrote the put() function within the Scheduler. 
  - Wrote the main function within the Scheduler.
  - Debugged and tested files.

## Sanya : 101205395
  - Created the class diagram.
  - Created the sequence diagram.
  - Tested the files.

## Partha : 101191302
  - Wrote the Floor class
  - Wrote the User_input class

# Setup and Test Instructions
Download the `Elevator.java`, `Floor.java`, and `Scheduler.java` files and open them within a java project on eclipse
Run Eclipse from the `Scheduler.java` file to start the main program within that class
