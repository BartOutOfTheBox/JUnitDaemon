package kuleuven.groep9.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Notifier<L extends Listener<E>,E> {
	private final List<L> listeners =
            Collections.synchronizedList(new ArrayList<L>());
	
	public void addListener(L listener) {
        listeners.add(listener);
    }
	
	public void removeListener(L listener) {
        listeners.remove(listener);
    }
	
	protected List<L> getListeners() {
		return listeners;
	}
	
	protected void notifyAllListeners(E event) {
		for (L listener : getListeners()){
			listener.onEvent(event);
		}
	}
}
