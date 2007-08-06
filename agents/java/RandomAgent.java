import java.util.Random;

public class RandomAgent extends Agent {
	
	private Random random;

	public void init (String[] args) {
		random = new Random();
	}
	
	public void respondToChange () {
		Direction[] values = Direction.values();
		writeMove(values[random.nextInt(values.length)]);
		sendMessage(state.getPlayer(), 
								state.getPlayer(), 
								values[random.nextInt(values.length)]);
	}

}
