package kuleuven.groep9.tests.statistics;

import kuleuven.groep9.statistics.Statistic;
import kuleuven.groep9.statistics.StatisticTracker;

import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;

public abstract class StatisticTest {
	protected static Description ALWAYS2_DESCRIPTION;
	protected static Description NEVER2_DESCRIPTION;
	protected static Description NEVER_DESCRIPTION;
	protected static Description ALWAYS_DESCRIPTION;
	protected static Description SOMETIMES_DESCRIPTION;
	protected static Class<?> clazz;
	
	protected  JUnitCore core;
	protected  StatisticTracker<? extends Statistic> tracker;
	
	public StatisticTest() {
		setupCore();
		setupTracker();
		core.addListener(tracker);
		core.run(new Class<?>[] {clazz});
	}
	
	@BeforeClass
	public static void setup() throws ClassNotFoundException {
		clazz = Class.forName("kuleuven.groep9.statistics.TestClass");
		NEVER_DESCRIPTION = Description.createTestDescription(clazz, "neverCorrect");
		ALWAYS_DESCRIPTION = Description.createTestDescription(clazz, "alwaysCorrect");
		ALWAYS2_DESCRIPTION = Description.createTestDescription(clazz, "alwaysCorrect2");
		NEVER2_DESCRIPTION = Description.createTestDescription(clazz, "neverCorrect2");
		SOMETIMES_DESCRIPTION = Description.createTestDescription(clazz, "sometimesCorrect");
	}
	
	protected abstract void setupTracker();

	protected void setupCore() {
		this.core = new JUnitCore();
	}
}
