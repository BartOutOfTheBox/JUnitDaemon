package kuleuven.groep9.classloading;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kuleuven.groep9.taskqueues.TaskQueue;
import kuleuven.groep9.taskqueues.TimedTask;
import kuleuven.groep9.taskqueues.Worker;

/**
 * This class represents a kind of code from the project to be analyzed by the JUnitDaemon.
 * It is responsible for listening to file changes, 
 * and making sure the latest version of the .class files is loaded into the virtual machine.
 * 
 * It is also possible for other classes to subscribe to this class.
 * If they do, they will get a notification if a class is (re)loaded into the jvm. 
 * 
 * @author r0254751
 *
 */
public class Code extends Notifier<Code.Listener> {

	private static final String JAVA_EXTENSION = "class";
	
	private final Project project;
	
	private final Path codebaseDir;
	private final Path codeDir;
	
	//TODO use hashmaps <name, class>
	private Map<String, Class<?>> dirtyClasses = new HashMap<String, Class<?>>();	
	private Map<String, Class<?>> activeClasses = new HashMap<String, Class<?>>();
	
	private final long timeToCombine;
	private final TaskQueue<ClassEvent> eventQueue;
	
	/**
	 * This constructor uses the dedicated constructor {@link #Code(Path, Path)} as follows:
	 * this(codeDir, codeDir)
	 * 
	 * To be used when you want to represent all code in a sourcefolder in one object.
	 * 
	 * @throws IOException
	 */
	public Code(Project project, Path codeDir, long timeToCombine) throws IOException {
		this(project, codeDir, codeDir, timeToCombine);
	}
	
	/**
	 * This constructor is the dedicated constructor.
	 * It's supposed to be used by any other constructor in this class.
	 * 
	 * @param project	The project this code is related to.
	 * @param codebaseDir	The directory where the source folder relevant to 
	 * 						this code is found.
	 * 						This directory may differ from the directory where 
	 * 						the files relevant to this code are stored.
	 * 						This is the case for example when the source folder is located in 
	 * 						"my-app/", and the package which contains the relevant code is located in
	 * 						"my-app/my-package".
	 * @param codeDir	The directory where the package relevant to this code is found.
	 * @throws IOException When the directoryNotifier can not notify about the requested sourcefolder.
	 */
	public Code(Project project, Path codebaseDir, Path codeDir, long timeToCombine) throws IOException {
		this.timeToCombine = timeToCombine;
		this.eventQueue = new TaskQueue<ClassEvent>();
		this.project = project;
		this.codebaseDir = codebaseDir;
		this.codeDir = codeDir;
		
		// TODO  multiple workers?
        new Worker<ClassEvent>(eventQueue) {

			@Override
			protected void work(ClassEvent task) {
				task.execute();
			}
		}.start();
		
		DirectoryNotifier dirNotifier = new DirectoryNotifier(codeDir, 200L);
		dirNotifier.addListener(this.getListener());
	}

	private DirectoryNotifier.Listener getListener() {
		return dirListener;
	}

	/**
	 * @return the directory this code lives in.
	 */
	public Path getCodebaseDir() {
		return codebaseDir;
	}

	/**
	 * @return the codeDir the packages relevant to this code are located.
	 */
	public Path getCodeDir() {
		return codeDir;
	}
	
	/**
	 * Checks whether the given path is of the form *.class
	 */
	private boolean isJava(String name){
		String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
		return JAVA_EXTENSION.equalsIgnoreCase(extension);
	}
	
