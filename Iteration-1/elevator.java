//import scheduler.java

//Program for Elevator (consumer)
class elevator implements Runnable
{
    private User_input user_input;
    private boolean goingUp;
    private Scheduler scheduler;
    
    public elevator(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
    public void run()
    {
        while(true){
            user_input = scheduler.get();
            system.out.println("Retrieving next path from Scheduler");

        }
    }
}

