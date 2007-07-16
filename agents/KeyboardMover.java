/**
 * @file move according to input from a keyboard
 */

public class KeyboardMover extends UserInputMover
{
	volatile char lastKey = 'n';
	
	protected void poll()
	{
		try
		{
			if(0 < System.in.available())
			{
				lastKey = (char)System.in.read();
			}
		}
		catch(Exception ex)
		{
		}
	}

	protected Direction getData()
	{
		Direction ret = Direction.NONE;
		switch(lastKey)
		{
			case 'w':
				ret = Direction.UP;
				break;
			case 's':
				ret = Direction.DOWN;
				break;
			case 'a':
				ret = Direction.LEFT;
				break;
			case 'd':
				ret = Direction.RIGHT;
				break;
			default:
				break;
		}
		return ret;
	}
}
