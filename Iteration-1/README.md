# ======= ITERATION 1 =======

## Files included:
- Floor.java
- elevator.java
- Scheduler.java
- floor_input.csv
- UML-diagrams

In-depth description about each file and their roles:

## UML-diagrams 
Folder containing two UML diagrams (one class and sequence diagram) describing the elevator system and their functions/methods. The class diagram shows the subsystems and their relations to other subsystems in a blue-print/schematic format. The sequence diagram on the other hand shows these subsystems in the context of their lifelines. 

## floor_input.csv
This is the file within which the user input is stored, polled and received from the rest of the subsystems. Its format is primarily in a table with columns for Time, Floor, Floor button and Car button. These provide key information that the rest of the subsystems need to schedule and execute. 

## Floor.java
This file represents a floor in the building within which the elevator functions. The primary task for the floor is to retrieve and send data from the floor_input file to provide updates for user input and current status to the scheduler. 

## Scheduler.java
The scheduler is the brain of the program. Its function is to take user inputs that it receives from Floor and create algorithms that the elevators will follow to satisfy the requests of users. A notable method that it uses for communication is get(). get() serves to take modified information based off of the input and algorithm that is being executed to be used by elevator.

## Elevator.java
Elevator simply takes information from the scheduler using its get() method. 

# Responsibilities
## Josh
  - Wrote the get() Function within the Scheduler. 
  - Wrote the csv_to_user() within Floor 

## Sanskar

## Jakob

## Sanya

## Partha
