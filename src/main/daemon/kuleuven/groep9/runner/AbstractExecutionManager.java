/**
 * 
 */
package kuleuven.groep9.runner;

import java.util.ArrayList;
import java.util.List;

import kuleuven.groep9.statistics.StatisticTracker;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * @author Thomas
 *
 */
public abstract class AbstractExecutionManager {
	
	private OverviewComputer computer;
	private OverviewRunner runner;
	private RunnerBuilder builder;
	private RunNotifier notifier;
	private Sorter[] sorters = {Sorter.NULL};
	private int[] weights = {1};
	
	private List<StatisticTracker> trackers = new ArrayList<StatisticTracker>();
	
	private final Project project;
	
	private class LoadingThread extends Thread{
		public LoadingThread(){
			super(new Runnable(){
	
						@Override
						public void run() {
							while(! project.isLoaded()){
								System.out.println("the project is loading?: " + project.isLoaded());
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							System.out.println("starting test run");
							startTestRun();
							
						}
						
					});
		}
	}
	
	private Thread waitTillDone;
	
	private Listener<ClassLoadedEvent> functionalCodeListener = new Listener<ClassLoadedEvent>() {
		
		@Override
		public void onEvent(ClassLoadedEvent event) {
			System.out.println("ClassLoadedEvent received");
			if (event.getKind().equals(ClassLoadedEvent.Kind.CHANGED)
					|| event.getKind().equals(ClassLoadedEvent.Kind.RELOADED)) {
				System.out.println("ClassLoadedEvent received for functionalcode (changed or reloaded");
				if(waitTillDone == null || !waitTillDone.isAlive()){
					waitTillDone = new LoadingThread();
					waitTillDone.start();
				}
			}
		}
	};
	
	private Listener<ClassLoadedEvent> testCodeListener = new Listener<ClassLoadedEvent>() {

		@Override
		public void onEvent(ClassLoadedEvent event) {
			System.out.println("ClassLoadedEvent received");
			if (event.getKind().equals(ClassLoadedEvent.Kind.NEW)
					|| event.getKind().equals(ClassLoadedEvent.Kind.CHANGED)
					|| event.getKind().equals(ClassLoadedEvent.Kind.RELOADED)) {
				System.out.println("ClassLoadedEvent received for testcode (new or changed or reloaded");
				try {
					System.out.println("reloading test in the computer");
					getComputer().reloadClass(getRunner(), event.getClazz(), builder);
				} catch (InitializationError e) {
					e.printStackTrace();
				}
				if(waitTillDone == null || !waitTillDone.isAlive()){
					waitTillDone = new LoadingThread();
					waitTillDone.start();
				}
			} else if (event.getKind().equals(ClassLoadedEvent.Kind.DELETED)) {
				getComputer().removeClass(getRunner(), event.getClazz());
			}
			
			if (event.getKind().equals(ClassLoadedEvent.Kind.CHANGED) || event.getKind().equals(ClassLoadedEvent.Kind.DELETED)) {
				for(StatisticTracker track : getTrackers()){
					track.removeClass(event.getClazz());
				}
			}
		}
		
	};
	
	public AbstractExecutionManager(Project project) throws InitializationError {
		this.project = project;
		System.out.println("binding listeners to test and tested code");
		project.getTestCode().addListener(testCodeListener);
		project.getTestedCode().addListener(functionalCodeListener);
		setComputer(new OverviewComputer());
		builder = new AllDefaultPossibilitiesBuilder(true);
		Class<?>[] classes = project.getTestCode().getActiveClasses();
		setRunner(getComputer().getSuite(builder, classes));
		setNotifier(new RunNotifier());
	}
	
	public void run(RunNotifier notifier){
		getRunner().sort(getSorters(),getWeights());
		getRunner().run(notifier);
	}
	
	public OverviewComputer getComputer() {
		return computer;
	}
	public void setComputer(OverviewComputer computer) {
		this.computer = computer;
	}
	public RunNotifier getNotifier() {
		return notifier;
	}
	public void setNotifier(RunNotifier notifier) {
		this.notifier = notifier;
	}
	public OverviewRunner getRunner() {
		return runner;
	}
	protected void setRunner(OverviewRunner runner) {
		this.runner = runner;
	}
	
	public Sorter[] getSorters() {
		return sorters;
	}
	
	public int[] getWeights(){
		return weights;
	}
	
	public void setSorters(Sorter[] sorters, int[] weights){
		this.sorters = sorters;
		this.weights = weights;
	}

	public void setSorters(Sorter[] sorters) {
		int[] newWeights = new int[sorters.length];
		for(int i=0; i<sorters.length;i++){
			newWeights[i] = 1;
		}
		
		setSorters(sorters,newWeights);
	}
	
	public abstract void startTestRun();
	
	public List<StatisticTracker> getTrackers() {
		return this.trackers;
	}
	
	public void addTracker(StatisticTracker tracker) {
		trackers.add(tracker);
		getNotifier().addListener(tracker);
	}
}
