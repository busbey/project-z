/**
 * @file move an agent based on input from the user.
 */
public abstract class UserInputMover implements Mover
{
	public final static long POLL_WAIT = 5;

	protected abstract void poll();
	protected abstract Direction getData();

	public UserInputMover()
	{
	    Thread poll = new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
					poll();
					try
					{
						Thread.sleep(POLL_WAIT);
					}
					catch(Exception ex)
					{
					}
				}
			}
		} ,"Polling");	
		poll.setDaemon(true);
		poll.start();
	}

    public Direction respondToChange (State newState)
	{
		return getData();
	}
}
