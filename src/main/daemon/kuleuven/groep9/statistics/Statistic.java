package kuleuven.groep9.statistics;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Class representing a Statistic. It has to extend RunListener
 * because a Statistic will have to react (i.e. change its values)
 * when an event occurs in the test run.
 */
public abstract class Statistic implements Comparable<Statistic>{
	private Description testDescription;
	
	public Statistic(Description descr){
		this.testDescription = descr;
	}

	public Description getTestDescription() {
		return testDescription;
	}
	
	protected void setTestDescription(Description descr){
		testDescription = descr;
	}
	
	abstract public Statistic clone(Description descr);
	
	public void testStarted(){}
	
	public void testFailure(Failure failure){}
	
	public void testFinished(){}
	
	/**
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 *       More specifically, the 'testDescription' field is not taken into account
	 *       when comparing with this method.
	 */
	abstract public int compareTo(Statistic o);
}
