package kuleuven.groep9.samples;

import static org.junit.Assert.*;

import org.junit.Test;

public class FailingMethods {
	
	@Test
	public void nothing1(){
		doNothing();
		assertTrue(false);
	}
	
	@Test 
	public void nothing2(){
		doNothing();
		assertTrue(false);
	}
	
	@Test
	public void notInNothing(){
		doNothing();
	}
	
	@Test
	public void throwsSomething(){
		wantError();
	}
	
	@Test
	public void throwsIndex(){
		moreElements();
	}
	
	@Test
	public void throwsOtherIndex(){
		moreElements(0);
	}
	
	@Test
	public void firstLine(){
		twoLines(20);
	}
	
	@Test
	public void secondLine(){
		twoLines(5);
	}
	
	public void doNothing(){}
	
	public void wantError(){
		throw new Error("What did you expect?");
	}
	
	public void moreElements(){
		int[] list = new int[3];
		int elem = list[10];
	}
	
	public void moreElements(int meaningless){
		int[] list = new int[3];
		int elem = list[10];
	}
	
	public void twoLines(int index){
		int[] list1 = new int[10];
		int elem = list1[index];
		
		int[] list2 = new int[3];
		elem = list2[index];
	}

}
