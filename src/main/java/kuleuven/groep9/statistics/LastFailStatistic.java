package kuleuven.groep9.statistics;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Class representing if a test has or has not failed
 * during its last run.
 */
public class LastFailStatistic extends Statistic{
	
	public LastFailStatistic(Description descr) {
		super(descr);
	}

	private boolean failed;

	public boolean hasFailed() {
		return failed;
	}

	private void setFailed(boolean failed) {
		this.failed = failed;
	}
	
	/**
	 * When a test is started, it has not (yet) failed.
	 */
	@Override
	public void testStarted() {
		setFailed(false);
	}
	
	/**
	 * When a test fails, this method will save it.
	 */
	@Override
	public void testFailure(Failure failure) {
		setFailed(true);
	}

	@Override
	public Statistic clone() {
		return new LastFailStatistic(getTestDescription());
	}

	@Override
	public int compareTo(Statistic o) {
		LastFailStatistic other = (LastFailStatistic)o;
		
		boolean f1 = this.hasFailed();
		boolean f2 = other.hasFailed();
		return -Boolean.compare(f1, f2);	
	}

}
