package kuleuven.groep9.tests.statistics;

import kuleuven.groep9.statistics.LastFailStatistic;
import kuleuven.groep9.statistics.IStatisticTracker;

import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class ManualStatisticTrackerTest {

	private JUnitCore core;
	private IStatisticTracker<LastFailStatistic> tracker;
	
	private static Class<?> clazz;
	
	public static void main(String... args) {
		try {
			clazz = Class.forName("kuleuven.groep9.statistics.TestClass");
			new ManualStatisticTrackerTest();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private ManualStatisticTrackerTest() {
		core = new JUnitCore();
		tracker = new IStatisticTracker<LastFailStatistic>(new LastFailStatistic(null));
		core.addListener(tracker);
		core.addListener(new TextListener(new RealSystem()));
		core.run(new Class<?>[] {clazz});
	}
}
