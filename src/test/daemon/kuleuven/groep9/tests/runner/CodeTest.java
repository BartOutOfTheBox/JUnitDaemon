package kuleuven.groep9.tests.runner;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import kuleuven.groep9.runner.Project;

import org.junit.Before;
import org.junit.Test;

public class CodeTest {
	
	private Project testProject;
	
	@Before
	public void setProject() {
		Path codebaseDir = Paths.get("./bin");
		Path codeDir = Paths.get("./bin/kuleuven/groep9/samples/location0/");
		Path testCodebaseDir = Paths.get("./bin");
		Path testCodeDir = Paths.get("./bin/kuleuven/groep9/samples/tests/");
		try {
			this.testProject = new Project(codebaseDir, codeDir, testCodebaseDir, testCodeDir);
		} catch (IOException e) {
			//should not happen
			fail("IOException in opening the testProject. Should not happen.");
		}
	}
	
	@Test
	public void getClassNameFromPathTest() {
		Path pathToClass = Paths.get("./bin/kuleuven/groep9/samples/location0/HeadsOrTails.class");
		String classname = testProject.getTestedCode().getClassNameFromPath(pathToClass);
		String actualClassName = "kuleuven.groep9.samples.location0.HeadsOrTails";
		assertEquals(classname, actualClassName);
	}
}
