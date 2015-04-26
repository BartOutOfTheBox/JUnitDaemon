/**
 * 
 */
package kuleuven.groep9;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.InitializationError;

import kuleuven.groep9.classloading.Project;
import kuleuven.groep9.runner.InterruptingManager;
import kuleuven.groep9.runner.manipulation.CombinedSorter;
import kuleuven.groep9.statistics.FrequencyStatistic;
import kuleuven.groep9.statistics.LastFailStatistic;
import kuleuven.groep9.statistics.IStatisticTracker;
import kuleuven.groep9.statistics.StatisticTracker;
import kuleuven.groep9.statistics.TestGroupManager;
import kuleuven.groep9.statistics.TestGroupStatistic;

/**
 * @author OSS groep 9
 * 
 * Dit is de klasse die instaat voor het opstarten van de Daemon.
 * Daarvoor wordt er een execution manager aangemaakt, 
 * ook wordt er een notifier en een listener aangemaakt.
 */
public class JUnitDaemon {

	
	
	/**
	 * 
	 * 
	 * @param 	args
	 * 			The arguments of the program are the source directory,
	 * 			where the *.class files of the tested project are stored in subdirectories,
	 * 			the test directory, where the test classes are kept, 
	 * 			and a string indicating the statistic by which the tests should
	 * 			be sorted at execution.
	 * 			The ossrewriter agent should also be given as an argument to the 
	 * 			Java Virtual Machine.
	 * @throws InitializationError 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InitializationError {
		if(args.length < 3)
			return;
		
		Path codebase = Paths.get(args[0]);
		Path src = Paths.get(args[1]);
		Path test = Paths.get(args[2]);
		Project project = new Project(codebase, src, codebase, test);
		InterruptingManager manager = new InterruptingManager(project);
		
		StatisticTracker<LastFailStatistic> lastFailTracker = 
				new IStatisticTracker<LastFailStatistic>(new LastFailStatistic(null));
		StatisticTracker<FrequencyStatistic> frequencyTracker = 
				new IStatisticTracker<FrequencyStatistic>(new FrequencyStatistic(null, 20));
		StatisticTracker<TestGroupStatistic> testGroupTracker = 
				new IStatisticTracker<TestGroupStatistic>(new TestGroupStatistic(null, new TestGroupManager()));
		
		manager.addTracker(lastFailTracker);
		manager.addTracker(frequencyTracker);
		manager.addTracker(testGroupTracker);
		
		if(6 <= args.length){
			Sorter[] sorters = new Sorter[3];
			sorters[0] = lastFailTracker.getSorter();
			sorters[1] = frequencyTracker.getSorter();
			sorters[2] = testGroupTracker.getSorter();
			int[] weights = {Integer.valueOf(args[3]),Integer.valueOf(args[4]),Integer.valueOf(args[5])};
			
			manager.setSorter(new CombinedSorter(sorters));
		}
		
		RunListener listener = new TextListener(new RealSystem());
        manager.getCore().addListener(listener);
	}
}
