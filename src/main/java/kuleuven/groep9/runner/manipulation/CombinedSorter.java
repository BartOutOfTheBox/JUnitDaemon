package kuleuven.groep9.runner.manipulation;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;

public class CombinedSorter extends Sorter {

	private final Map<Sorter, Integer> sorters;
	
	public CombinedSorter(Sorter... sorters) {
		super(null);
		this.sorters = new HashMap<Sorter, Integer>();
		for (Sorter s : sorters)
			this.sorters.put(s, 1);
	}
	
	public CombinedSorter(Map<Sorter, Integer> sorters) {
		super(null);
		this.sorters = new HashMap<Sorter, Integer>();
		this.sorters.putAll(sorters);
	}
	
	//TODO complete
	@Override
	public int compare(Description o1, Description o2) {
		return 0;
	}
}
