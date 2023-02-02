//Program for Elevator (consumer)
class Elevator implements Runnable
{
    private int floor;
    private boolean directionUp;
    private Scheduler scheduler;
    
    public Elevator(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
    
    public void run()
    {
        while(true){
            System.out.println(Thread.currentThread().getName() + " is ready on floor " + floor);
            User_input user_input = scheduler.get();
            
            System.out.println(Thread.currentThread().getName() + " is moving to floor " + user_input.getFloor() + " to pick up user");
            // Sleep for travel time
            try {
	            Thread.sleep(1000); 
	        } catch (InterruptedException e) {}
            
            System.out.println(Thread.currentThread().getName() + " is moving user to floor " + user_input.getCar_button() + " to drop off user");
            // Sleep for travel time
            try {
	            Thread.sleep(1000); 
	        } catch (InterruptedException e) {}
            
            // Set the floor elevator is on
            floor = user_input.getCar_button();
            // Set direction elevator is moving
            directionUp = user_input.getFloor_button();

            // Sleep for 1 second
            try {
	            Thread.sleep(1000); 
	        } catch (InterruptedException e) {}
        }
    }
}
