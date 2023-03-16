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
