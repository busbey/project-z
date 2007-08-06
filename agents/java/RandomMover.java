import java.util.Random;

public class RandomMover implements Mover {
	
	private Random random;
	
	public RandomMover () {
		random = new Random();
	}
	
	public void respondToChange (State newState) {
		Direction[] values = Direction.values();
		writeMove(values[random.nextInt(values.length)]);
	}

}
