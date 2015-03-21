package kuleuven.groep9.tests.classloading;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import kuleuven.groep9.classloading.DirectoryNotifier;
import kuleuven.groep9.classloading.DirectoryNotifier.Listener;

public class ManualDirectoryNotifierTest {
	public static void main(String... args) {
		new ManualDirectoryNotifierTest();
	}
	
	public ManualDirectoryNotifierTest() {
		try {
			setupPath();
			setupNotifier();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Path tmpPath;
	private DirectoryNotifier notifier;
	
	private void setupPath() throws IOException {
		tmpPath = Files.createTempDirectory("JUnitDaemonDirNotifierTest");
		System.out.println("Created new test directory: " + tmpPath.toString());
	}
	
	private void removeTmpFiles() throws IOException {
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
	
	@SuppressWarnings("unused")
	private void breakdownPath() throws IOException {
		removeTmpFiles();
		try {
			Files.delete(tmpPath);
			System.out.println("deleted temp directory: " + tmpPath.toString());
		} catch (NoSuchFileException e) {
			e.printStackTrace();
		} catch (DirectoryNotEmptyException e) {
			e.printStackTrace();
		}
	}
	
	private void setupNotifier() {
		try {
			notifier = new DirectoryNotifier(tmpPath, 200L);
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
}
