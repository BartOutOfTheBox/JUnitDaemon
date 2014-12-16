package kuleuven.groep9.tests.classloading;

import java.io.IOException;
import java.nio.file.Paths;

import kuleuven.groep9.classloading.DirectoryNotifier;

public class DirectoryNotifierManualTest {
	
	static DirectoryNotifier n;
	
	public static void main(String...args) {
		try {
			n = new DirectoryNotifier(Paths.get("C:\\Users\\Bart\\Desktop\\test"));
			n.addListener(new DirectoryNotifier.Listener() {
				@Override
				public void directoryChanged() {
					System.out.println("directory changed.");
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
