package kuleuven.groep9.classloading;

import java.util.Date;
import java.util.Iterator;

import kuleuven.groep9.Notifier;

public class SteadyStateAnalyzer extends Notifier<SteadyStateAnalyzer.Listener> implements Runnable {
	private final long STEADY_STATE_TIME;	//in ms
	
	private boolean steady;
	private Date lastPulse;
	
	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				while (isSteady())
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				while (getTimeSinceLastPulse() < STEADY_STATE_TIME)
					try {
						this.wait(STEADY_STATE_TIME - getTimeSinceLastPulse());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			enterSteadyState();
		}
	}

	private long getTimeSinceLastPulse() {
		return new Date().getTime() - lastPulse.getTime();
	}

	public SteadyStateAnalyzer(long steadyTime) {
		this.STEADY_STATE_TIME = steadyTime;
		
		setSteady(false);
		lastPulse = new Date();
	}
	
	public void givePulse() {
		setSteady(false);
		lastPulse = new Date();
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	protected void enterSteadyState() {
		setSteady(true);
		Iterator<Listener> it = super.getListeners();
		while (it.hasNext()) {
			it.next().steadyStateDetected();
		}
	}

	public boolean isSteady() {
		return steady;
	}

	protected synchronized void setSteady(boolean steady) {
		this.steady = steady;
	}
	
	public static interface Listener {
		void steadyStateDetected();
	}	
}
