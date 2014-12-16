package kuleuven.groep9.runner;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class Code extends Notifier<Listener<ClassLoadedEvent>, ClassLoadedEvent> {

	private static final String JAVA_EXTENSION = "class";
	
	private final Project project;
	
	private final Path codebaseDir;
	private final Path codeDir;
	
	private Set<Path> dirtyPaths = new HashSet<Path>();
	
	private List<Class<?>> activeClasses = new ArrayList<Class<?>>();
	
	/**
	 * This DirectoryListener is responsible for calling the right methods 
	 * when a file related to the code this object represents is added/altered/deleted.
	 */
	private final Listener<DirectoryEvent> dirListener = new Listener<DirectoryEvent>() {
		
		@Override
		public void onEvent(DirectoryEvent event) {
			Path eventPath = event.getDir().resolve(event.getEvent().context());
			System.out.println("event received: " + event.getDir().resolve(eventPath));
			if (isJava(getFileName(event.getEvent()))) {
				//FIXME: this structure resembles a case like structure. 
				//Use some sort of strategy pattern?
				if (event.getEvent().kind().equals(StandardWatchEventKinds.ENTRY_DELETE)){
					System.out.println("File removed event received");
					//java doesn't allow manual unloading of classes,
					//(this isn't a problem though. If they were referenced, 
					//they couldn't be deleted without altering other files.)
					
					//but Listeners still might need to be notified,
					//since they need to remove the test class from the OverviewRunner
					//notify the ClassReloadedListeners there's a class gone.
					try {
						Class<?> clazz = loadClassFromPath(eventPath);
						activeClasses.remove(clazz);
						Code.this.notifyAllListeners(new ClassLoadedEvent(clazz, ClassLoadedEvent.Kind.DELETED));
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else if (event.getEvent().kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
					System.out.println("File created event received");
					//Load the new file
					//This can be done using the still existing ClassLoader.
					//Dependencies will still be okay, since no class actually changed.
					try {
						Class<?> clazz = loadClassFromPath(eventPath);
						activeClasses.add(clazz);
						System.out.println(clazz.getName());
						//notify the ClassReloadedListeners there's a brand new class.
						Code.this.notifyAllListeners(new ClassLoadedEvent(clazz, ClassLoadedEvent.Kind.NEW));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (event.getEvent().kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
					System.out.println("File modified event received");
					//Java doesn't allow reloading of classes using the same class loader.
					//If we would only load in the changed class in a new loader,
					//other classes will still reference to the old version of the class in the old loader.
					//to fix this, we need to take a new ClassLoader and reload all classes.
					//Deal with it!
					//The class Project is responsible for swapping ClassLoaders when needed.
					dirtyPaths.add(eventPath);
					getProject().reload();
				}
			}
		}
		
		/**
		 * Get the filename of the file related to the given event.
		 * @param event The event to examine.
		 * @return The name of the altered file.
		 */
		private String getFileName(WatchEvent<Path> event){
	        Path name = event.context();
	        Path child = name.resolve(name);
	        return child.toString();
		}
	};
	
	/**
	 * This constructor uses the dedicated constructor {@link #Code(Path, Path)} as follows:
	 * this(codeDir, codeDir)
	 * 
	 * To be used when you want to represent all code in a sourcefolder in one object.
	 * 
	 * @throws IOException
	 */
	public Code(Project project, Path codeDir) throws IOException {
		this(project, codeDir, codeDir);
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
	public Code(Project project, Path codebaseDir, Path codeDir) throws IOException {
		this.project = project;
		this.codebaseDir = codebaseDir;
		this.codeDir = codeDir;
		DirectoryNotifier dirNotifier = new DirectoryNotifier(codeDir, true);
		dirNotifier.addListener(this.getListener());
	}

	private Listener<DirectoryEvent> getListener() {
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
		System.out.println("codebasedir: " + getCodebaseDir());
		System.out.println("pathToClass: " + pathToClass);
		className = getCodebaseDir().relativize(pathToClass).toString();
		className = className.replace('/', '.');
		className = className.replace('\\', '.');
		className = className.substring(0, className.length() - JAVA_EXTENSION.length() - 1);
		System.out.println("dirived classname: " + className);
		return className;
	}

	protected Project getProject() {
		return project;
	}

	public void reload() {
		try {
			activeClasses.clear();
			Files.walkFileTree(getCodeDir(), new SimpleFileVisitor<Path>() {			    
			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
			    	throws IOException {
			    	System.out.println("reloading code.");
			    	System.out.println("now reloading: " + file);
			    	if(isJava(file.toString())){
			    		try {
							Class<?> clazz = loadClassFromPath(file);
							activeClasses.add(clazz);
							if (dirtyPaths.contains(file)) {
								notifyAllListeners(new ClassLoadedEvent(clazz, ClassLoadedEvent.Kind.CHANGED));
								dirtyPaths.remove(file);
							} else
								notifyAllListeners(new ClassLoadedEvent(clazz, ClassLoadedEvent.Kind.RELOADED));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
			    	}
			        return FileVisitResult.CONTINUE;
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Class<?>[] getActiveClasses() {
		return activeClasses.toArray(new Class<?>[activeClasses.size()]);
	}
	
}
