package kuleuven.groep9.tests.classloading;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import kuleuven.groep9.classloading.DirectoryNotifier.Listener;

public class ModifyFileInRootTest extends DirectoryNotifierTest {

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
	
	private void setFileModifiedListener(final Path filePath) {
		Listener l = new Listener() {
			
			@Override
			public void fileModified(Path absoluteDir) {
				assertTrue(absoluteDir.equals(filePath));
				synchronized (ModifyFileInRootTest.this) {
					ModifyFileInRootTest.this.notifyAll();
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
}
