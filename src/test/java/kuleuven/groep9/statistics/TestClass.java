package kuleuven.groep9.statistics;

import static org.junit.Assert.*;

import org.junit.*;

public class TestClass {
	private static int testNb = 0;
	private boolean failNext = false;
	
	public void doFailNext() {
		failNext = true;
	}
	
	@AfterClass
	public static void incNbTests() {
		testNb++;
	}
	
	@Test
	public void alwaysCorrect() {
		assertTrue(true);
	}

	@Test
	public void alwaysCorrect2() {
		assertTrue(true);
	}

	@Test
	public void sometimesCorrect() {
		assertTrue(testNb%2 == 0);
	}
	
	@Test
	public void neverCorrect() {
		assertTrue(false);
	}
	
	@Test
	public void neverCorrect2() {
		assertTrue(false);
	}
	
	@Test
	public void failOnCommand() {
		if (failNext) {
			failNext = false;
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}
}
