import net.java.games.input.*;

public class JoystickMover implements Mover
{
	public final static long POLL_WAIT = 5;

	Component pad = null;
	
	public JoystickMover(String[] args)
	{
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller joystick = null;
		/* find the first joystick or gamepad. */
		for(int i = 0; i < controllers.length; i++)
		{
			Controller.Type type = controllers[i].getType();
			if(Controller.Type.GAMEPAD == type || Controller.Type.STICK == type)
			{
				joystick = controllers[i];
			}
		}

		if(null == joystick)
		{
			throw new RuntimeException("No controllers connected.");
		}
		final Controller controller = joystick;
		System.err.println("Using controller '" + controller.getName() +"'");
		pad = controller.getComponent(Component.Identifier.Axis.POV);
	    Thread poll = new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
					controller.poll();
					try
					{
						Thread.sleep(POLL_WAIT);
					}
					catch(Exception ex)
					{
					}
				}
			}
		} ,"Controller Polling");	
		poll.setDaemon(true);
		poll.start();
	}

    public Direction respondToChange (State newState)
	{
		Direction ret = Direction.NONE;
		float data = pad.getPollData();
		if(Component.POV.UP == data)
		{
			ret = Direction.UP;
		}
		else if(Component.POV.DOWN == data)
		{
			ret = Direction.DOWN;
		}
		else if(Component.POV.LEFT == data)
		{
			ret = Direction.LEFT;
		}
		else if(Component.POV.RIGHT == data)
		{
			ret = Direction.RIGHT;
		}
		return ret;
	}
}
