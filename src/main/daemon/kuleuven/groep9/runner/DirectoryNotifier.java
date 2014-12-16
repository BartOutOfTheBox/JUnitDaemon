package kuleuven.groep9.runner;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class DirectoryNotifier extends Notifier<Listener<DirectoryEvent>, DirectoryEvent> {
	
	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
	
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
    DirectoryNotifier(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }
        
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
            	System.out.println("notifying directoryListeners for: " + dir.resolve(((WatchEvent<Path>) event).context()));
            	WatchEvent<Path> castedEvent = (WatchEvent<Path>) event;
                notifyAllListeners(new DirectoryEvent(dir, castedEvent));
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

	/**
	 * @return the recursive
	 */
	public boolean isRecursive() {
		return recursive;
	}

}
