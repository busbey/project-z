/**
 * @file move according to input from a joystick
 */
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
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

public class JoystickAgent extends UserInputAgent
{
	Component pad = null;
	Controller controller = null;
	
	public JoystickAgent()
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
			System.err.println("library path: " + System.getProperty("java.library.path", "none"));
			throw new RuntimeException("No controllers connected.");
		}
		String name = joystick.getName();
		System.err.println("Using controller '" + name +"'");
		pad = joystick.getComponent(Component.Identifier.Axis.POV);
		controller = joystick;
	}

	protected void poll()
	{
		if(null != controller)
		{
			controller.poll();
		}
	}

	protected Direction getData()
	{
		Direction ret = Direction.NONE;
		if(null != pad)
		{
			float data = pad.getPollData();
			System.err.println("polled data: " + data);
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
		}
		return ret;
	}
}
