public enum Direction {
	
	NONE ('n'), 
		UP ('u'), 
		DOWN ('d'), 
		LEFT ('l'), 
		RIGHT ('r');
	
	private final byte direction;
  
	Direction (char direction) {
		this.direction = (byte) direction;
	}

	public static Direction lookup (byte toLookup) {
		switch ((char) toLookup) {
		case 'n':
			return Direction.NONE;
		case 'u':
			return Direction.UP;
		case 'd':
			return Direction.DOWN;
		case 'l':
			return Direction.LEFT;
		case 'r':
			return Direction.RIGHT;
		}
		return Direction.NONE;
	}

	public byte getByte () { return direction; }
	
}

