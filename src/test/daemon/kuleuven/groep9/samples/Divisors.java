package kuleuven.groep9.samples;

import static org.junit.Assert.*;

import org.junit.Test;

public class Divisors {
	public static int number = 0;
	
	@Test
	public void two(){
		assertTrue(number%2==0);
	}
	
	@Test
	public void three(){
		if(number%3!=0){
			thirtythree();
		}
	}
	
	@Test
	public void five(){
		if(number%5!=0){
			thirtyfive();
		}
	}
	
	@Test
	public void seven(){
		if(number%7!=0){
			thirtyfive();
		}
	}
	
	@Test
	public void eleven(){
		if(number%11!=0){
			thirtythree();
		}
	}
	
	public void thirtythree(){
		throw new Error();
	}
	
	public void thirtyfive(){
		throw new Error();
	}
}
