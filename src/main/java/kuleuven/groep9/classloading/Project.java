package kuleuven.groep9.classloading;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Iterator;

import kuleuven.groep9.Notifier;

/**
 * This class represents the subject project which is to be automatically tested by the JUnitDaemon.
 * It contains two parts of code: the test code, and the actual code (which is to be tested).
 * 
 * The project is also responsible for maintaining a current ClassLoader.
 * Code objects need to share a ClassLoader in order for both codes to be able to interact.
 * 
 * @author r0254751
 *
 */
public class Project extends Notifier<Project.Listener>{
	private final Code testCode;
	private final Code testedCode;
	
	private int nbLoaders;
	
	//FIXME: own classloader needed? see examples online.
	private URLClassLoader classLoader;
	
	/**
	 * This constructor accepts different codebasefolders
	 * and subsets of these codebasefolders containing the needed code.
	 * 
	 * @param codebaseDir The codebase directory of the code to test.
	 * @param codeDir The directory containing all code to test.
	 * @param testCodebaseDir The codebase directory of the testcode.
	 * @param testCodeDir The directory containing all testcode.
	 * 
	 * @throws IOException If for some reason it's not possible to access one of the directories.
	 */
	public Project(Path codebaseDir, Path codeDir, Path testCodebaseDir, Path testCodeDir) throws IOException {
		this.nbLoaders = 0;
		this.testedCode = new Code(this, codebaseDir, codeDir, 1000L);
		this.testCode = new Code(this, testCodebaseDir, testCodeDir, 1000L);
		this.setNewClassLoader();
	}
	
	public Code getTestCode() {
		return this.testCode;
	}
	
	public Code getTestedCode() {
		return this.testedCode;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	public void reload() {
		try {
			System.out.println("reloading project");
			incNbLoaders();
			flushClassLoader();
			getTestCode().reload();
			getTestedCode().reload();
			decNbLoaders();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Close the old {@link ClassLoader} and set a new {@link URLClassLoader} as {@link ClassLoader}.
	 * @throws IOException if closing any file opened by this class loader resulted in an IOException.
	 */
	protected void flushClassLoader() throws IOException {
		try {
			classLoader.close();
			setNewClassLoader();
		} catch (MalformedURLException e) {
			//Should not happen..
			e.printStackTrace();
		}
	}

	protected void setNewClassLoader() throws MalformedURLException {
		URLClassLoader cl = newURLClassLoader();
		this.setClassLoader(cl);
	}

	protected URLClassLoader newURLClassLoader() throws MalformedURLException {
		URL[] urls = new URL[] {
				getTestCode().getCodebaseDir().toUri().toURL(),
				getTestedCode().getCodebaseDir().toUri().toURL()};
		URLClassLoader cl = new URLClassLoader(urls) {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				try {
					return findClass(name);
				} catch (ClassNotFoundException e) {
					Class<?> clazz =  super.loadClass(name);
					return clazz;
				}
			}
		};
		return cl;
	}

	protected void setClassLoader(URLClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/**
	 * Let this project know you are loading new Code into it.
	 */
	void incNbLoaders() {
		if (nbLoaders == 0) {
			Iterator<Project.Listener> it = getListeners();
			while (it.hasNext()) {
				it.next().startedLoading();
			}
		}
		this.nbLoaders++;
	}
	
	/**
	 * Let this project know you are done loading new Code into it.
	 */
	void decNbLoaders() {
		this.nbLoaders--;
		if (nbLoaders == 0) {
			Iterator<Project.Listener> it = getListeners();
			while (it.hasNext()) {
				it.next().stoppedLoading();
			}
		}
	}
	
	/**
	 * Check if there new classes are being loaded into this project.
	 * @return if there are new classes are being loaded into this project.
	 */
	public boolean isLoaded() {
		return (nbLoaders == 0);
	}

	public interface Listener {
		public void startedLoading();
		public void stoppedLoading();
	}
}
