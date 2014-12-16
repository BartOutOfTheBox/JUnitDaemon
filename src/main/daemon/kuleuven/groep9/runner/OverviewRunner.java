package kuleuven.groep9.runner;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;
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

	public void sort(Sorter[] sorters){
		int[] weights = new int[sorters.length];
		for(int i=0; i<weights.length; i++){
			weights[i] = 1;
		}
		sort(sorters, weights);
	}

	/**
	 * Uses multiple sorters to determine the order of the tests.
	 * Implements a round robin that allows each sorter to decide on a number of next elements,
	 * equal to the weight of the sorter.
	 * A sorter's suggestions for next tests are ignored if the test is already present in the result.
	 * If the original children lists contained duplicates for some reason,
	 * these will not be found after sorting.
	 * The order of the given sorters determines what sorter makes the first choice.
	 * 
	 * @param sorters
	 * 			The sorters that should order the tests
	 * @param weights
	 * 			These integers determine the importance of each sorter.
	 */
	public void sort(Sorter[] sorters, int[] weights){
		if(sorters.length != weights.length){
			throw new Error("There should be exactly one weight for each sorter.");
		}
		
		List<List<RecurringTest>> sorted = new ArrayList<List<RecurringTest>>();
		for(int i=0;i<sorters.length;i++){
			List<RecurringTest> sortedElem = new ArrayList<RecurringTest>();
			sort(sorters[i]);
			for(int j=0;j<getFilteredChildren().size();j++){
				RecurringTest test = getFilteredChildren().get(j);
				sortedElem.add(j,test);
			}
			sorted.add(sortedElem);
		}
		
		List<RecurringTest> result = new ArrayList<RecurringTest>();
		int[] index = new int[sorters.length];
		boolean finished = false;
		while(!finished){
			finished = true;
			for(int i=0; i<sorted.size(); i++){
				// Adds as many tests to the result as specified by the weight of the sorter,
				// unless some tests are already present in the result or the end of the sorted list is reached
				int endIndex = index[i]+weights[i];
				for(;index[i]<Math.min(sorted.get(i).size(),endIndex); index[i]++){
					finished = false;
					if(!result.contains(sorted.get(i).get(index[i]))){
						result.add(sorted.get(i).get(index[i]));
					}
				}
			}
			// Once the result contains all the tests, the sorting is done
			if(result.size()==getFilteredChildren().size()){
				finished = true;
			}
		}
		
		getFilteredChildren().clear();
		for(int j=0;j<result.size();j++){
			getFilteredChildren().add(j, result.get(j));
		}
	}
}
