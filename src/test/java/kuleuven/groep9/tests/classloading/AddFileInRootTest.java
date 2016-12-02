package kuleuven.groep9.tests.classloading;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import kuleuven.groep9.classloading.DirectoryNotifier.Listener;

public class AddFileInRootTest extends DirectoryNotifierTest {
	@Test
	public void addFileInRoot() throws IOException, InterruptedException {
		final Path filePath = tmpPath.resolve(Paths.get("file1"));
		setFileAddedListener(filePath);
		Files.createFile(filePath);
		waitForListener();
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
				synchronized (AddFileInRootTest.this) {
					AddFileInRootTest.this.notifyAll();
				}
			}
		};
		notifier.addListener(l);
		setDirListener(l);
	}
}
