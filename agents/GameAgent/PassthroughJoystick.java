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

public class PassthroughJoystick implements Runnable
{
	protected volatile boolean useAgent = true;
	protected volatile boolean agentRunning = false;

	/* producer/consumer queues for each direction */
	final ArrayList<byte[]> states = new ArrayList<byte[]>();
	final ArrayList<byte[]> actions = new ArrayList<byte[]>();
	
	/* parts of the joystick we need. */
	Component startButton = null;
	Component pad = null;
	Controller controller = null;
	/* if we poll too often, the network won't be able to handle things 
		this is nanos.

		XXX rounds are 250 ms, so poll 2x.
	*/
	protected final long POLL_WAIT = 5*10000000;

	/* networking info */
	String hostname = null;
	int	port = 0;
	ServerSocket listening = null;
	
	public PassthroughJoystick(String host, int port, int listenPort) throws IOException
	{
		/* set up the first joystick or gamepad. */
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller joystick = null;
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
		startButton = joystick.getComponent(Component.Identifier.Button._9);
		controller = joystick;

		/* save off the network info. */
		hostname = host;
		this.port = port;
		/* set up our listening to a socket. */
		listening = new ServerSocket(listenPort);
	}

	public volatile boolean running = true;

	public void run()
	{
		try
		{
			/* set up our connections to the server */
			Socket server = new Socket(hostname, port);
			final DataOutputStream serverOut = new DataOutputStream(server.getOutputStream());
			final DataInputStream serverIn = new DataInputStream(server.getInputStream());

			/* spawn a thread to fill the state queue 
					XXX only parse enough to know that we're reading the
					correct number of bytes.
			 */
			Thread stateReader = new Thread(new Runnable()
			{
				public void run()
				{
					while(running)
					{
						try
						{
							// XXX start with minimum size and realloc when we have a better idea.
							byte[] state = new byte[1 + 1 + 4 + 4 + 0*0 + 4 + 0*3];
							int offset = 0;
							/* read off a complete state */
							state[0] = serverIn.readByte();
							if((byte)0xFF==state[0])
							{
								System.err.println("Game has ended...");
								running = false;
								continue;
							}
							state[1] = serverIn.readByte();
							offset=2;
							final int cols = serverIn.readInt();
							final int rows = serverIn.readInt();
							byte[] tmp = new byte[1+1+4+4+cols*rows+4+0*3];
							System.arraycopy(state, 0, tmp, 0, offset);
							state=tmp;
							tmp=null;
							// now encode the number of cols and rows. 
							state[offset + 0] = (byte)((0xFF000000 & cols) >>> 24);
							state[offset + 1] = (byte)((0x00FF0000 & cols) >>> 16);
							state[offset + 2] = (byte)((0x0000FF00 & cols) >>> 8);
							state[offset + 3] = (byte)(0x000000FF & cols);
							state[offset + 4] = (byte)((0xFF000000 & rows) >>> 24);
							state[offset + 5] = (byte)((0x00FF0000 & rows) >>> 16);
							state[offset + 6] = (byte)((0x0000FF00 & rows) >>> 8);
							state[offset + 7] = (byte)(0x000000FF & rows);
							offset+=8;
							// and pull off the board
							serverIn.readFully(state, offset, cols*rows);
							offset+=cols*rows;
							final int chats = serverIn.readInt();
							tmp = new byte[1+1+4+4+cols*rows+4+chats*3];
							System.arraycopy(state,0,tmp,0,offset);
							state=tmp;
							tmp=null;
							//now encode the number of chats
							state[offset + 0] = (byte)((0xFF000000 & chats) >>> 24);
							state[offset + 1] = (byte)((0x00FF0000 & chats) >>> 16);
							state[offset + 2] = (byte)((0x0000FF00 & chats) >>> 8);
							state[offset + 3] = (byte)(0x000000FF & chats);
							offset+=4;
							// and pull off the chats.
							serverIn.readFully(state,offset,chats*3);
							offset+=chats*3;
							synchronized(states)
							{
								/* If no agent is connected, only keep the last one. */
								if(!agentRunning)
								{
									states.clear();
								}
								states.add(state);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							running =false;
						}
					}
				}
			}, "State Reader");

			stateReader.setDaemon(true);
			stateReader.start();
			
			/* spawn a thread to empty the action queue */
			Thread actionSender = new Thread(new Runnable()
			{
				public void run()
				{
					while(running)
					{
						try
						{
							if(0 < actions.size())
							{
								byte[] action;
								/* we're doing this manually rather than iterating over the list because we need the critical section
					   				to be small and non-blocking so the rest of the server can't get hung up by a non-responsive client
								 */
								synchronized(actions)
								{
									action = actions.remove(0);
								}
								serverOut.write(action);
								serverOut.flush();
							}
							else
							{
								Thread.yield();
								try
								{
									Thread.sleep(POLL_WAIT/1000000);
								}
								catch(InterruptedException iex)
								{
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							running = false;
						}
					}
				}
			}, "Move Sender");
			actionSender.setDaemon(true);
			actionSender.start();

			/* spawn a thread to poll the joystick and act as appropriate */
	    	Thread poll = new Thread(new Runnable()
			{
				public void run()
				{
					long start;
					long end;
					float lastData = -1.0f;
					while(running)
					{
						start = System.nanoTime();
						controller.poll();
						/* always need to check for the start button */
						if(null != startButton)
						{
							final float button = startButton.getPollData();
							if(button != lastData)
							{
								if(startButton.getDeadZone() < button)
								{
									useAgent = !useAgent;
									System.err.println(useAgent ? agentRunning ? "Forwarding moves from agent." : "Waiting for agent." : "Taking moves from Joystick.");
								}
								lastData = button;
							}
						}
						/* moves? only if we're active.  */
						if(!useAgent)
						{
							if(null != pad)
							{
								float data = pad.getPollData();
								byte move = (byte)'n';
								if(Component.POV.UP == data)
								{
									move = (byte)'u';
								}
								else if(Component.POV.DOWN == data)
								{
									move = (byte)'d';
								}
								else if(Component.POV.LEFT == data)
								{
									move = (byte)'l';
								}
								else if(Component.POV.RIGHT == data)
								{
									move = (byte)'r';
								}
								synchronized(actions)
								{
									actions.add(new byte[]{move});
								}
							}
						}
						end = System.nanoTime();
						final long elapsed = end - start;
						if(elapsed < POLL_WAIT)
						{
							try
							{
								Thread.sleep((POLL_WAIT - elapsed)/1000000);
							}
							catch(Exception ex)
							{
							}
						}
					}
				}
			} ,"Polling");	
			poll.setDaemon(true);
			poll.start();

			/* Listen for connections and set up service for bugs */
			while(running)
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
									System.err.println("Ready to write state out to bug...");
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
													Thread.sleep(POLL_WAIT/1000000);
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
									/* XXX I apologize.  Java is not my friend for parsing and validating single characters. 
									 */
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
														synchronized(actions)
														{
															actions.add(new byte[]{firstByte});
														}
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
														synchronized(actions)
														{
															actions.add(new byte[]{speaker,subject,action});
														}
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
		catch(Exception e)
		{
			e.printStackTrace();
			running = false;
		}
	}


	public static void main(String[] args) throws IOException
	{
		PassthroughJoystick pj = new PassthroughJoystick(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
		Thread pjThread = new Thread(pj);
		pjThread.setDaemon(true);
		pjThread.start();
		System.out.println("Press any key to exit...");
		System.in.read();
	}
}
