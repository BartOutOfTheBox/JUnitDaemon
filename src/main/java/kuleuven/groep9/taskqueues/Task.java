package kuleuven.groep9.taskqueues;

import java.util.concurrent.Delayed;

public abstract class Task<T extends Task<?>> implements Combinable<T>, Delayed {
	
	public abstract void execute();

}
