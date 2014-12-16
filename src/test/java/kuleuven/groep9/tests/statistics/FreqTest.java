package kuleuven.groep9.tests.statistics;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import kuleuven.groep9.runner.OverviewComputer;
import kuleuven.groep9.runner.OverviewRunner;
import kuleuven.groep9.statistics.FrequencyStatistic;
import kuleuven.groep9.statistics.StatisticTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

public class FreqTest {

	private OverviewRunner runner;
	private Class<?> clazz;
	private StatisticTracker<FrequencyStatistic> tracker;
	private RunNotifier notifier;
	
	@Before
	public void setup() throws ClassNotFoundException, InitializationError, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		String testPath = "kuleuven.groep9.samples.SwitchingFail";
		clazz = Class.forName(testPath);
		clazz.getField("success").set(clazz, true);
		Class<?>[] classes = {clazz};
		
		RunnerBuilder builder = new AllDefaultPossibilitiesBuilder(true);
		OverviewComputer computer = new OverviewComputer();
		runner = computer.getSuite(builder, classes);
		
		notifier = new RunNotifier();
		tracker = new StatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null,20));
		notifier.addListener(tracker);
	}
	
	@Test
	public void expectedOrder(){
		String[] expected = {"normal","reverse"};
		int nbTest = runner.getDescription().getChildren().size();
		for(int i=0; i<nbTest; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertTrue(name.equals(expected[i]));
		}
	}
	
	@Test
	public void initialNormal(){
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		double freq = tracker.getStatistic(descr).getFailFrequency();
		assertTrue(freq==0.0);
	}
	
	@Test
	public void initialReverse(){
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(1);
		double freq = tracker.getStatistic(descr).getFailFrequency();
		assertTrue(freq==1.0);
	}
	
	@Test
	public void half() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		runner.run(notifier);
		
		clazz.getMethod("toggle").invoke(clazz, null);
		
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		double freq = tracker.getStatistic(descr).getFailFrequency();
		assertTrue(freq==0.5);
	}
	
	@Test
	public void full(){
		runner.run(notifier);
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		double freq = tracker.getStatistic(descr).getFailFrequency();
		assertTrue(freq==0.0);
	}
	
	@Test
	public void historyCnt(){
		runner.run(notifier);
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		int cnt = tracker.getStatistic(descr).getActualHistoryCnt();
		assertTrue(cnt==2);
	}
	
	@Test
	public void max(){
		for(int i=0; i<23; i++){
			runner.run(notifier);
		}
		Description descr = runner.getDescription().getChildren().get(0);
		int cnt = tracker.getStatistic(descr).getActualHistoryCnt();
		int max = tracker.getStatistic(descr).getMaxHistoryCnt();
		assertTrue(max==20);
		assertTrue(cnt==20);
	}
	
	@Test
	public void overflowFrequency() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		for(int i=0; i<20; i++){
			runner.run(notifier);
		}
		clazz.getMethod("toggle").invoke(clazz, null);
		for(int i=0; i<15; i++){
			runner.run(notifier);
		}
		Description descr = runner.getDescription().getChildren().get(0);
		double freq = tracker.getStatistic(descr).getFailFrequency();
		assertTrue(freq==0.75);
	}
	
}
