/**
 * @file move according to input from a keyboard
 */
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
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
