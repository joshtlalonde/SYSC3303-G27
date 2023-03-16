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

Note Floor fault in real life is a door failure (Fails to open or close) (Door stuck. Cheat on your man homie AAGH I tried to sneak through the door man! Can't make it. Can't make it. Shit's stuck. Outta my way son! DOOR STUCK! DOOR STUCK! PLEASE! I BEG YOU! We're dead. You're a genuine dick sucker.)
