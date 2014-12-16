package kuleuven.groep9.samples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SwitchingFail {
	
	public static boolean success = true;
	
	@Test
	public void normal(){
		assertTrue(success);
	}
	
	@Test
	public void reverse(){
		assertTrue(!success);
	}
	
	public static void toggle(){
		success = !success;
	}

}
