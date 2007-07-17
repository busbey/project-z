/**
 * @file move according to input from a keyboard
 */
import net.java.games.input.*;

public class KeyboardMover extends UserInputMover
{
	Keyboard keyboard = null;
	
	public KeyboardMover()
	{
		super();
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		/* find the first joystick or gamepad. */
		for(int i = 0; i < controllers.length; i++)
		{
			Controller.Type type = controllers[i].getType();
			if(Controller.Type.KEYBOARD == type )
			{
				keyboard = (Keyboard)controllers[i];
			}
		}

		if(null == keyboard)
		{
			throw new RuntimeException("No keyboard connected.");
		}
		System.err.println("Using controller '" + keyboard.getName() +"'");
	}

	protected void poll()
	{
		if(null != keyboard)
		{
			keyboard.poll();
		}
	}

	protected Direction getData()
	{
		Direction ret = Direction.NONE;
		if(null != keyboard)
		{
			if(keyboard.isKeyDown(Component.Identifier.Key.UP) ||
				keyboard.isKeyDown(Component.Identifier.Key.W))
			{
				ret = Direction.UP;
			}
			else if(keyboard.isKeyDown(Component.Identifier.Key.DOWN) ||
				keyboard.isKeyDown(Component.Identifier.Key.S))
			{
				ret = Direction.DOWN;
			}
			else if(keyboard.isKeyDown(Component.Identifier.Key.LEFT) ||
				keyboard.isKeyDown(Component.Identifier.Key.A))
			{
				ret = Direction.LEFT;
			}
			else if(keyboard.isKeyDown(Component.Identifier.Key.RIGHT) ||
				keyboard.isKeyDown(Component.Identifier.Key.D))
			{
				ret = Direction.RIGHT;
			}
		}
		return ret;
	}
}
