package kuleuven.groep9.tests.comparator;

import static org.junit.Assert.*;
import kuleuven.groep9.runner.OverviewComputer;
import kuleuven.groep9.runner.OverviewRunner;
import kuleuven.groep9.statistics.FrequencyStatistic;
import kuleuven.groep9.statistics.LastFailStatistic;
import kuleuven.groep9.statistics.StatisticTracker;
import kuleuven.groep9.statistics.TestGroupManager;
import kuleuven.groep9.statistics.TestGroupStatistic;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

import be.kuleuven.cs.ossrewriter.OSSRewriter;

public class SortTest {

	private OverviewRunner runner;
	private Class<?> clazz;
	
	private StatisticTracker<LastFailStatistic> lastFail;
	private StatisticTracker<FrequencyStatistic> mostFreq;
	private StatisticTracker<TestGroupStatistic> distinct;
	private RunNotifier notifier;
	
	@Before
	public void setup() throws ClassNotFoundException, InitializationError, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		String testPath = "kuleuven.groep9.samples.Divisors";
		clazz = Class.forName(testPath);
		clazz.getField("number").set(clazz, 0);
		TestClass testClass = new TestClass(clazz);
		Class<?>[] classes = {clazz};
		
		RunnerBuilder builder = new AllDefaultPossibilitiesBuilder(true);
		OverviewComputer computer = new OverviewComputer();
		runner = computer.getSuite(builder, classes);
		
		lastFail = new StatisticTracker<LastFailStatistic>(new LastFailStatistic(null));
		mostFreq = new StatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null,20));
		TestGroupManager manager = new TestGroupManager();
		distinct = new StatisticTracker<TestGroupStatistic>(new TestGroupStatistic(null,manager));
		
		notifier = new RunNotifier();
		notifier.addListener(lastFail);
		notifier.addListener(mostFreq);
		notifier.addListener(distinct);
	}
	
	@Test
	public void expectedOrder(){
		String[] expected = {"eleven","two","five","seven","three"};
		int nbTest = runner.getDescription().getChildren().size();
		for(int i=0; i<nbTest; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void initialAllSucceed(){
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			assertTrue(!lastFail.getStatistic(descr).hasFailed());
		}
	}
	
	@Test
	public void lastFailSevenEleven() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 77);
		
		runner.run(notifier);
		runner.sort(lastFail.getSorter());
		
		String[] expected = {"two","five","three","eleven","seven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void lastFailTwenty() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 20);
		
		runner.run(notifier);
		runner.sort(lastFail.getSorter());
		
		String[] expected = {"eleven","seven","three","two","five"};;
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void inOrder() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Sorter sorter = mostFreq.getSorter();
		
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		runner.sort(sorter);
		
		String[] expected = {"two","three","five","seven","eleven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
			
//			if(i==1){
//				assertTrue(descr.getFrequencyStatistic().getFailFrequency()==0.75);
//			}
		}
	}
	
	@Test
	public void groupsAllbutOne() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 7);
		runner.run(notifier);
		
		runner.sort(distinct.getSorter());
		
		String[] expected = {"eleven","two","five","seven","three"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void groupsAll() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		runner.sort(distinct.getSorter());
		
		String[] expected = {"eleven","two","five","seven","three"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void freqGroup() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		Sorter[] sorters = {mostFreq.getSorter(),distinct.getSorter()};
		runner.sort(sorters);
		
		String[] expected = {"two","three","five","seven","eleven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
//			System.out.println(name);
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void inOrderAgain() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		Sorter[] sorters = {mostFreq.getSorter(),mostFreq.getSorter()};
		runner.sort(sorters);
		
		String[] expected = {"two","three","five","seven","eleven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void groupsAllAgain() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		Sorter[] sorters = {distinct.getSorter()};
		int[] weight = {3};
		runner.sort(sorters,weight);
		
		String[] expected = {"eleven","two","five","seven","three"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void firstIgnored() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		Sorter[] sorters = {distinct.getSorter(),mostFreq.getSorter()};
		int[] weights = {0,1};
		runner.sort(sorters,weights);
		
		String[] expected = {"two","three","five","seven","eleven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void secondIgnored() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 23);
		runner.run(notifier);
		
		Sorter[] sorters = {mostFreq.getSorter(),distinct.getSorter()};
		int[] weights = {1,0};
		runner.sort(sorters,weights);
		
		String[] expected = {"two","three","five","seven","eleven"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
	}
	
	@Test
	public void removeClass() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Sorter sorter = mostFreq.getSorter();
		
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		
		mostFreq.removeClass(clazz);
		clazz.getField("number").set(clazz, 2);
		runner.run(notifier);
		
		runner.sort(sorter);
		
		String[] expected = {"eleven","five","seven","three","two"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
			assertEquals(expected[i],name);
		}
		
		assertTrue(mostFreq.getStatistic(runner.getDescription().getChildren().get(4)).getFailFrequency()==0);
	}
	@Test
	public void inOrderReverse() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		StatisticTracker<FrequencyStatistic> inOrder  = new StatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null,20));
		notifier.addListener(inOrder);
		
		clazz.getField("number").set(clazz, 3*5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 5*7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 7*11);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 11);
		runner.run(notifier);
		
		notifier.removeListener(inOrder);
		StatisticTracker<FrequencyStatistic> reverse  = new StatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null,20));
		notifier.addListener(reverse);
		
		clazz.getField("number").set(clazz, 2*3*5*7);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 2*3*5);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 2*3);
		runner.run(notifier);
		
		clazz.getField("number").set(clazz, 2);
		runner.run(notifier);
		
		Sorter[] sorters = {inOrder.getSorter(),reverse.getSorter()};
		int[] weights = {1,1};
		runner.sort(sorters,weights);
		
//		String[] expected = {"two","three","five","seven","eleven"};
//		String[] expected = {"eleven","seven","five","three","two"};
		String[] expected = {"two","eleven","three","seven","five"};
		for(int i=0; i<expected.length; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String name = descr.getMethodName();
//			System.out.println(name);
			assertEquals(expected[i],name);
		}
	}
	
}
