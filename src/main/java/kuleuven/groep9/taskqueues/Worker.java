package kuleuven.groep9.taskqueues;

import java.util.concurrent.BlockingQueue;

public abstract class Worker<T extends Task<?>> {

	public Worker(BlockingQueue<T> taskQueue) {
		this.taskQueue = taskQueue;
	}

	private final BlockingQueue<T> taskQueue;

	protected abstract void work(T task);
	
	private boolean working = false;
	
	public final void start() throws IllegalStateException {
		if (this.working) 
			throw new IllegalStateException("This worker is already working");
		this.working = true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (working)
					try {
						work(taskQueue.take());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}).start();
	}
	
	public final void stop() {
		this.working = false;
	}
}
