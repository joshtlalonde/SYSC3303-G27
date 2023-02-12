# TODO
- We should be coming up with some states and how they interact by Monday.
  - That way we can have something to go off of once we're done midterms.
- This is my suggestion on how we should continue working in github
  - Create a Branch with your name
  - Make changes to the files as needed
  - Create a pull request when you are complete
  - Allow people to write notes on the pull request before it is agreed that it is good
    - Unit tests are a requirement so that should be run with any code that is written before pushed to main
  - Push to main 

# Format
## StateName 
### Events
  - What event makes the system enter this state
  - What event will occur to leave the state
### Actions (optional)
  - What action occurs at the entry of the state
  - What action occurs at the exit of the state
  - What action occurs at the transition to the next state
### Activities (optional)
  - What activities occur during the execution of this state

Try to come up with as many as possible (at least two)
- We need the states of the elevator and scheduler (ex: moving, request)
- And what actions/events occur between the states (ex: button click, doors closed)
