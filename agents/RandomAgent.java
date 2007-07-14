import java.util.Random;

public class RandomAgent extends Agent {
    
    private Random random;

    public static void main (String[] args) {
	if (args.length < 2) {
	    System.out.println("java RandomAgent [hostname] [port]");
	    return;
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
	RandomAgent agent = new RandomAgent(hostname, port);
	if (!agent.openConnection())
	    return;
	agent.runAgent();
    }

    public RandomAgent (String hostname, int port) {
	super(hostname, port);
	random = new Random();
    }

    public Direction respondToChange (State newState) {
	Direction[] values = Direction.values();
	return values[random.nextInt(values.length)];
    }

}
