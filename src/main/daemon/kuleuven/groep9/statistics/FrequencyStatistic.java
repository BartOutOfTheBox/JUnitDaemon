package kuleuven.groep9.statistics;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Class representing the results of the last X test runs.
 * X is a variable represented by maxHistoryCnt. The key
 * of this class is an array of size X that contains boolean
 * values, i.e. true if failure, false if succeeded.
 * The array is treated as circular by keeping the current 
 * index as an attribute, if it reaches X, it is set to 0
 * again, so old values can be written over.
 */
public class FrequencyStatistic extends Statistic{
	
	private boolean[] failHistory;
	private int maxHistoryCnt;
	private int nextIndex;
	private boolean full;
	
	public FrequencyStatistic(Description descr, int maxHistoryCnt) {
		super(descr);
		setMaxHistoryCnt(maxHistoryCnt);
		this.nextIndex = 0;
		this.failHistory = new boolean[maxHistoryCnt];
		this.full = false;
	}

	public int getMaxHistoryCnt() {
		return maxHistoryCnt;
	}

	protected void setMaxHistoryCnt(int historyCnt) {
		this.maxHistoryCnt = historyCnt;
	}
	
	/**
	 * This method returns the amount of times the method has been run.
	 * The upper limit of this number is the maximum amount of history
	 * that is being kept (i.e. when a method has been run 50 times,
	 * but only the 20 last results are kept, 20 is the return value).
	 */
	public int getActualHistoryCnt() {
		if(full) {
			return maxHistoryCnt;
		}
		else {
			return nextIndex;
		}
	}

	/**
	 * When a test is started, it is not (yet) failed, but the run
	 * has to be taken into account. This method adds FALSE to the list
	 * of the last run results.
	 */
	@Override
	public void testStarted() {
		failHistory[nextIndex] = false;
	}
	
	/**
	 * When a test is finished, whether it has failed or succeeded, 
	 * only the index needs to be incremented.
	 */
	@Override
	public void testFinished() {
		full = full || (nextIndex == 19);
		nextIndex = (nextIndex + 1) % 20;
	}
	
	/**
	 * When a test fails, the corresponding value in the list
	 * needs to be changed to TRUE.
	 */
	@Override
	public void testFailure(Failure failure) {
		failHistory[nextIndex] = true;
	}
	
	/**
	 * This method calculates the frequency of failure in 
	 * the last test runs.
	 * 
	 * @return	The fail frequency
	 */
	public double getFailFrequency() {
		double frequency = 0;
		for(int i = 0; i < getActualHistoryCnt(); i++) {
			if(failHistory[i] == true) {
				frequency++;
			}
		}
		if(getActualHistoryCnt() != 0) {
			return frequency/getActualHistoryCnt();
		}
		else {
			return 0;
		}
	}

	@Override
	public Statistic clone(Description descr) {
		return new FrequencyStatistic(descr,getMaxHistoryCnt());
	}

	@Override
	public int compareTo(Statistic o) {
		FrequencyStatistic other = (FrequencyStatistic)o;
		
		double f1 = this.getFailFrequency();
		double f2 = other.getFailFrequency();
		return -Double.compare(f1, f2);
	}
}
