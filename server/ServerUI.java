/**
 * @file server up world state and change it based on agent feedback
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

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class ServerUI extends JFrame
{
	protected final World world;
	public ServerUI(World world)
	{
		super("Server Controls");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(0,1));
		this.world = world;

		if(world instanceof RotatingWorld)
		{
			/* Map Changing */
			JPanel mapChange = new JPanel(new BorderLayout());
			JCheckBox enableChange= new JCheckBox("<html><h1>Rotate Maps</h1></html>");
			mapChange.add(enableChange, BorderLayout.NORTH);
			JPanel changeOptions = new JPanel(new GridLayout(0,1));
			JCheckBox resetMap = new JCheckBox("Clear game when rotating");
			changeOptions.add(resetMap);
			changeOptions.add(new JLabel("Rotate maps every (min):"));
			JSlider changeSpeed = new JSlider(JSlider.HORIZONTAL, 1,20,10);
			changeSpeed.setSnapToTicks(true);
			changeSpeed.setPaintTicks(true);
			changeSpeed.setPaintLabels(true);
			changeOptions.add(changeSpeed);
			mapChange.add(changeOptions,BorderLayout.EAST);
		
			JPanel gotoMap = new JPanel(new GridLayout(0,1));
			gotoMap.add(new JLabel("Jump to map:"));
			RotatingWorld rotWorld = (RotatingWorld)world;
			JList maps = new JList(rotWorld.getMaps());
			gotoMap.add(new JScrollPane(maps));
			mapChange.add(gotoMap,BorderLayout.WEST);
			
			add(mapChange);
		}
		/* Map Animation */
		JPanel mapAnimate = new JPanel(new BorderLayout());
		JCheckBox enableAnimate = new JCheckBox("<html><h1>Map Animation</h1></html>");
		mapAnimate.add(enableAnimate, BorderLayout.NORTH);
		JPanel animateOptions = new JPanel(new GridLayout(0,1));
		animateOptions.add(new JLabel("Advance animation every (rounds):"));
		JSlider changeAnimate = new JSlider(JSlider.HORIZONTAL, 1,120,5);
		changeAnimate.setSnapToTicks(true);
		changeAnimate.setPaintTicks(true);
		animateOptions.add(changeAnimate);
		mapAnimate.add(animateOptions, BorderLayout.CENTER);
		add(mapAnimate);
		/* Fog Of War */
		JPanel fogOfWar = new JPanel(new BorderLayout());
		JCheckBox enableFogOfWar = new JCheckBox("<html><h1>Fog Of War</h1></html>");
		fogOfWar.add(enableFogOfWar, BorderLayout.NORTH);
		add(fogOfWar);


		JPanel fogOptions = new JPanel(new GridLayout(0,1));
		JCheckBox flexFog = new JCheckBox("Change visibility with movement");
		fogOptions.add(flexFog);
		fogOptions.add(new JLabel("Base radius (tiles):"));
		JSlider changeFog = new JSlider(JSlider.HORIZONTAL, 5,50,10);
		changeFog.setSnapToTicks(true);
		changeFog.setPaintTicks(true);
		fogOptions.add(changeFog);
		fogOfWar.add(fogOptions,BorderLayout.CENTER);
		
		/* Agent Invocation */
		JPanel agents = new JPanel(new BorderLayout());
		agents.add(new JLabel("<html><h1>Grief</h1></html>"), BorderLayout.NORTH);
		JPanel buttons = new JPanel(new GridLayout(1,0));
		JButton bugStorm = new JButton(new ImageIcon("../dependencies/media/cute/Enemy Bug Purple.png"));
		JButton outsider = new JButton(new ImageIcon("../dependencies/media/cute/Character Boy.png"));
		buttons.add(bugStorm);
		buttons.add(outsider);
		agents.add(buttons, BorderLayout.CENTER);
		add(agents);
		pack();
		setVisible(true);
	}
}
