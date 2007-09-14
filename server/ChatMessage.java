/**
 * @file Java doesn't have tuples.
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

public class ChatMessage
{
	byte speaker;
	byte subject;
	byte move;
	public ChatMessage(byte speaker, byte subject, byte move)
	{
		this.speaker=speaker;
		this.subject=subject;
		this.move=move;
	}
	public void serialize(java.io.DataOutputStream out) throws java.io.IOException
	{
		out.writeByte(speaker);
		out.writeByte(subject);
		out.writeByte(move);
	}
}
