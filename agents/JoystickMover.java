/**
 * @file move according to input from a joystick
 */
import net.java.games.input.*;

public class JoystickMover extends UserInputMover
{
	Component pad = null;
	Controller controller = null;
	
	public JoystickMover()
	{
		super();
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
		System.err.println("Using controller '" + joystick.getName() +"'");
		pad = joystick.getComponent(Component.Identifier.Axis.POV);
		controller = joystick;
	}

	protected void poll()
	{
		controller.poll();
	}

	protected Direction getData()
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
