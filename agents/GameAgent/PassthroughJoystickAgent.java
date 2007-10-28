/**
 * @file move according to input from a joystick or from a connected agent
 * toggle with start button.
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
import java.util.*;
import java.net.*;
import java.io.*;

public class PassthroughJoystickAgent extends JoystickAgent
{
	public static final int PASSTHROUGH_PORT = 10001;
	protected volatile boolean useAgent = true;
	protected volatile boolean agentRunning = true;
	protected float lastData = 0f;
	Component startButton;

	final ArrayList<byte[]> states = new ArrayList<byte[]>();
	
	public PassthroughJoystickAgent()
	{
		super();
		System.err.println("Setting up passthrough joystick agent");
		startButton = controller.getComponent(Component.Identifier.Button._9);
		/* Start a thread to listen for agents */
		Thread listener = new Thread(new Runnable()
		{
			public void run()
			{
				System.err.println("Setting up listening on port " + PASSTHROUGH_PORT + "...");
				try
				{
				ServerSocket listening = new ServerSocket(PASSTHROUGH_PORT);
				/* we'll be marked as a daemon, so we'll exit
				 	when runAgent returns.
				 */
				while(true)
				{
					Socket client = listening.accept();
					agentRunning = true;
					System.err.println("Setting up new passthrough...");
					try
					{
						final OutputStream outStream  = client.getOutputStream();
						Thread stateOut = new Thread(new Runnable()
						{
							public void run()
							{
								System.err.println("Ready to write state out to server...");
								while(agentRunning)
								{
									try
									{
										if(0 < states.size())
                        				{
                            				byte[] state;
                            				/* we're doing this manually rather than iterating over the list because we need the critical section
                                				to be small and non-blocking so the rest of the server can't get hung up by a non-responsive client
                            				 */
                            				synchronized(states)
                            				{
                                				state = states.remove(0);
                            				}
                            				outStream.write(state);
                            				outStream.flush();
                        				}
                        				else
                        				{
                            				Thread.yield();
                            				try
                            				{
                                				Thread.sleep(50);
                            				}
                            				catch(InterruptedException iex)
                            				{
                            				}
                        				}

									}
									catch(Exception ex)
									{
										ex.printStackTrace();
										agentRunning = false;
									}
								}
							}
						});

						stateOut.setDaemon(true);
						stateOut.start();
						DataInputStream inStream = new DataInputStream(client.getInputStream());
						System.err.println("Ready to start passing moves back.");
						while(agentRunning)
						{
							byte firstByte = inStream.readByte();
							/* XXX I apologize.  Java is not my friend for parsing and validating single characters. */
                        	switch(firstByte)
                        	{
                            	case 'l':
                            	case 'r':
                            	case 'u':
                            	case 'd':
                            	case 'n':
                                	/* 'i move' case */
									if(useAgent)
									{
										writeMove(Direction.lookup(firstByte));
									}
                                	break;
                            	default:
                                	/* 
                                    	'i say' case 
                                 	*/
                                    byte speaker = firstByte;
                                    byte subject = inStream.readByte();
                                    byte action = inStream.readByte();
									if(useAgent)
									{
										sendMessage(speaker,subject,Direction.lookup(action));
									}
                                	break;
							}
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						agentRunning = false;
					}
				}
				} 
				catch(IOException iex)
				{
					throw new RuntimeException("Couldn't set up ability to listen for agents", iex);
				}
			}
		});

		listener.setDaemon(true);
		listener.start();
	}

	protected void poll()
	{
		super.poll();
		if(null != startButton)
		{
			final float start = startButton.getPollData();
			if(start != lastData)
			{
				if(startButton.getDeadZone() < start)
				{
					useAgent = !useAgent;
				}
				lastData = start;
			}
		}
	}

	public void setUseAgent(boolean use)
	{
		useAgent = use;
	}

	public void endHuman()
	{
		setUseAgent(true);
	}

	public boolean isHuman()
	{
		return !useAgent;
	}

	public void respondToChange()
	{
		/* pass state through to client */
		byte[] serialized = SerializableState.serialize(state);
		synchronized(states)
		{
			states.add(serialized);
		}
		if(!useAgent)
		{
			super.respondToChange();
		}
	}
}
