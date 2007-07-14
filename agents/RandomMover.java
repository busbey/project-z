import java.util.Random;

public class RandomMover implements Mover {
    
    private Random random;

    public RandomAgent (String args[]) {
	random = new Random();
    }

    public Direction respondToChange (State newState) {
	Direction[] values = Direction.values();
	return values[random.nextInt(values.length)];
    }

}
