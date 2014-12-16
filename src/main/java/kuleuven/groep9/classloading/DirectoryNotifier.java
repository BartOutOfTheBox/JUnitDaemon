package kuleuven.groep9.classloading;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import kuleuven.groep9.Notifier;
import kuleuven.groep9.taskqueues.Task;
import kuleuven.groep9.taskqueues.TaskQueue;

public class DirectoryNotifier extends Notifier<DirectoryNotifier.Listener> {
	
	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
	
	private final long timeToCombine = 200L;
	private final TaskQueue<DirectoryEvent> eventQueue = new TaskQueue<DirectoryEvent>();

	
	/**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }
    
    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
            	register(start.resolve(dir));
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Creates a WatchService and registers the given directory
     */
    public DirectoryNotifier(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        
        registerAll(dir);
        
        Thread watching = new Thread(){
        	public void run() {
        		processEvents();
        	}
        };
        watching.start();
    }
	
	/**
     * Process all events for keys queued to the watcher
     */
    @SuppressWarnings("unchecked")
	void processEvents() {
        for (;;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
            	WatchEvent<Path> castedEvent = (WatchEvent<Path>) event;
            	Path absoluteDir = dir.resolve(castedEvent.context());
            	try {
            		if (Files.isDirectory(absoluteDir))
                		if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE))
                				//TODO: might be needed to catch the exception when a 
                				//dir was added and removed again at light speed.
    							registerAll(absoluteDir);
            		registerEvent(castedEvent, absoluteDir);
            	} catch (IOException e) {
            		
            	}
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

	private void registerEvent(WatchEvent<Path> event, Path absoluteDir) {
		DirectoryEvent e;
		if (event.kind().equals(ENTRY_CREATE))
			e = new FileAddedEvent(absoluteDir);
		if (event.kind().equals(ENTRY_DELETE))
			e = new FileDeletedEvent(absoluteDir);
		if (event.kind().equals(ENTRY_MODIFY))
			e = new FileModifiedEvent(absoluteDir);
		else
			throw new IllegalArgumentException();
		eventQueue.add(e);
	}

	public static interface Listener {
		void fileAdded(Path absoluteDir);
		void fileModified(Path absoluteDir);
		void fileDeleted(Path absoluteDir);
	}
	
	protected abstract class DirectoryEvent extends Task<DirectoryEvent> {
		private Path id;
		private final Date eventTime;
		private final Date deadline;
		
		public DirectoryEvent(Path id) {
			this(id, DirectoryNotifier.this.timeToCombine);
		}
		
		protected DirectoryEvent(Path id, long timeToCombineMillis) {
			this.id = id;
			Date date = new Date();
			this.eventTime = date;
			this.deadline = new Date(date.getTime() + timeToCombineMillis);
		}
		
		@Override
		public boolean canCombine(DirectoryEvent other) {
			if (other == null)
				return false;
			return (other.getId().equals(this.getId()));
		}

		protected Path getId() {
			return this.id;
		}
		
		@Override
		public long getDelay(TimeUnit unit) {
			long delay = unit.convert((getDeadline().getTime() -  (new Date()).getTime()), TimeUnit.MILLISECONDS);
			System.out.println("The delay for this elm is " + delay);
			return delay;
		}

		@Override
		public int compareTo(Delayed o) {
			return (int) (o.getDelay(TimeUnit.MILLISECONDS) - this.getDelay(TimeUnit.MILLISECONDS));
		}

		protected Date getDeadline() {
			return deadline;
		}

		protected Date getEventTime() {
			return eventTime;
		}
	}
	
	protected class NoEvent extends DirectoryEvent {

		public NoEvent(Path id) {
			super(id);
		}

		@Override
		public DirectoryEvent combine(DirectoryEvent other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given events cannot combine.");
			if (other instanceof NoEvent)
				return new NoEvent(getId());
			return other.combine(this);
		}

		@Override
		public void execute() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class FileAddedEvent extends DirectoryEvent {

		public FileAddedEvent(Path id) {
			super(id);
		}

		@Override
		public DirectoryEvent combine(DirectoryEvent other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given events cannot combine.");
			if (other instanceof NoEvent)
				return new FileAddedEvent(getId());
			if (other instanceof FileAddedEvent)		//TODO: check if this is a valid scenario
				return new FileAddedEvent(getId());
			return other.combine(this);
		}

		@Override
		public void execute() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class FileDeletedEvent extends DirectoryEvent {

		public FileDeletedEvent(Path id) {
			super(id);
		}

		@Override
		public DirectoryEvent combine(DirectoryEvent other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given events cannot combine.");
			if (other instanceof NoEvent)
				return new FileDeletedEvent(getId());
			if (other instanceof FileAddedEvent) {
				// Determine which event was first
				if (this.getEventTime().before(other.getEventTime()))
					//delete was first;
					return new FileAddedEvent(getId());
				//added and then deleted again
				return new NoEvent(getId());
			} if (other instanceof FileDeletedEvent)
				return new FileDeletedEvent(getId());	//TODO: check if this is a valid scenario
			return other.combine(this);
		}

		@Override
		public void execute() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class FileModifiedEvent extends DirectoryEvent {

		public FileModifiedEvent(Path id) {
			super(id);
		}

		@Override
		public DirectoryEvent combine(DirectoryEvent other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given events cannot combine.");
			if (other instanceof NoEvent)
				return new FileModifiedEvent(getId());
			if (other instanceof FileAddedEvent)
				return new FileAddedEvent(getId());
			if (other instanceof FileDeletedEvent)
				return new FileDeletedEvent(getId());	//TODO: is removed -> modified a scenario?
			if (other instanceof FileModifiedEvent)
				return new FileModifiedEvent(getId());
			return other.combine(this);
		}

		@Override
		public void execute() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
