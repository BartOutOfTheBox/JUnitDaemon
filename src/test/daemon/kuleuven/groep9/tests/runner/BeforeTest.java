package kuleuven.groep9.tests.runner;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import kuleuven.groep9.runner.RecurringTest;
import kuleuven.groep9.statistics.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import be.kuleuven.cs.ossrewriter.Monitor;
import be.kuleuven.cs.ossrewriter.MonitorEntrypoint;
import be.kuleuven.cs.ossrewriter.OSSRewriter;

public class BeforeTest {
	
	private String testPath;
	private RecurringTest[] runners;
	private RunListener listener;
	
	@Before
	public void setUp() throws InitializationError, ClassNotFoundException{
		//-----> This code should stay similar to the one in OverviewComputer
		Computer computer = Computer.serial();
		AllDefaultPossibilitiesBuilder builder = new AllDefaultPossibilitiesBuilder(true);
		testPath = "kuleuven.groep9.samples.BeforeClassBeforeCode";
		Class<?> clazz = Class.forName(testPath);
		TestClass testClass = new TestClass(clazz);
		List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Test.class);
		Class<?>[] classes = {clazz};
		runners = new RecurringTest[methods.size()];
		for(int i=0; i<methods.size(); i++){
			Runner baseRunner = computer.getSuite(builder, classes);
			runners[i] = new RecurringTest(clazz, methods.get(i), baseRunner);
		}
		//<---------
		listener = new RunListener(){
			@Override
			public void testFailure(Failure failure){
				ArrayList<String> fails = new ArrayList<String>();
				fails.add("shouldFail");
				String failMethod = failure.getDescription().getMethodName();
				assertTrue(fails.contains(failMethod));
			}
			@Override
			public void testIgnored(Description descr){
				String failMethod = descr.getMethodName();
				assertTrue(failMethod.equals("shouldIgnore"));
			}
			@Override
			public void testRunFinished(Result result){
				if(0<result.getFailureCount()){
					assertTrue(1==result.getFailureCount());
				}
				if(0<result.getIgnoreCount()){
					assertTrue(1==result.getIgnoreCount());
				}
			}
		};
	}
	
	@Test
	public void ExpectedOrder(){
		// Waarom die volgorde?
		// Het belangrijkste is dat numberAOne voor changeA komt, en in reverse order omgekeerd
		String[] expected = {"shouldIgnore","numberAOne","numberBTwo","shouldFail","changeA","changeB"};
		String[] result = new String[runners.length];
		for(int i=0; i<runners.length; i++){
			result[i] = runners[i].getMethod().getName();
			assertEquals(result[i],expected[i]);
		}
	}
	
	@Test
	public void allRunInOrder(){
		RunNotifier notifier = new RunNotifier();
		notifier.addListener(listener);
		for(int i=0; i<runners.length; i++){
			runners[i].run(notifier);
		}
	}
	
	@Test
	public void allRunReverseOrder(){
		RunNotifier notifier = new RunNotifier();
		notifier.addListener(listener);
		for(int i=runners.length-1; 0<=i; i--){
			runners[i].run(notifier);
		}
	}
	
	@Test
	public void runOnlyOneSucceed(){
		RunNotifier notifier = new RunNotifier();
		listener = new RunListener(){
			@Override
			public void testRunFinished(Result result){
				assertTrue(1==result.getRunCount());
			}
		};
		RecurringTest toRun = runners[2];
		assertTrue(toRun.getMethod().getName().equals("numberBTwo"));
		toRun.run(notifier);
	}
}
