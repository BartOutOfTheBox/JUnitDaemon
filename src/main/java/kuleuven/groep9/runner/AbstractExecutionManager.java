/**
 * 
 */
package kuleuven.groep9.runner;

import java.util.ArrayList;
import java.util.List;

import kuleuven.groep9.classloading.Code;
import kuleuven.groep9.classloading.Project;
import kuleuven.groep9.statistics.Statistic;
import kuleuven.groep9.statistics.IStatisticTracker;
import kuleuven.groep9.statistics.StatisticTracker;
import kuleuven.groep9.internal.requests.SortingRequest;



import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.manipulation.Sorter;
import org.junit.runners.model.InitializationError;

/**
 * @author Thomas
 *
 */
public abstract class AbstractExecutionManager {
	
	private final JUnitCore core = new JUnitCore();
	
	private OverviewComputer computer;
	private Sorter sorter = Sorter.NULL;
	
	private List<StatisticTracker<? extends Statistic>> trackers = 
			new ArrayList<StatisticTracker<? extends Statistic>>();
	
	private final Project project;
	
	protected class TestingThread extends Thread{
		public TestingThread(){
			super(new Runnable(){
						@Override
						public void run() {
							if (project.isLoaded())
								AbstractExecutionManager.this.run();
						}
						
					});
		}
	}
	
	private Project.Listener projListener = new Project.Listener() {
		
		@Override
		public void stoppedLoading() {
			startTestRun();
			System.out.println("project stopped loading");
		}
		
		@Override
		public void startedLoading() {
			// TODO Auto-generated method stub
			System.out.println("project started loading");
		}
	};
	
	private Code.Listener functionalCodeListener = new Code.Listener() {
		
		@Override
		public void classRemoved(Class<?> clazz) {
			// TODO Auto-generated method stub
			System.out.println("functional class removed: " + clazz);
		}
		
		@Override
		public void classReloaded(Class<?> clazz) {
			//TODO Auto-generated method stub
			System.out.println("functional class reloaded: " + clazz);
		}
		
		@Override
		public void classChanged(Class<?> clazz) {
			//TODO Auto-generated method stub
			System.out.println("functional class changed: " + clazz);
		}
		
		@Override
		public void classAdded(Class<?> clazz) {
			// TODO Auto-generated method stub
			System.out.println("functional class added: " + clazz);
			if (project.isLoaded())
				startTestRun();
		}
	};
	
	private Code.Listener testCodeListener = new Code.Listener() {
		
		@Override
		public void classRemoved(Class<?> clazz) {
			System.out.println("Test class removed: " + clazz);
			for (StatisticTracker<? extends Statistic> t : getTrackers())
				t.removeClass(clazz);
		}
		
		@Override
		public void classReloaded(Class<?> clazz) {
			System.out.println("Test class reloaded: " + clazz);
		}
		
		@Override
		public void classChanged(Class<?> clazz) {
			System.out.println("Test class changed: " + clazz);
			for(StatisticTracker<? extends Statistic> track : getTrackers())
				track.removeClass(clazz);
		}
		
		@Override
		public void classAdded(Class<?> clazz) {
			System.out.println("Test class added: " + clazz);
			//Tests from this class will be automatically added to the subscribed trackers.
			if (project.isLoaded()) {
				startTestRun();
			}
		}
	};
	
	public AbstractExecutionManager(Project project) throws InitializationError {
		this.project = project;
		System.out.println("binding listeners to project");
		project.addListener(projListener);
		System.out.println("binding listeners to test and tested code");
		project.getTestCode().addListener(testCodeListener);
		project.getTestedCode().addListener(functionalCodeListener);
		setComputer(new OverviewComputer());
		project.reload();
	}
	
	public void run(){
		Class<?>[] testClasses = project.getTestCode().getActiveClasses().values().toArray(new Class[0]);
		core.run(new SortingRequest(Request.classes(testClasses), getSorter()));
		//core.run(new SortingRequest(Request.classes(getComputer(), testClasses), getSorter()));
	}
	
	public OverviewComputer getComputer() {
		return computer;
	}
	public void setComputer(OverviewComputer computer) {
		this.computer = computer;
	}
	
	public Sorter getSorter() {
		return sorter;
	}
	
	public void setSorter(Sorter sorter){
		this.sorter = sorter;
	}
	
	public abstract void startTestRun();
	
	public List<StatisticTracker<? extends Statistic>> getTrackers() {
		return this.trackers;
	}
	
	public void addTracker(StatisticTracker<? extends Statistic> tracker) {
		trackers.add(tracker);
		getCore().addListener(tracker);
	}

	public JUnitCore getCore() {
		return core;
	}
}
