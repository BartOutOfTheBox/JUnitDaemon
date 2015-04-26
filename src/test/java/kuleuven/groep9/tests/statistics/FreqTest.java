package kuleuven.groep9.tests.statistics;

import static org.junit.Assert.*;
import kuleuven.groep9.statistics.FrequencyStatistic;
import kuleuven.groep9.statistics.IStatisticTracker;

import org.junit.Test;

public class FreqTest extends StatisticTest {
	
	public FreqTest() {
		super();
		for (int i=1; i<20; i++)
			core.run(new Class<?>[] {clazz});
	}
	
	@Test
	public void test0SuccesRate() {
		double freq = ((FrequencyStatistic) tracker.getStatistic(NEVER_DESCRIPTION)).getFailFrequency();
		System.out.println(freq);
		assertTrue(freq > 0.9);
	}
	
	@Test
	public void test100SuccesRate() {
		double freq = ((FrequencyStatistic) tracker.getStatistic(ALWAYS_DESCRIPTION)).getFailFrequency();
		System.out.println(freq);
		assertTrue(freq < 0.1);
	}
	
	@Test
	public void test50SuccesRate() {
		double freq = ((FrequencyStatistic) tracker.getStatistic(SOMETIMES_DESCRIPTION)).getFailFrequency();
		assertTrue(0.4 < freq && freq < 0.6);
	}
	
	@Test
	public void testOrder() {
		int comp = tracker.getSorter().compare(SOMETIMES_DESCRIPTION, ALWAYS_DESCRIPTION);
		assertTrue(comp < 0);
	}

	@Override
	protected void setupTracker() {
		super.tracker = new IStatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null, 20));
	}
}
