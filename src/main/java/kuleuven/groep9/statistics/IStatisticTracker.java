package kuleuven.groep9.statistics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;

/**
 * This class is responsible for maintaining a specific kind of statistics about test runs it listens to.
 *  
 * @author Bart Verhoeven
 * @author Thomas Neven
 * @author Karel Moens
 *
 * @param <T>
 */
public class IStatisticTracker<T extends Statistic> extends StatisticTracker<T>{
	private final Map<Description,T> map;
	private final T prototype;
	
	public IStatisticTracker(T prototype){
		this.map = new HashMap<Description,T>();
		this.prototype = prototype;
	}
	
	@Override
	public void testStarted(Description descr) throws Exception {
		updateDescription(descr);
		getStatistic(descr).testStarted();
	}
	
	@Override
	public void testFailure(Failure failure){
		getStatistic(failure.getDescription()).testFailure(failure);
	}
	
	@Override
	public void testFinished(Description descr) {
		getStatistic(descr).testFinished();
	}

	protected Map<Description,T> getMap() {
		return map;
	}

	protected T getPrototype() {
		return prototype;
	}
	
	/* (non-Javadoc)
	 * @see kuleuven.groep9.statistics.StatisticTracker#getStatistic(org.junit.runner.Description)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getStatistic(Description descr){
		T stat;
		if(!getMap().containsKey(descr)){
			stat = (T)getPrototype().clone();
			stat.setTestDescription(descr);
			getMap().put(descr, stat);
		}else{
			stat = getMap().get(descr);
		}
		
		return stat;
	}
	
	/**
	 * This method is used to keep the Description objects in this Tracker up to date.
	 * Descriptions are regarded as equal when their class name and method name are equal.
	 * This is useful for these kind of mappings but can cause other info stored in the
	 * Description to age.
	 * More specifically, the class containing the test might have been reloaded.
	 * This leaves its name intact but all the references to the old Class object,
	 * such as the one in a Description, are rendered useless and thus waste memory.
	 * @param descr
	 */
	protected void updateDescription(Description descr){
		T stat = getStatistic(descr);
		stat.setTestDescription(descr);
		
		getMap().remove(descr);
		getMap().put(descr, stat);
	}
	
	/* (non-Javadoc)
	 * @see kuleuven.groep9.statistics.StatisticTracker#getSorter()
	 */
	@Override
	public Sorter getSorter(){
		Comparator<Description> comp = new Comparator<Description>(){
			@Override
			public int compare(Description o1, Description o2) {
				T t1 = getStatistic(o1);
				T t2 = getStatistic(o2);
				return t1.compareTo(t2);
			}
		};
		
		return new Sorter(comp);
	}
	
	/* (non-Javadoc)
	 * @see kuleuven.groep9.statistics.StatisticTracker#removeClass(java.lang.Class)
	 */
	@Override
	public void removeClass(Class<?> clazz){
		Set<Description> descrs = getMap().keySet();
		String clazzStr = clazz.getName();
		
		Description[] list = descrs.toArray(new Description[descrs.size()]);
		for(int i=0; i<list.length; i++){
			Description descr = list[i];
			if(descr.getTestClass().getName().equals(clazzStr)){
				getMap().remove(descr);
			}
		}
	}
}
