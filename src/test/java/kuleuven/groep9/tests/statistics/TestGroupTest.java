package kuleuven.groep9.tests.statistics;

import org.junit.Test;

import kuleuven.groep9.statistics.IStatisticTracker;
import kuleuven.groep9.statistics.TestGroupManager;
import kuleuven.groep9.statistics.TestGroupStatistic;

public class TestGroupTest extends StatisticTest {

	@Override
	protected void setupTracker() {
		super.tracker = 
				new IStatisticTracker<TestGroupStatistic>(new TestGroupStatistic(null, new TestGroupManager()));
	}
	
	@Test
	public void testSameGroup() {
		//TODO
	}
	
}
