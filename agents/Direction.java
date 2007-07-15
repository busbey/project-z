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
    
    public byte getByte () { return direction; }

}

