package kuleuven.groep9.tests.statistics;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import kuleuven.groep9.runner.OverviewComputer;
import kuleuven.groep9.runner.OverviewRunner;
import kuleuven.groep9.statistics.LastFailStatistic;
import kuleuven.groep9.statistics.StatisticTracker;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

import be.kuleuven.cs.ossrewriter.OSSRewriter;

public class LastFailTest {

	private OverviewRunner runner;
	private Class<?> clazz;
	private StatisticTracker<LastFailStatistic> tracker;
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
		tracker = new StatisticTracker<LastFailStatistic>(new LastFailStatistic(null));
		notifier.addListener(tracker);
	}
	
	@Test
	public void expectedOrder(){
		String[] expected = {"normal","reverse"};
		int nbTest = runner.getDescription().getChildren().size();
		for(int i=0; i<nbTest; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(name,expected[i]);
		}
	}
	
	@Test
	public void before(){
		Description descr = runner.getDescription().getChildren().get(0);
		boolean normal = tracker.getStatistic(descr).hasFailed();
		assertTrue(!normal);
	}
	
	@Test
	public void initialNormal(){
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		boolean normal = tracker.getStatistic(descr).hasFailed();
		assertTrue(!normal);
	}
	
	@Test
	public void initialReverse(){
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(1);
		boolean reverse = tracker.getStatistic(descr).hasFailed();
		assertTrue(reverse);
	}
	
	@Test
	public void switchNormal() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		clazz.getMethod("toggle").invoke(clazz, null);
		
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		boolean normal = tracker.getStatistic(descr).hasFailed();
		assertTrue(normal);
	}
	
	@Test
	public void switchReverse() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		clazz.getMethod("toggle").invoke(clazz, null);
		
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(1);
		boolean reverse = tracker.getStatistic(descr).hasFailed();
		assertTrue(!reverse);
	}
	
	@Test
	public void consequtive() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		runner.run(notifier);
		Description descr = runner.getDescription().getChildren().get(0);
		boolean normal = tracker.getStatistic(descr).hasFailed();
		assertTrue(!normal);
		
		clazz.getMethod("toggle").invoke(clazz, null);
		
		runner.run(notifier);
		descr = runner.getDescription().getChildren().get(0);
		normal = tracker.getStatistic(descr).hasFailed();
		assertTrue(normal);
	}
	
}
