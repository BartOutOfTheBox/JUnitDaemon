package kuleuven.groep9.runner;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;

/**
 * A RecurringTest is a Runner executing only a single test method.
 * It contains a reference to the method it represents and the test 
 * class the method belongs to. 
 */
public class RecurringTest extends Runner {

	private final FrameworkMethod method;
	private final Class<?> clazz;
	private Runner filteredRunner;
	
	public RecurringTest(Class<?> clazz, String method, Runner baseRunner) throws NoSuchMethodException, SecurityException {
		FrameworkMethod fMethod = new FrameworkMethod(clazz.getMethod(method, new Class<?>[0]));
		this.method = fMethod;
		this.clazz = clazz;
		calculateRunner(baseRunner);
	}
	
	public RecurringTest(Class<?> clazz, FrameworkMethod method,Runner baseRunner){
		this.method = method;
		this.clazz = clazz;
		calculateRunner(baseRunner);
	}

	private void calculateRunner(Runner baseRunner) {
		System.out.println("UnfilteredTests: " + baseRunner.getDescription().getDisplayName());
		
		Description descr = Description.createTestDescription(clazz, method.getName(), method.getAnnotations());
		Filter filter = Filter.matchMethodDescription(descr);
		try {
			filter.apply(baseRunner);
		} catch (NoTestsRemainException e) {
			baseRunner = Suite.emptySuite();
		}
		
		filteredRunner = baseRunner;
		String testName = filteredRunner.getDescription().getDisplayName();
		System.out.println("One test is: " + testName);
	}
	
	public FrameworkMethod getMethod() {
		return method;
	}

	public Class<?> getTestClass() {
		return clazz;
	}
	
	/**
	 * Run the test with the given notifier.
	 */
	@Override
	public void run(RunNotifier notifier) {
		filteredRunner.run(notifier);
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(getTestClass(), getMethod().getName());
	}

}
