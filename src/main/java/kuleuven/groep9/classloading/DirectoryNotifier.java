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

import kuleuven.groep9.Notifier;

public class DirectoryNotifier extends Notifier<DirectoryNotifier.Listener> {
	
	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
	private SteadyStateAnalyzer analyzer;
	
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
        this.analyzer = new SteadyStateAnalyzer(1000);
        
        analyzer.addListener(new SteadyStateAnalyzer.Listener() {
			@Override
			public void steadyStateDetected() {
				Iterator<DirectoryNotifier.Listener> it = DirectoryNotifier.super.getListeners();
				while (it.hasNext())
					it.next().directoryChanged();
			}
		});
        
        Thread analyzing = new Thread(analyzer);
        
        registerAll(dir);
        
        Thread watching = new Thread(){
        	public void run() {
        		processEvents();
        	}
        };
        watching.start();
        analyzing.start();
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
		analyzer.givePulse();
	}

	public static interface Listener {
		void directoryChanged();
	}	
}
