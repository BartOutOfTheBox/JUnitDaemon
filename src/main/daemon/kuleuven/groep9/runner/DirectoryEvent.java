package kuleuven.groep9.runner;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class DirectoryEvent {
	private final Path dir;
	private final WatchEvent<Path> event;
	
	public DirectoryEvent(Path dir, WatchEvent<Path> event) {
		this.dir = dir;
		this.event = event;
	}

	public WatchEvent<Path> getEvent() {
		return event;
	}

	public Path getDir() {
		return dir;
	}

}
