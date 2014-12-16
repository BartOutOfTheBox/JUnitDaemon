package kuleuven.groep9.taskqueues;

public interface Combinable<T> {
	public T combine(T other);
	public boolean canCombine(T other);
}
