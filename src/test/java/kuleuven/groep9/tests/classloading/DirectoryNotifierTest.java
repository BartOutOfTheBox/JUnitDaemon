package kuleuven.groep9.tests.classloading;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import kuleuven.groep9.classloading.DirectoryNotifier;
import kuleuven.groep9.classloading.DirectoryNotifier.Listener;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class DirectoryNotifierTest {
	
	@Rule
    public Timeout globalTimeout = new Timeout(1000);
	
	private static DirectoryNotifier notifier;
	private static Path tmpPath;
	
	@BeforeClass
	public static void setup() throws IOException{
		setupPath();
		setupNotifier();
	}
	
	private static void setupPath() throws IOException {
		tmpPath = Files.createTempDirectory("JUnitDaemonDirNotifierTest");
		System.out.println("Created new test directory: " + tmpPath.toString());
	}
	
	private static void setupNotifier() {
		try {
			notifier = new DirectoryNotifier(tmpPath, true);
			notifier.addListener(new Listener() {
					
					@Override
					public void fileModified(Path absoluteDir) {
						System.out.println("\"" + absoluteDir.toString() + "\" has been modified.");
					}
					
					@Override
					public void fileDeleted(Path absoluteDir) {
						System.out.println("\"" + absoluteDir.toString() + "\" has been deleted.");
					}
					
					@Override
					public void fileAdded(Path absoluteDir) {
						System.out.println("\"" + absoluteDir.toString() + "\" has been added.");					
					}
				});
			System.out.println("listening to: " + tmpPath.toString());
			
		} catch (IOException e) {
			System.out.println("The path you tried to specify was illegal.");
		}
	}
	
	private Path rootFile;
	private Listener dirListener;
	
	@Before
	public void addRootTestFiles() throws IOException {
		rootFile = Files.createFile(tmpPath.resolve(Paths.get("rootFile")));
	}
	
	@Test
	public void addFileInRoot() throws IOException, InterruptedException {
		final Path filePath = tmpPath.resolve(Paths.get("file1"));
		setFileAddedListener(filePath);
		Files.createFile(filePath);
		waitForListener();
	}

	@Test
	public void rmFileInRoot() throws IOException, InterruptedException {
		final Path filePath = rootFile;
		setFileRMListener(filePath);
		Files.delete(rootFile);
		waitForListener();
	}

	@Test
	public void modifyFileInRoot() throws InterruptedException, IOException {
		final Path filePath = rootFile;
		setFileModifiedListener(filePath);
		try (BufferedWriter o = Files.newBufferedWriter(filePath, Charset.defaultCharset())) {
			o.write("test");
			o.flush();
		}
		waitForListener();
	}
	
	@Test
	public void addFileInSub() throws IOException, InterruptedException {
		final Path dirPath = tmpPath.resolve(Paths.get("subfolder"));
		final Path filePath = tmpPath.resolve(Paths.get("subfolder/files1"));
//		setFileAddedListener(filePath);
		Files.createDirectory(dirPath);
		Files.createFile(filePath);
//		waitForListener();
	}

	private void waitForListener() throws InterruptedException {
		synchronized (this) {
			this.wait();
			System.out.println("event received");
			notifier.removeListener(getDirListener());
		}
	}

	private void setFileAddedListener(final Path filePath) {
		Listener l = new Listener() {
			
			@Override
			public void fileModified(Path absoluteDir) {
				fail();
			}
			
			@Override
			public void fileDeleted(Path absoluteDir) {
				fail();
			}
			
			@Override
			public void fileAdded(Path absoluteDir) {
				assertTrue(absoluteDir.equals(filePath));
				synchronized (DirectoryNotifierTest.this) {
					DirectoryNotifierTest.this.notifyAll();
				}
			}
		};
		notifier.addListener(l);
		setDirListener(l);
	}

	private void setFileRMListener(final Path filePath) {
		Listener l = new Listener() {
			
			@Override
			public void fileModified(Path absoluteDir) {
				fail();
			}
			
			@Override
			public void fileDeleted(Path absoluteDir) {
				assertTrue(absoluteDir.equals(filePath));
				synchronized (DirectoryNotifierTest.this) {
					DirectoryNotifierTest.this.notifyAll();
				}
			}
			
			@Override
			public void fileAdded(Path absoluteDir) {
				fail();
			}
		};
		notifier.addListener(l);
		setDirListener(l);
	}

	private void setFileModifiedListener(final Path filePath) {
		Listener l = new Listener() {
			
			@Override
			public void fileModified(Path absoluteDir) {
				assertTrue(absoluteDir.equals(filePath));
				synchronized (DirectoryNotifierTest.this) {
					DirectoryNotifierTest.this.notifyAll();
				}
			}
			
			@Override
			public void fileDeleted(Path absoluteDir) {
				fail();
			}
			
			@Override
			public void fileAdded(Path absoluteDir) {
				fail();
			}
		};
		notifier.addListener(l);
		setDirListener(l);
	}
	
	private Listener getDirListener() {
		return dirListener;
	}

	private void setDirListener(Listener dirListener) {
		this.dirListener = dirListener;
	}

	@After
	public void afterTest() throws IOException, InterruptedException {
		removeCurrentListener();
		removeTmpFiles();
		Thread.sleep(500);
	}
	
	private void removeCurrentListener() {
		notifier.removeListener(getDirListener());
		setDirListener(null);
	}

	public void removeTmpFiles() throws IOException {
		Files.walkFileTree(tmpPath, new SimpleFileVisitor<Path>()
		{
		    @Override
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		            throws IOException {
		    	if (! file.equals(tmpPath))
		    		Files.delete(file);
		        return FileVisitResult.CONTINUE;
		    }
		
		    @Override
		    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		        // try to delete the file anyway, even if its attributes
		        // could not be read, since delete-only access is
		        // theoretically possible
		    	if (! file.equals(tmpPath))
		    		Files.delete(file);
		        return FileVisitResult.CONTINUE;
		    }
		
		    @Override
		    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		        if (exc == null) {
		        	if (! dir.equals(tmpPath))
		        		Files.delete(dir);
		            return FileVisitResult.CONTINUE;
		        }
		        else {
		        	// directory iteration failed; propagate exception
		        	throw exc;
		        }	
		    }
		});
	}
	
	@AfterClass
	public static void breakdown() throws IOException {
//		unlinkNotifier();
		breakdownPath();
	}

	@SuppressWarnings("unused")
	private static void unlinkNotifier() {
		notifier.removeListeners();
		System.out.println("deleted all listeners");
	}

	private static void breakdownPath() throws IOException {
		try {
			Files.delete(tmpPath);
			System.out.println("deleted temp directory: " + tmpPath.toString());
		} catch (NoSuchFileException e) {
			e.printStackTrace();
		} catch (DirectoryNotEmptyException e) {
			e.printStackTrace();
		}
	}
}
