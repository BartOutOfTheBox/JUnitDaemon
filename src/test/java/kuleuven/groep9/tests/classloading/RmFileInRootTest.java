package kuleuven.groep9.tests.classloading;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import kuleuven.groep9.classloading.DirectoryNotifier.Listener;

public class RmFileInRootTest extends DirectoryNotifierTest {
	@Test
	public void rmFileInRoot() throws IOException, InterruptedException {
		final Path filePath = rootFile;
		setFileRMListener(filePath);
		Files.delete(rootFile);
		waitForListener();
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
				synchronized (RmFileInRootTest.this) {
					RmFileInRootTest.this.notifyAll();
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
}
