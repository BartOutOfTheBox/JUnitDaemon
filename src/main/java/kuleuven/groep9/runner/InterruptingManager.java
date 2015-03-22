/**
 * 
 */
package kuleuven.groep9.runner;

import kuleuven.groep9.classloading.Project;

import org.junit.runners.model.InitializationError;

/**
 * @author Thomas
 *
 */
public class InterruptingManager extends AbstractExecutionManager{

	public InterruptingManager(Project project) throws InitializationError {
		super(project);
	}

	@Override
	public void startTestRun() {
		(new AbstractExecutionManager.TestingThread()).run();
	}
}
