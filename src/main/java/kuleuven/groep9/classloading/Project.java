package kuleuven.groep9.classloading;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

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
public class Project {
	private final Code testCode;
	private final Code testedCode;
	
	private int nbLoaders;
	
	//FIXME: own classloader needed? see examples online.
	private URLClassLoader classLoader;
	
	/**
	 * This constructor assumes all code contained 
	 * in the codebaseDirectories is relevant to that part of the code.
	 * 
	 * @param codebaseDir The codebase directory of the code to test.
	 * @param testCodebaseDir The codebase directory of the testcode.
	 * 
	 * @throws IOException If for some reason it's not possible to access one of the directories.
	 */
	public Project(Path codebaseDir, Path testCodebaseDir) throws IOException {
		this.nbLoaders = 0;
		this.testedCode = new Code(this, codebaseDir);
		this.testCode = new Code(this, testCodebaseDir);
		this.setNewClassLoader();
	}
	
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
		this.testedCode = new Code(this, codebaseDir, codeDir);
		this.testCode = new Code(this, testCodebaseDir, testCodeDir);
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
		System.out.println("setting new ClassLoader");
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
					System.out.println("trying to load: " + name);
					return findClass(name);
				} catch (ClassNotFoundException e) {
					System.out.println("failed to find: " + name + " myself. Lets hope super can load it.");
					Class<?> clazz =  super.loadClass(name);
					System.out.println("super loaded " + name);
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
		this.nbLoaders++;
	}
	
	/**
	 * Let this project know you are done loading new Code into it.
	 */
	void decNbLoaders() {
		this.nbLoaders--;
	}
	
	/**
	 * Check if there new classes are being loaded into this project.
	 * @return if there are new classes are being loaded into this project.
	 */
	public boolean isLoaded() {
		return (nbLoaders == 0);
	}
}
