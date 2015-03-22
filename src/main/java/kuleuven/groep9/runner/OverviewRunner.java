package kuleuven.groep9.runner;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

/**
 * OverviewRunner is a ParentRunner of RecurringTest objects. A list of 
 * tests that need to run is being maintained. Besides that, there is a
 * StatisticCollector for each statistic.
 */
public class OverviewRunner extends ParentRunner<RecurringTest>{

	private final List<RecurringTest> fRunners;
	
	public OverviewRunner() throws InitializationError{
		super(null);
		fRunners = new ArrayList<RecurringTest>();
	}
	
	@Override
	protected final List<RecurringTest> getChildren() {
		System.out.println("the amount of children in the overview runner is " + fRunners.size());
		return fRunners;
	}

	/**
	 * Obtain the DaemonDescription of the given test.
	 * 
	 * @param	child
	 * 			The given test.
	 */
	@Override
	protected Description describeChild(RecurringTest child) {
		return child.getDescription();
	}

	/**
	 * Run the given test with the given notifier.
	 * 
	 * @param	child
	 * 			The given test.
	 * @param	notifier
	 * 			The given notifier.
	 */
	@Override
	protected void runChild(RecurringTest child, RunNotifier notifier) {
		child.run(notifier);
	}
	
	/**
	 * Delete all the tests that originate from the given test class.
	 * 
	 * @param 	testClass
	 * 			The given test class
	 */
	protected void removeClass(Class<?> testClass){
		RecurringTest[] children = getChildren().toArray(new RecurringTest[getChildren().size()]);
		for(RecurringTest child : children){
			Class<?> childClass = child.getDescription().getTestClass();
			if(testClass.getName().equals(childClass.getName())){
				fRunners.remove(child);
			}
		}
	}
	
	/**
	 * Add new RecurringTest objects that have to be run.
	 * 
	 * @param 	newRunners
	 * 			The tests to be added.
	 */
	protected void addChildren(List<RecurringTest> newRunners){
		fRunners.addAll(newRunners);
	}
	
	protected void addChild(RecurringTest newRunner) {
		fRunners.add(newRunner);
	}
}
