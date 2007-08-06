public class Message {
	
	private byte speaker;
	private byte subject;
	public Direction direction;

	public Message (byte speaker, byte subject, Direction direction) {
		this.speaker = speaker;
		this.subject = subject;
		this.direction = direction;
	}

	public byte getSpeaker () { return speaker; }
	public byte getSubject () { return subject; }
	public Direction getDirection () { return direction; }

}