	/**
	 * This method makes a new {@link URLClassLoader} and 
	 * uses this to load the class specified by the given path.
	 * 
	 * @param pathToClass The path to the class to load.
	 * @return A {@link Class} object representing the newly loaded class.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected Class<?> loadClassFromPath(Path pathToClass) 
			throws ClassNotFoundException, IOException {
		String className = getClassNameFromPath(pathToClass);
		Class<?> clazz = getProject().getClassLoader().loadClass(className);
		return clazz;
	}
	
	/**
	 * Get the name of a class given a Path to the class file.
	 * @param pathToClass The path to the class file
	 * @return the name of the class specified by the given Path
	 */
	public String getClassNameFromPath(Path pathToClass) {
		String className;
		className = getCodebaseDir().relativize(pathToClass).toString();
		className = className.replace('/', '.');
		className = className.replace('\\', '.');
		if (isJava(className)) 
			className = className.substring(0, className.length() - JAVA_EXTENSION.length() - 1);
		return className;
	}

	protected Project getProject() {
		return project;
	}

	public void reload() {
		try {
			final Map<String, Class<?>> newActiveClasses = new HashMap<String, Class<?>>();
			Files.walkFileTree(getCodeDir(), new SimpleFileVisitor<Path>() {			    
			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
			    	throws IOException {
			    	if(isJava(file.toString())){
			    		try {
							Class<?> clazz = loadClassFromPath(file);
							newActiveClasses.put(clazz.getName(), clazz);
							if (dirtyClasses.containsKey(clazz.getName())) {
								Iterator<Code.Listener> it = getListeners();
								while (it.hasNext())
									it.next().classChanged(clazz);
							} else if (activeClasses.containsKey(clazz.getName())) {
								Iterator<Code.Listener> it = getListeners();
								while (it.hasNext())
									it.next().classReloaded(clazz);
							} else {
								Iterator<Code.Listener> it = getListeners();
								while (it.hasNext())
									it.next().classAdded(clazz);
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
			    	}
			        return FileVisitResult.CONTINUE;
			    }
			});
			for (Class<?> clazz : this.activeClasses.values()) {
				boolean contains = false;
				for (Class<?> currentClazz : newActiveClasses.values()) {
					if (currentClazz.getName().startsWith(clazz.getName())) {
						contains = true;
						break;
					}
				}
				if (! contains) {
					Iterator<Code.Listener> it = getListeners();
					while (it.hasNext())
						it.next().classRemoved(clazz);
				}
			}
			this.activeClasses = newActiveClasses;
			this.dirtyClasses.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Class<?>> getActiveClasses() {
		return activeClasses;
	}

	/**
	 * This DirectoryListener is responsible for calling the right methods 
	 * when a file related to the code this object represents is added/altered/deleted.
	 */
	private final DirectoryNotifier.Listener dirListener = new DirectoryNotifier.Listener() {
		
		@Override
		public void fileDeleted(Path absoluteDir) {
			if (isJava(getFileName(absoluteDir))) {
				for (Class<?> clazz : getActiveClasses().values()) {
					if (clazz.getName().startsWith(getClassNameFromPath(absoluteDir))) {
						eventQueue.add(new ClassDeletedEvent(clazz));
					}
				}
			}
		}
		
		@Override
		public void fileModified(Path absoluteDir) {
			if (isJava(getFileName(absoluteDir))) {
				for (Class<?> clazz : getActiveClasses().values()) {
					if (clazz.getName().startsWith(getClassNameFromPath(absoluteDir))) {
						eventQueue.add(new ClassModifiedEvent(clazz));
					}
				}
			}
		}
		
		@Override
		public void fileAdded(Path absoluteDir) {
			//Load the new file(s)
			//This can be done using the still existing ClassLoader.
			//Dependencies will still be okay, since no class actually changed.
			try {
				// TODO does this walkFileTree work if you give it a file to walk on?
				Files.walkFileTree(absoluteDir, new SimpleFileVisitor<Path>() {			    
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
							throws IOException {
						if(isJava(file.toString())){
							eventQueue.add(new ClassAddedEvent(file));
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * Get the filename of the file related to the given event.
		 * @param event The event to examine.
		 * @return The name of the altered file.
		 */
		private String getFileName(Path path){
	        Path child = path.resolve(path);
	        return child.toString();
		}
	};

	public interface Listener {
		public void classReloaded(Class<?> clazz);
		public void classChanged(Class<?> clazz);
		public void classRemoved(Class<?> clazz);
		public void classAdded(Class<?> clazz);
	}
	
	protected abstract class ClassEvent extends TimedTask<ClassEvent> {
		protected Class<?> clazz;
		
		public ClassEvent(Class<?> clazz) {
			this(clazz, Code.this.timeToCombine);
		}
		
		protected ClassEvent(Class<?> clazz, long timeToCombineMillis) {
			super(timeToCombineMillis);
			this.clazz = clazz;
		}
		
		public boolean canAlwaysCombine(ClassEvent other) {
			if (other == null)
				return false;
			if (other.getClazz().getName().equals(getClazz().getName()))
				return true;
			return false;
		}

		public Class<?> getClazz() {
			return this.clazz;
		}
	}
	
	protected class ClassDeletedEvent extends ClassEvent {

		public ClassDeletedEvent(Class<?> clazz) {
			super(clazz);
		}

		@Override
		public ClassEvent combine(ClassEvent other) {
			if (other instanceof ClassDeletedEvent)
				return new ClassDeletedEvent(getClazz());
			return other.combine(this);
		}

		@Override
		public void execute() {
			//java doesn't allow manual unloading of classes,
			//(this isn't a problem though. If they were referenced, 
			//they couldn't be deleted without altering other files.)
			
			//but Listeners still might need to be notified,
			//since they need to remove the test class from the OverviewRunner
			//notify the ClassReloadedListeners there's a class gone.
			Iterator<Code.Listener> it = getListeners();
			while (it.hasNext())
				it.next().classRemoved(getClazz());
		}

		@Override
		public boolean canCombine(ClassEvent other) {
			if (canAlwaysCombine(other))
				return true;
			if (other instanceof ClassDeletedEvent)
				return false;
			return other.canCombine(this);
		}
	}
	
	protected class ClassAddedEvent extends ClassEvent {

		private final Path pathToClass;
		
		public ClassAddedEvent(Path pathToClass) {
			super(null);
			this.pathToClass = pathToClass;
		}

		private Path getPathToClass() {
			return pathToClass;
		}
		
		@Override
		public Class<?> getClazz() {
			if (this.clazz == null)
				try {
					this.clazz = loadClassFromPath(getPathToClass());
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return this.clazz;
		};
		
		@Override
		public ClassEvent combine(ClassEvent other) {
			if (other instanceof ClassDeletedEvent)
				return new ClassModifiedEvent(getClazz());
			else if (other instanceof ClassAddedEvent)
				return new ClassAddedEvent(getPathToClass());
			else return other.combine(this);
		}

		@Override
		public void execute() {
			Class<?> clazz = getClazz();
			getActiveClasses().put(clazz.getName(), clazz);
			Iterator<Code.Listener> it = getListeners();
			while (it.hasNext())
				it.next().classAdded(clazz);
		}

		@Override
		public boolean canCombine(ClassEvent other) {
			if (canAlwaysCombine(other))
				return true;
			if (other instanceof ClassDeletedEvent)
				return false;
			if (other instanceof ClassAddedEvent)
				return false;
			return other.canCombine(this);
		}
	}
	
	protected class ClassModifiedEvent extends ClassEvent {

		public ClassModifiedEvent(Class<?> clazz) {
			super(clazz);
			Code.this.dirtyClasses.put(clazz.getName(), clazz);
		}
		
		@Override
		public boolean canCombine(ClassEvent  other) {
			if (other == null)
				return false;
			return true;
		}

		@Override
		public ClassEvent combine(ClassEvent other) {
			return new ClassModifiedEvent(this.getClazz());
		}

		@Override
		public void execute() {
			//Java doesn't allow reloading of classes using the same class loader.
			//If we would only load in the changed class in a new loader,
			//other classes will still reference to the old version of the class in the old loader.
			//to fix this, we need to take a new ClassLoader and reload all classes.
			//Deal with it!
			//The class Project is responsible for swapping ClassLoaders when needed.
			getProject().reload();
		}
		
	}
	
}
