/**
 * @file move an agent based on input from the user.
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
