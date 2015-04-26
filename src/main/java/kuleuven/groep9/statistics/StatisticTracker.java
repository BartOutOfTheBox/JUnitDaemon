package kuleuven.groep9.statistics;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunListener;

public abstract class StatisticTracker<T extends Statistic> extends RunListener {

	public abstract T getStatistic(Description descr);

	public abstract Sorter getSorter();

	public abstract void removeClass(Class<?> clazz);

}