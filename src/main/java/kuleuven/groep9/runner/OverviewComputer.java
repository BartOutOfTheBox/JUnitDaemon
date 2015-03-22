package kuleuven.groep9.runner;

import org.junit.runners.model.*;
import org.junit.runner.*;


/**
 * @author Thomas
 *
 */
public class OverviewComputer extends Computer {

	@Override
	public OverviewRunner getSuite(RunnerBuilder builder, Class<?>[] classes)
			throws InitializationError {
		OverviewRunner overview = new OverviewRunner();
		for(Class<?> clazz : classes){
			loadClass(overview,clazz,builder);
		}
		return overview;
	}
	
	/**
	 * Voegt een lijst van Runners die elk een test uit een testklasse uitvoeren
	 * toe aan de OVerviewRunner.
	 * @param overview
	 * 			De OverviewRunner die aangepast zal worden.
	 * @param clazz
	 * 			De testklasse waarvan de methodes zullen worden toegevoegd.
	 * @param builder
	 * 			De RunnerBuilder die bepaald hoe de testmethodes moeten worden uitgevoerd.
	 * @throws InitializationError
	 */
	protected void loadClass(OverviewRunner overview, Class<?> clazz, RunnerBuilder builder) {
		Runner classRunner = builder.safeRunnerForClass(clazz);
		addAtomsToOverview(classRunner.getDescription(), overview, classRunner);
		
	}
	
	private void addAtomsToOverview(Description description,
			OverviewRunner overview, Runner baseRunner) {
		if (description.isEmpty()) {
			return;
		} else if (description.isTest()) {
			try {
				overview.addChild(new RecurringTest(description.getTestClass(), description.getMethodName(), baseRunner));
			} catch (Exception e) {
				
			}
		} else if (description.isSuite()) {
			for (Description d : description.getChildren()) {
				addAtomsToOverview(d, overview, baseRunner);
			}
		}
	}

	/**
	 * Verwijdert alle testen die afkomstig zijn van een gegeven testklasse en voegt ze opnieuw toe.
	 * @param overview
	 * @param clazz
	 * @param builder
	 * @throws InitializationError
	 */
	public void reloadClass(OverviewRunner overview, Class<?> clazz, RunnerBuilder builder) throws InitializationError{
		removeClass(overview,clazz);
		loadClass(overview,clazz,builder);
	}
	
	protected void removeClass(OverviewRunner overview, Class<?> clazz){
		overview.removeClass(clazz);
	}

}
