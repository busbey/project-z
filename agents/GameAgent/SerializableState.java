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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SerializableState extends State
{
	public SerializableState (byte player, int rows, int columns) 
	{
		super(player,rows,columns);
	}
	public byte[] serialize()
	{
		return SerializableState.serialize(this);
	}
	public static byte[] serialize(State state)
	{
		byte[] out = new byte[1 + 1 + 4 + 4 + state.rows*state.columns + 4 + 3*state.messages.size()];
		int offset = 0;
		out[offset] =(byte)0;
		if(state.killerBug)
		{
			out[offset] |= KILLER_BUG;
		}
		if(state.wasKilled)
		{
			out[offset] |= WAS_KILLED;
		}
		if(state.wasStunned)
		{
			out[offset] |= WAS_STUNNED;
		}
		if(state.killedSomeone)
		{
			out[offset] |= KILLED_SOMEONE;
		}
		offset++;
		out[offset] = (byte) state.player;
		offset++;
        out[offset + 0] = (byte)((0xFF000000 & state.columns) >>> 24);
        out[offset + 1] = (byte)((0x00FF0000 & state.columns) >>> 16);
        out[offset + 2] = (byte)((0x0000FF00 & state.columns) >>> 8);
        out[offset + 3] = (byte)(0x000000FF & state.columns);
        out[offset + 4] = (byte)((0xFF000000 & state.rows) >>> 24);
        out[offset + 5] = (byte)((0x00FF0000 & state.rows) >>> 16);
        out[offset + 6] = (byte)((0x0000FF00 & state.rows) >>> 8);
        out[offset + 7] = (byte)(0x000000FF & state.rows);
		offset+=8;
		for(int i = 0; i < state.rows; i++)
		{
			for(int j = 0; j < state.columns; j++,offset++)
			{
				out[offset] = state.board[i][j];
			}
		}
        out[offset+0] = (byte)((0xFF000000 & state.messages.size()) >>> 24);
        out[offset+1] = (byte)((0x00FF0000 & state.messages.size()) >>> 16);
        out[offset+2] = (byte)((0x0000FF00 & state.messages.size()) >>> 8);
        out[offset+3] = (byte)(0x000000FF & state.messages.size());
		offset+=4;
		for(Message message : state.messages)
		{
			out[offset+0] = message.speaker;
			out[offset+1] = message.subject;
			out[offset+2] = message.direction.getByte();
			offset+=3;
		}

		return out;
	}
}
