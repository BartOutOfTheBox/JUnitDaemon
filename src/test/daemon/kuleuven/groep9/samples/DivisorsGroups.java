package kuleuven.groep9.samples;

import static org.junit.Assert.*;

import org.junit.Test;

public class DivisorsGroups {
	public static int number = 0;
	
	@Test
	public void two(){
		assertTrue(number%2==0);
	}
	
	@Test
	public void three(){
		thirtythree();
		assertTrue(number%3==0);
	}
	
	@Test
	public void five(){
		thirtyfive();
		assertTrue(number%5==0);
	}
	
	@Test
	public void seven(){
		thirtyfive();
		assertTrue(number%7==0);
	}
	
	@Test
	public void eleven(){
		thirtythree();
		assertTrue(number%11==0);
	}
	
	public void thirtythree(){}
	
	public void thirtyfive(){}
}
