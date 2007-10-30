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

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Game
{
	public static final String[] launchBackgroundBug = {"./runbug.sh"};
	public static void usage()
	{
		System.err.println("usage: java Game stationName path/to/image");
	}

	public static void main (String[] args)
	{
		if(4 > args.length)
		{
			usage();
			return;
		}
		String hostname = args[2];
		int port = Integer.parseInt(args[3]);
		JFrame mainWindow = new JFrame(args[0]);
		ImageIcon bug = new ImageIcon(args[1]);
		mainWindow.setBackground(Color.BLACK);
		bug.setImage(bug.getImage().getScaledInstance(800, -1, Image.SCALE_SMOOTH));
		JLabel label = new JLabel("Push Start", bug, SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.TOP);
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
		
		mainWindow.add(label);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = null;
		try
		{
			if(! ge.isHeadless())
			{
				gd = ge.getDefaultScreenDevice();
				gd.setFullScreenWindow(mainWindow);
			}
			PassthroughJoystickAgent joystick = new PassthroughJoystickAgent();
			try
			{
				Runtime.getRuntime().exec(launchBackgroundBug);
			}
			catch(IOException iex)
			{
			}
			joystick.init(null);

			if (!joystick.openConnection(hostname, port)) 
			{
				return;
			}
			joystick.runAgent();
		}
		finally
		{
			if(null != gd)
			{
				gd.setFullScreenWindow(null);
			}
		}
		return;	
	}
}
