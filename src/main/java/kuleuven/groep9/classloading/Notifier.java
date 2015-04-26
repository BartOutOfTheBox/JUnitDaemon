package kuleuven.groep9.classloading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class Notifier<L> {
	private final List<L> listeners =
            Collections.synchronizedList(new ArrayList<L>());
	
	public void addListener(L listener) {
        listeners.add(listener);
    }
	
	public void removeListener(L listener) {
        listeners.remove(listener);
    }
	
	public void removeListeners() {
		listeners.clear();
	}
	
	protected Iterator<L> getListeners() {
		return listeners.iterator();
	}
}
