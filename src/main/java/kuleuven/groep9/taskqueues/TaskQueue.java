package kuleuven.groep9.taskqueues;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class TaskQueue<E extends Task<E>> extends DelayQueue<E> {
	@Override
	public boolean add(E e) {
		e = combineWhenPossible(e);
		return super.add(e);
	}
	
	@Override
	public boolean offer(E e) {
		e = combineWhenPossible(e);
		return super.offer(e);
	}
	
	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) {
		e = combineWhenPossible(e);
		return super.offer(e, timeout, unit);
	}
	
	@Override
	public void put(E e) {
		e = combineWhenPossible(e);
		super.put(e);
	}

	private E combineWhenPossible(E e) throws ClassCastException {
		Iterator<E> it = super.iterator();
		while(it.hasNext()) {
			System.out.println(">> checking combinations");
			E current = it.next();
			if (current.canCombine(e)) {
				it.remove();
				//TODO: remove multiple events if one super event acts about both events.
				return current.combine(e);
			}
		}
		return e;
	}
}
