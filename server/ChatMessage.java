/**
 * @file Java doesn't have tuples.
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
