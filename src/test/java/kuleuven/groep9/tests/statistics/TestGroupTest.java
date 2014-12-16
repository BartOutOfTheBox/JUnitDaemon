package kuleuven.groep9.tests.statistics;

import static org.junit.Assert.*;
import kuleuven.groep9.runner.OverviewComputer;
import kuleuven.groep9.runner.OverviewRunner;
import kuleuven.groep9.runner.RecurringTest;
import kuleuven.groep9.statistics.FrequencyStatistic;
import kuleuven.groep9.statistics.StatisticTracker;
import kuleuven.groep9.statistics.TestGroup;
import kuleuven.groep9.statistics.TestGroupManager;
import kuleuven.groep9.statistics.TestGroupStatistic;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

public class TestGroupTest {
	
	private OverviewRunner runner;
	private StatisticTracker<TestGroupStatistic> tracker;
	private RunNotifier notifier;
	
	@Before
	public void setup() throws ClassNotFoundException, InitializationError{
		String testPath = "kuleuven.groep9.samples.FailingMethods";
		Class<?> clazz = Class.forName(testPath);
		TestClass testClass = new TestClass(clazz);
		Class<?>[] classes = {clazz};
		
		RunnerBuilder builder = new AllDefaultPossibilitiesBuilder(true);
		OverviewComputer computer = new OverviewComputer();
		runner = computer.getSuite(builder, classes);
		
		notifier = new RunNotifier();
		TestGroupManager manager = new TestGroupManager();
		tracker = new StatisticTracker<TestGroupStatistic>(new TestGroupStatistic(null,manager));
		notifier.addListener(tracker);
	}
	
	@Test
	public void before(){
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			String groupName = tracker.getStatistic(descr).getGroup().getName();
			assertEquals(groupName,"Unknown");
		}
	}
	
	@Test
	public void expected(){
		runner.run(notifier);
		String[] expected = {"kuleuven.groep9.samples.FailingMethods.moreElements(FailingMethods.java:64)",
				"kuleuven.groep9.samples.FailingMethods.twoLines(FailingMethods.java:69)",
				"kuleuven.groep9.samples.FailingMethods.twoLines(FailingMethods.java:72)",
				"kuleuven.groep9.samples.FailingMethods.moreElements(FailingMethods.java:59)",
				"kuleuven.groep9.samples.FailingMethods.wantError(FailingMethods.java:54)",
				"Success",
				"org.junit.Assert.fail(Assert.java:86)",
				"org.junit.Assert.fail(Assert.java:86)"};
		int nbTest = runner.getDescription().getChildren().size();
		for(int i=0; i<nbTest; i++){
			Description descr = runner.getDescription().getChildren().get(i);
			String groupName = tracker.getStatistic(descr).getGroup().getName();
//			System.out.println(groupName);
			assertEquals(groupName,expected[i]);
		}
	}
	
	@Test
	public void sameGroup(){
		String nothing1 = "";
		String nothing2 = "";
		
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			if(descr.getMethodName().equals("nothing1")){
				nothing1 = tracker.getStatistic(descr).getGroup().getName();
			}else if(descr.getMethodName().equals("nothing2")){
				nothing2 = tracker.getStatistic(descr).getGroup().getName();
			}
		}
		assertTrue(!nothing1.equals(""));
		assertEquals(nothing1,nothing2);
	}
	
	@Test
	public void oneRepresentative(){
		boolean nothing1 = false;
		boolean nothing2 = false;
		
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			if(descr.getMethodName().equals("nothing1")){
				nothing1 = tracker.getStatistic(descr).isRepresentative();
			}else if(descr.getMethodName().equals("nothing2")){
				nothing2 = tracker.getStatistic(descr).isRepresentative();
			}
		}
		assertTrue(nothing1||nothing2);
		assertTrue(nothing1!=nothing2);
	}
	
	@Test
	public void differentGroup(){
		String first = "";
		String other = "";
		
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			if(descr.getMethodName().equals("throwsIndex")){
				first = tracker.getStatistic(descr).getGroup().getName();
			}else if(descr.getMethodName().equals("throwsOtherIndex")){
				other = tracker.getStatistic(descr).getGroup().getName();
			}
		}
		assertTrue(!first.equals(""));
		assertTrue(!other.equals(""));
		assertTrue(!first.equals(other));
	}
	
	@Test
	public void differentGroupSameMethod(){
		String first = "";
		String second = "";
		
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			if(descr.getMethodName().equals("firstLine")){
				first = tracker.getStatistic(descr).getGroup().getName();
			}else if(descr.getMethodName().equals("secondLine")){
				second = tracker.getStatistic(descr).getGroup().getName();
			}
		}
		assertTrue(!first.equals(""));
		assertTrue(!second.equals(""));
		assertTrue(!first.equals(second));
	}
	
	@Test
	public void unknownRepresentative(){
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			boolean representative = tracker.getStatistic(descr).isRepresentative();
			assertTrue(!representative);
		}
	}
	
	@Test
	public void successRepresentative(){
		runner.run(notifier);
		
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			if(descr.getMethodName().equals("notInNothing")){
				boolean representative = tracker.getStatistic(descr).isRepresentative();
				assertTrue(!representative);
			}
		}
	}
	
	@Test
	public void groupContains(){
		runner.run(notifier);
		for(Description each :runner.getDescription().getChildren()){
			Description descr = each;
			String name = descr.getDisplayName();
			TestGroup group = tracker.getStatistic(descr).getGroup();
			
			boolean included = false;
			for(TestGroupStatistic test : group.getMembers()){
				if(test.getTestDescription().getDisplayName().equals(name)){
					included = true;
				}
			}
			assertTrue(included);
		}
	}

}
