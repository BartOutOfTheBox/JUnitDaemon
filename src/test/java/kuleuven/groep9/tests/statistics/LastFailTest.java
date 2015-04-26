package kuleuven.groep9.tests.statistics;

import static org.junit.Assert.*;

import kuleuven.groep9.statistics.LastFailStatistic;
import kuleuven.groep9.statistics.IStatisticTracker;

import org.junit.Test;

public class LastFailTest extends StatisticTest {
	
	@Test
	public void testOrderTest() {
		int comp = tracker.getSorter().compare(ALWAYS_DESCRIPTION, NEVER_DESCRIPTION);
		assertTrue(comp > 0 );
	}
	
	@Test
	public void testBothFailed() {
		int comp = tracker.getSorter().compare(NEVER_DESCRIPTION, NEVER2_DESCRIPTION);
		assertTrue(comp == 0 );
	}
	
	@Test
	public void testBothPassed() {
		int comp = tracker.getSorter().compare(ALWAYS_DESCRIPTION, ALWAYS2_DESCRIPTION);
		assertTrue(comp == 0 );
	}

	@Override
	protected void setupTracker() {
		super.tracker = new IStatisticTracker<LastFailStatistic>(new LastFailStatistic(null));
	}
	
}
