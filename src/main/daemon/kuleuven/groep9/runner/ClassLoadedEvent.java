package kuleuven.groep9.runner;

public class ClassLoadedEvent {
	
	private final Class<?> clazz;
	private final Kind kind;
	
	public ClassLoadedEvent(Class<?> clazz, Kind kind) {
		this.clazz = clazz;
		this.kind = kind;
	}
	
	public Class<?> getClazz() {
		return this.clazz;
	}
	
	public Kind getKind() {
		return kind;
	}

	public enum Kind {
		NEW, RELOADED, DELETED, CHANGED
	}
}
