package kuleuven.groep9.taskqueues;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class TimedTask<T extends TimedTask<?>> extends Task<T>  {
	private final Date eventTime;
	private final Date deadline;
	
	protected TimedTask(long timeToCombineMillis) {
		Date date = new Date();
		this.eventTime = date;
		this.deadline = new Date(date.getTime() + timeToCombineMillis);
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		long delay = unit.convert((getDeadline().getTime() -  (new Date()).getTime()), TimeUnit.MILLISECONDS);
		return delay;
	}

	@Override
	public int compareTo(Delayed o) {
		return (int) (o.getDelay(TimeUnit.MILLISECONDS) - this.getDelay(TimeUnit.MILLISECONDS));
	}

	public Date getDeadline() {
		return deadline;
	}

	public Date getEventTime() {
		return eventTime;
	}
}
