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
import java.awt.event.*;
import javax.swing.event.*;

public class ServerUI extends JFrame
{
	protected final World world;

	protected final String[] bugStormCommand = {"./bugstorm.sh", "../agents"};
	protected final String[] blockerCommand = {"./blockers.sh","../agents/openBug"};
	
	public ServerUI(World passedworld)
	{
		super("Server Controls");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(0,1));
		this.world = passedworld;

		if(world instanceof RotatingWorld)
		{
			/* Map Changing */
			final RotatingWorld rotWorld = (RotatingWorld)world;
			JPanel mapChange = new JPanel(new BorderLayout());
			final JCheckBox enableChange= new JCheckBox("<html><h1>Rotate Maps</h1></html>");
			enableChange.setMnemonic(KeyEvent.VK_R);
			if(rotWorld.isRotating())
			{
				enableChange.setSelected(true);
			}
			mapChange.add(enableChange, BorderLayout.NORTH);
			JPanel changeOptions = new JPanel(new GridLayout(0,1));
			final JCheckBox resetMap = new JCheckBox("Clear game when rotating");
			if(rotWorld.willReset())
			{
				resetMap.setSelected(true);
			}
			changeOptions.add(resetMap);
			changeOptions.add(new JLabel("Rotate maps every (min):"));
			final JSlider changeSpeed = new JSlider(JSlider.HORIZONTAL, 0,20,rotWorld.changeEvery()/60);
			changeSpeed.setSnapToTicks(true);
			changeSpeed.setPaintTicks(true);
			changeSpeed.setPaintLabels(true);
			changeOptions.add(changeSpeed);
			mapChange.add(changeOptions,BorderLayout.EAST);

			/* add Listeners */
			enableChange.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if(ItemEvent.DESELECTED == e.getStateChange())
					{
						rotWorld.stopRotating();
					}
					else if (ItemEvent.SELECTED == e.getStateChange())
					{
						int changeEvery;
						while(changeSpeed.getValueIsAdjusting())
						{
							// wait till they finish.
						}
						changeEvery = 60 * changeSpeed.getValue();
						boolean reset = resetMap.isSelected();
						rotWorld.rotate(changeEvery, reset);
					}
					
				}
			});
			resetMap.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if(enableChange.isSelected())
					{
						boolean reset = (ItemEvent.SELECTED == e.getStateChange());
						int changeEvery;
						while(changeSpeed.getValueIsAdjusting())
						{
							// wait till they finish.
						}
						changeEvery = 60 * changeSpeed.getValue();
						rotWorld.rotate(changeEvery, reset);
					}
					
				}
			});
			changeSpeed.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e) 
				{
    				if (enableChange.isSelected() &&
						!changeSpeed.getValueIsAdjusting()) 
					{
						boolean reset = resetMap.isSelected();
						int changeEvery = 60 * changeSpeed.getValue();
						rotWorld.rotate(changeEvery, reset);
			        }
    			}
			});
		
			JPanel gotoMap = new JPanel(new GridLayout(0,1));
			gotoMap.add(new JLabel("Jump to map:"));
			final JList maps = new JList(rotWorld.getMaps());
			maps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			maps.setSelectedIndex(rotWorld.curBoard());
			maps.ensureIndexIsVisible(rotWorld.curBoard());
			maps.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e)
				{
					if(!e.getValueIsAdjusting())
					{
						int board = maps.getSelectedIndex();
						boolean reset = resetMap.isSelected();
						if(-1 != board)
						{
							try
							{
								rotWorld.loadBoard(board, reset);
							}
							catch(IOException iox)
							{
								iox.printStackTrace();
								System.exit(-1);
							}
						}
					}
				}
			});

			gotoMap.add(new JScrollPane(maps));
			mapChange.add(gotoMap,BorderLayout.WEST);
			
			add(mapChange);
		}
		/* Map Animation */
		JPanel mapAnimate = new JPanel(new BorderLayout());
		JCheckBox enableAnimate = new JCheckBox("<html><h1>Map Animation</h1></html>");
		enableAnimate.setSelected(world.getAnimate());

		/* add Listeners */
		enableAnimate.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				world.setAnimate(ItemEvent.SELECTED == e.getStateChange());
			}
		});
		
		mapAnimate.add(enableAnimate, BorderLayout.NORTH);
		JPanel animateOptions = new JPanel(new GridLayout(0,1));
		animateOptions.add(new JLabel("Advance animation every (rounds):"));
		final JSlider changeAnimate = new JSlider(JSlider.HORIZONTAL, 1,120,world.ROUNDS_PER_FRAME);
		changeAnimate.setSnapToTicks(true);
		changeAnimate.setPaintTicks(true);
		changeAnimate.setPaintLabels(true);

		changeAnimate.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e) 
			{
				if(!changeAnimate.getValueIsAdjusting())
				{
					world.ROUNDS_PER_FRAME = changeAnimate.getValue();
				}
    		}
		});
	
		
		animateOptions.add(changeAnimate);
		mapAnimate.add(animateOptions, BorderLayout.CENTER);
		add(mapAnimate);
		/* Fog Of War */
		JPanel fogOfWar = new JPanel(new BorderLayout());
		final JCheckBox enableFogOfWar = new JCheckBox("<html><h1>Fog Of War</h1></html>");
		WorldFilter filter = world.getFilter();
		enableFogOfWar.setSelected(filter instanceof FixedRadiusFilter);

		fogOfWar.add(enableFogOfWar, BorderLayout.NORTH);


		JPanel fogOptions = new JPanel(new GridLayout(0,1));
		final JCheckBox flexFog = new JCheckBox("Change visibility with movement");
		flexFog.setSelected(filter instanceof InverseMoveRadiusFilter);
		fogOptions.add(flexFog);
		fogOptions.add(new JLabel("Base radius (tiles):"));
		int startingRadius = 5;
		if(filter instanceof FixedRadiusFilter)
		{
			startingRadius = ((FixedRadiusFilter)filter).getRadius();
		}
		final JSlider changeFog = new JSlider(JSlider.HORIZONTAL, 5,50,startingRadius);
		changeFog.setSnapToTicks(true);
		changeFog.setPaintTicks(true);
		changeFog.setPaintLabels(true);
		fogOptions.add(changeFog);
		fogOfWar.add(fogOptions,BorderLayout.CENTER);
		
		/* add Listeners */
		enableFogOfWar.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if(ItemEvent.DESELECTED == e.getStateChange())
				{
					world.clearFilter();
				}
				else if(ItemEvent.SELECTED == e.getStateChange())
				{
					int radius;
					while(changeFog.getValueIsAdjusting())
					{
						//wait for them to finish.
					}
					radius = changeFog.getValue();
					if(flexFog.isSelected())
					{
						world.setFilter(new InverseMoveRadiusFilter(radius, InverseMoveRadiusFilter.MANHATTAN_DISTANCE, 1, 2, 2, 1));
					}
					else
					{
						world.setFilter(new FixedRadiusFilter(radius, FixedRadiusFilter.MANHATTAN_DISTANCE));
					}
				}
			}
		});
		flexFog.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if(enableFogOfWar.isSelected())
				{
					int radius;
					while(changeFog.getValueIsAdjusting())
					{
						//wait for them to finish.
					}
					radius = changeFog.getValue();
					
					if(e.SELECTED == e.getStateChange())
					{
						world.setFilter(new InverseMoveRadiusFilter(radius, InverseMoveRadiusFilter.MANHATTAN_DISTANCE, 1, 2, 2, 1));
					}
					else
					{
						world.setFilter(new FixedRadiusFilter(radius, FixedRadiusFilter.MANHATTAN_DISTANCE));
					}
				}
			}
		});
		changeFog.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e) 
			{
    			if (enableFogOfWar.isSelected() &&
					!changeFog.getValueIsAdjusting()) 
				{
					int radius = changeFog.getValue();
					if(flexFog.isSelected())
					{
						world.setFilter(new InverseMoveRadiusFilter(radius, InverseMoveRadiusFilter.MANHATTAN_DISTANCE, 1, 2, 2, 1));
					}
					else
					{
						world.setFilter(new FixedRadiusFilter(radius, FixedRadiusFilter.MANHATTAN_DISTANCE));
					}
			    }
    		}
		});


		
		add(fogOfWar);
		/* Agent Invocation */
		JPanel agents = new JPanel(new BorderLayout());
		agents.add(new JLabel("<html><h1>Grief</h1></html>"), BorderLayout.NORTH);
		JPanel buttons = new JPanel(new GridLayout(1,0));
		JButton bugStorm = new JButton(new ImageIcon("../dependencies/media/cute/Enemy Bug Purple.png"));
		bugStorm.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.err.println( "Initiating Bug Storm..." );
				try
				{
					Runtime.getRuntime().exec(bugStormCommand);
				}
				catch(Exception ex)
				{
					System.err.println("Error running bug storm: " + ex.getMessage());
				}
			}
		});
		JButton outsider = new JButton(new ImageIcon("../dependencies/media/cute/Character Pink Boy.png"));
		outsider.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.err.println( "Summoning Blockers..." );
				try
				{
					Runtime.getRuntime().exec(blockerCommand);
				}
				catch(Exception ex)
				{
					System.err.println("Error summonging blockers: " + ex.getMessage());
				}
			}
		});
		buttons.add(bugStorm);
		buttons.add(outsider);
		agents.add(buttons, BorderLayout.CENTER);
		add(agents);
		pack();
		setVisible(true);
	}
}
