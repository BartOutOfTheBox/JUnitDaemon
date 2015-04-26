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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kuleuven.groep9.taskqueues.TaskQueue;
import kuleuven.groep9.taskqueues.TimedTask;
import kuleuven.groep9.taskqueues.Worker;

public class DirectoryNotifier extends Notifier<DirectoryNotifier.Listener> {
	
	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
	
	private final long timeToCombine;
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
    public DirectoryNotifier(Path dir, long timeToCombine) throws IOException {
    	this.timeToCombine = timeToCombine;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        
        registerAll(dir);
        
        // TODO  multiple workers?
        new Worker<DirectoryEvent>(eventQueue) {

			@Override
			protected void work(DirectoryEvent task) {
				task.execute();
			}
		}.start();
        
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
		if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE))
			e = new FileAddedEvent(absoluteDir);
		else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE))
			e = new FileDeletedEvent(absoluteDir);
		else if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY))
			e = new FileModifiedEvent(absoluteDir);
		else
			throw new IllegalArgumentException(
					"Event type is: " + event.kind().toString() 
					+ ". This was not expected");
		eventQueue.add(e);
	}

	public static interface Listener {
		void fileAdded(Path absoluteDir);
		void fileModified(Path absoluteDir);
		void fileDeleted(Path absoluteDir);
	}
	
	protected abstract class DirectoryEvent extends TimedTask<DirectoryEvent> {
		private Path id;
		
		public DirectoryEvent(Path id) {
			this(id, DirectoryNotifier.this.timeToCombine);
		}
		
		protected DirectoryEvent(Path id, long timeToCombineMillis) {
			super(timeToCombineMillis);
			this.id = id;
		}
		
		@Override
		public boolean canCombine(DirectoryEvent other) {
			if (other == null)
				return false;
			if (other.getId().equals(this.getId()))
				return true;
			if (other.getId().startsWith(this.getId()))
				return true;
			if (this.getId().startsWith(other.getId()))
				return true;
			return false;
		}

		protected Path getId() {
			return this.id;
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
			boolean isSuperDirEvent = other.getId().startsWith(getId());
			if (other instanceof FileAddedEvent) {
				if (isSuperDirEvent)
					return new FileAddedEvent(getId());
				return new FileAddedEvent(other.getId());
			}
			return other.combine(this);
		}

		@Override
		public void execute() {
			Iterator<Listener> it = getListeners();
			while (it.hasNext())
				it.next().fileAdded(getId());				
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
			boolean isSuperDirEvent = other.getId().startsWith(getId());
			if (other instanceof FileAddedEvent) {
				// Determine which event was first
				if (this.getEventTime().before(other.getEventTime())) {
					//delete was first;
					// TODO return FileModifiedevent instead?
					if (isSuperDirEvent)
						return new FileAddedEvent(other.getId());
					return new FileAddedEvent(getId());
				}
				//added and then deleted again
				if (getId().equals(other.getId()))
					return new NoEvent(getId());
				if (isSuperDirEvent)
					return new FileDeletedEvent(this.getId());
				return new FileAddedEvent(other.getId());
			} if (other instanceof FileDeletedEvent) {
				if (isSuperDirEvent)
					return new FileDeletedEvent(getId());
				return new FileDeletedEvent(other.getId());
			}
			return other.combine(this);
		}

		@Override
		public void execute() {
			Iterator<Listener> it = getListeners();
			while (it.hasNext())
				it.next().fileDeleted(getId());	
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
				return new FileAddedEvent(other.getId());
			if (other instanceof FileDeletedEvent)
				return new FileDeletedEvent(other.getId());	//TODO: is removed -> modified a scenario?
			boolean isSuperDirEvent = other.getId().startsWith(getId());
			if (other instanceof FileModifiedEvent) {
				if (isSuperDirEvent)
					return new FileModifiedEvent(getId());
				return new FileModifiedEvent(other.getId());
			}
			return other.combine(this);
		}

		@Override
		public void execute() {
			// TODO always notify?
			if (getId().toFile().isFile()) {
				Iterator<Listener> it = getListeners();
				while (it.hasNext())
					it.next().fileModified(getId());		
			}
		}
	}
	
}
