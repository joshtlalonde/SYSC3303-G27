# Init


TODO:
Right now if all elevators are moving and someone requests to travel in a direction that none of the elevators are
moving we have an issue. Two possiblities, when elevators are moving check which one finishes closest to the request
that one will move to the passenger. Or Which one has the smallest amount of requests in their elevator queue. 
This will likely be the elevator that has the least amount of distance to cover. 

TODO:
Maybe make Idle and Stopped the same State -> Called Idle

TODO:
Change scheduler state machine to add floor lights

TODO:
Add Faults, Elevator Stuck (Permant fault (Loose an elevator)) and Floor fault (Transient fault (Temperary fault)). Put two transient faults, one permenet 
(I can't spell I am so tired) 
There will be 5th column made in the Floor txt file to show when a fault occurs
We will add in 2 different Floor faults around FloorRequest 5 and 10.
Then the elevator Fault should occur around FloorRequest 15 then 5 more packets after that

Note Floor fault in real life is a door failure (Fails to open or close) (Door stuck. Cheat on your man homie AAGH I tried to sneak through the door man! Can't make it. Can't make it. Shit's stuck. Outta my way son! DOOR STUCK! DOOR STUCK! PLEASE! I BEG YOU! We're dead. You're a genuine dick sucker.)

# Changes to Code
Still need to finish the states in the elevator
Scheduler needs to be programmed to decide which elevators get told to pick up a user. Like, if there is a person in between the floors that an elevator is moving between then that one will not get sent to an elevator yet. If there is no elevator then send an elevator to idle.

Add in a PORT for each of the Elevators...
