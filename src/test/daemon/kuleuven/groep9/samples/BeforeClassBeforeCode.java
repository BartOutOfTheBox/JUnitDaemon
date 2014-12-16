package kuleuven.groep9.samples;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BeforeClassBeforeCode {
	private static int numberA = 0;
	private int numberB = 0;
	
	@BeforeClass
	public static void classSetUp(){
		numberA = 1;
	}
	
	@Before
	public void setUp(){
		numberB = 2;
	}
	
	@Test
	public void numberAOne(){
		assertTrue(numberA==1);
	}
	
	@Test
	public void numberBTwo(){
		assertTrue(numberB==2);
	}
	
	@Test
	public void changeA(){
		numberA = 13;
	}
	
	@Test
	public void changeB(){
		numberB = 23;
	}
	
	@Test
	public void shouldFail(){
		doesNothing();
		assertTrue(false);
	}
	
	@Ignore
	@Test
	public void shouldIgnore(){
		
	}
	
	public void doesNothing(){}
}
