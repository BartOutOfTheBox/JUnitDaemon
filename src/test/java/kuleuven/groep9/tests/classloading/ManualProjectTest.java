package kuleuven.groep9.tests.classloading;

import java.io.IOException;
import java.nio.file.Paths;

import kuleuven.groep9.classloading.Code;
import kuleuven.groep9.classloading.Project;

public class ManualProjectTest {
	public static void main(String... args) {
		new ManualProjectTest();
	}
	
	public ManualProjectTest() {
		try {
			final Project proj = new Project(
					Paths.get("C:\\Users\\Bart\\JavaWorkspace\\testproject\\bin"), 
					Paths.get("C:\\Users\\Bart\\JavaWorkspace\\testproject\\bin\\code"), 
					Paths.get("C:\\Users\\Bart\\JavaWorkspace\\testproject\\bin"), 
					Paths.get("C:\\Users\\Bart\\JavaWorkspace\\testproject\\bin\\tests"));
			proj.getTestedCode().addListener(new Code.Listener() {
				
				@Override
				public void classRemoved(Class<?> clazz) {
					System.out.println(">>> " + clazz.getName() + " removed.");
					System.out.println(">>> Project loading status: " + proj.isLoaded());
				}
				
				@Override
				public void classReloaded(Class<?> clazz) {
					System.out.println(">>> " + clazz.getName() + " reloaded.");
					System.out.println(">>> Project loading status: " + proj.isLoaded());
				}
				
				@Override
				public void classChanged(Class<?> clazz) {
					System.out.println(">>> " + clazz.getName() + " changed.");
					System.out.println(">>> Project loading status: " + proj.isLoaded());
				}
				
				@Override
				public void classAdded(Class<?> clazz) {
					System.out.println(">>> " + clazz.getName() + " added.");
					System.out.println(">>> Project loading status: " + proj.isLoaded());
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
